/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.business.action.task.core;

import com.google.common.collect.HashBiMap;
import java.io.InputStream;
import java.util.*;
import javassist.NotFoundException;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectGroupDao;
import org.igov.service.business.email.EmailProcessSubjectService;
import org.igov.service.business.subject.SubjectGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.igov.service.business.subject.SubjectGroupTreeService;
import org.igov.service.conf.AttachmetService;
import org.igov.service.exception.CommonServiceException;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Сервис получения списка пользователей, если указан id групы
 *
 * @author inna
 */
@Component("usersService")
@Service
public class UsersService {

    private static final Logger LOG = LoggerFactory.getLogger(UsersService.class);

    private static final String DEFAULT_GROUP_TYPE = "assignment";

    @Autowired
    private IdentityService oIdentityService;

    @Autowired
    private RuntimeService oRuntimeService;

    @Autowired
    private AttachmetService oAttachmetService;

    @Autowired
    SubjectGroupTreeService oSubjectGroupTreeService;
    
    @Autowired
    SubjectGroupService oSubjectGroupService;
    
    @Autowired
    SubjectGroupDao oSubjectGroupDao; 
    
    @Autowired
    EmailProcessSubjectService oEmailProcessSubjectService;

    public List<Map<String, String>> getUsersByGroup(String sID_Group, String sFind) {

        List<Map<String, String>> amsUsers = new ArrayList<>(); 
        List<User> aoUsers;
        if (sFind != null) {
            aoUsers = oIdentityService.createNativeUserQuery().sql("SELECT * FROM \"act_id_user\" where UPPER(\"first_\") "
                    + "LIKE UPPER('%" + sFind + "%') OR UPPER(\"id_\") LIKE UPPER('%" + sFind + "%') OR UPPER(\"last_\") LIKE UPPER('%" + sFind + "%')").list();
        } else {
            aoUsers = oIdentityService.createUserQuery().memberOfGroup(sID_Group).list();
        }

        for (User oUser : aoUsers) {
            Map<String, String> mUserInfo = new LinkedHashMap();

            mUserInfo.put("sLogin", oUser.getId() == null ? "" : oUser.getId());
            // mUserInfo.put("sPassword", oUser.getPassword() == null ? "" : oUser.getPassword());
            mUserInfo.put("sFirstName", oUser.getFirstName() == null ? "" : oUser.getFirstName());
            mUserInfo.put("sLastName", oUser.getLastName() == null ? "" : oUser.getLastName());
            mUserInfo.put("sEmail", oUser.getEmail() == null ? "" : oUser.getEmail());
            mUserInfo.put("FirstName", oUser.getFirstName() == null ? "" : oUser.getFirstName());
            mUserInfo.put("LastName", oUser.getLastName() == null ? "" : oUser.getLastName());
            mUserInfo.put("Email", oUser.getEmail() == null ? "" : oUser.getEmail());
            mUserInfo.put("Picture", null); // Временно ставим картинку null, позже будет изменение на Base64 или ссылка
            amsUsers.add(mUserInfo);
        }

        return amsUsers;

    }

    public List<String> getUsersLoginByGroup(String sID_Group) {

        List<String> aUsers = new ArrayList<>(); // для возвращения результата, ибо возникает JsonMappingException и NullPointerException при записи картинки
        List<User> aoUsers = sID_Group != null
                ? oIdentityService.createUserQuery().memberOfGroup(sID_Group).list()
                : oIdentityService.createUserQuery().list();
        for (User oUser : aoUsers) {
            aUsers.add(oUser.getId());
        }

        LOG.info("aUsers in getUsersLoginByGroup {}", aUsers);
        return aUsers;
    }

    public List<String> getUsersEmailByGroup(String sID_Group) {

        List<String> aUsersEmail = new ArrayList<>();
        List<User> aoUsers = sID_Group != null
                ? oIdentityService.createUserQuery().memberOfGroup(sID_Group).list()
                : oIdentityService.createUserQuery().list();
        for (User oUser : aoUsers) {
            aUsersEmail.add(oUser.getEmail());
        }
        return aUsersEmail;
    }

    /**
     * create/update record in act_id_user, act_id_group, act_id_membership
     */
    public void setHuman(String sID_Group, String sPassword, String sFamily, String sName, String sSurname, String sEmail) {
        User user = oIdentityService.createUserQuery().userId(sID_Group).singleResult();
        boolean isCreate = user == null;
        if (isCreate) {
            user = oIdentityService.newUser(sID_Group);
            user.setPassword(sPassword); // updates are via REST
        }
        user.setFirstName(sFamily);
        user.setLastName(sName + " " + sSurname);
        user.setEmail(sEmail);
        oIdentityService.saveUser(user);

        Group group = getOrCreateGroup(sID_Group);
        group.setName(sFamily + " " + sName + " " + sSurname);
        group.setType(DEFAULT_GROUP_TYPE);
        oIdentityService.saveGroup(group);

        // avoid membership creation on update
        if (isCreate) {
            oIdentityService.createMembership(sID_Group, sID_Group);
            LOG.debug("set activiti membership, userId:'{}', groupId:'{}'", sID_Group, sID_Group);
        }
    }

    private User getOrCreateUser(String id) {
        User user = oIdentityService.createUserQuery().userId(id).singleResult();
        if (user == null) {
            user = oIdentityService.newUser(id);
        }
        return user;
    }

    private Group getOrCreateGroup(String id) {
        Group group = oIdentityService.createGroupQuery().groupId(id).singleResult();
        if (group == null) {
            group = oIdentityService.newGroup(id);
        }
        return group;
    }

    public void setUserGroup(String sID_Group_Activiti, String sLogin, Boolean bSendMail) throws Exception {
        if (!StringUtils.isAnyEmpty(sID_Group_Activiti, sLogin)) {
            LOG.info("Group id and user login are not empty");

            Set<String> asGroup = getUserGroupMember(sID_Group_Activiti);

            if (asGroup.contains(sLogin)) {

                String sNameReferent = "";
                String sNameHost = "";

                SubjectGroup oSubjectGroup = oSubjectGroupDao.findBy("sID_Group_Activiti", sID_Group_Activiti).orNull();
                if (oSubjectGroup != null) {
                    sNameReferent = oSubjectGroup.getName();
                }

                oSubjectGroup = oSubjectGroupDao.findBy("sID_Group_Activiti", sLogin).orNull();
                if (oSubjectGroup != null) {
                    sNameHost = oSubjectGroup.getName();
                }

                throw new RuntimeException("Користувач " + sNameReferent + "(" + sID_Group_Activiti + ") "
                        + "вже є референтом у " + sNameHost + "(" + sLogin + ")!");
            }

            asGroup = getUserGroupMember(sLogin);

            if (asGroup.contains(sID_Group_Activiti)) {

                String sNameReferent = "";
                String sNameHost = "";

                SubjectGroup oSubjectGroup = oSubjectGroupDao.findBy("sID_Group_Activiti", sLogin).orNull();
                if (oSubjectGroup != null) {
                    sNameReferent = oSubjectGroup.getName();
                }

                oSubjectGroup = oSubjectGroupDao.findBy("sID_Group_Activiti", sID_Group_Activiti).orNull();
                if (oSubjectGroup != null) {
                    sNameHost = oSubjectGroup.getName();
                }

                throw new RuntimeException("Користувач " + sNameReferent + "(" + sLogin + ") "
                        + "вже є референтом у " + sNameHost + "(" + sID_Group_Activiti + ")!");
            }

            if (bSendMail && !sID_Group_Activiti.equalsIgnoreCase("admin") && !sID_Group_Activiti.equalsIgnoreCase("superadmin") 
                    && !sID_Group_Activiti.equalsIgnoreCase("tester") && !sID_Group_Activiti.equals(sLogin) 
                    && !sID_Group_Activiti.equalsIgnoreCase("btsol_200687tov1")) {
                oEmailProcessSubjectService.sendEmail_referent(sID_Group_Activiti, sLogin);
            }            
            oIdentityService.createMembership(sLogin, sID_Group_Activiti);
            LOG.info("Membership for user " + sLogin + " in group " + sID_Group_Activiti + " created");
        }
    }

    public void removeUserGroup(String sID_Group_Activiti, String sLogin) throws Exception {
        if (!StringUtils.isAnyEmpty(sID_Group_Activiti, sLogin)) {
            LOG.info("Group id and user login are not empty");

            /*Set<String> asGroup = getUserGroupMember(sLogin);

            if (!asGroup.contains(sID_Group_Activiti)) {

                String sNameReferent = "";
                String sNameHost = "";

                SubjectGroup oSubjectGroup = oSubjectGroupDao.findBy("sID_Group_Activiti", sLogin).orNull();
                if (oSubjectGroup != null) {
                    sNameReferent = oSubjectGroup.getName();
                }

                oSubjectGroup = oSubjectGroupDao.findBy("sID_Group_Activiti", sID_Group_Activiti).orNull();
                if (oSubjectGroup != null) {
                    sNameHost = oSubjectGroup.getName();
                }

                throw new RuntimeException("Користувач " + sNameReferent + "(" + sLogin + ") "
                        + "не є референтом у " + sNameHost + "(" + sID_Group_Activiti + ") тому ви не можете його вилучити!");
            }*/

            oIdentityService.deleteMembership(sLogin, sID_Group_Activiti);
            LOG.info("Membership for user " + sLogin + " in group " + sID_Group_Activiti + " removed");
        }
    }

    public List<Map<String, String>> getFioUserGroupMember(String sLogin) throws Exception {
        List<Map<String, String>> aResMap = new ArrayList<>();
        Set<String> oGroupSet = getUserGroupMember(sLogin);
        
        for(String sGroupLogin : oGroupSet){
            Map<String, String> mReturn = new HashMap<>();
            mReturn.put("sLogin", sGroupLogin);
            mReturn.put("sFio", getUserFIObyLogin(sGroupLogin));
            aResMap.add(mReturn);
        }
        
        return aResMap;
    }

    public Set<String> getUserGroupMember(String sLogin) throws Exception {
        LOG.info("getUserGroupMember started...");
        if (sLogin != null && !sLogin.isEmpty()) {
            List<Group> aGroup = oIdentityService.createGroupQuery().groupMember(sLogin).list();
            Set<String> asID_Group = new HashSet<>();

            if (aGroup != null) {
                for (Group oGroup : aGroup) {
                    LOG.info("oGroup id {}", oGroup.getId());
                    String sSubjectType = oSubjectGroupService.getSubjectType(oGroup.getId());
                    LOG.info("sSubjectType {}", sSubjectType);

                    if (sSubjectType.equalsIgnoreCase("HUMAN")) {
                        asID_Group.add(oGroup.getId());
                    }
                }
            } else {
                throw new RuntimeException("Can't find any group by login");
            }

            return asID_Group;
        } else {
            throw new RuntimeException("sLogin is invalid");
        }
    }

    public String getUserFIObyLogin(String sLogin) {

        return oSubjectGroupDao.findBy("sID_Group_Activiti", sLogin).get()
                .getoSubject().getsLabel();
    }
    
    public Map<String, Object> getmUserProperty(String sID_Group) throws NotFoundException {
        List<Map<String, Object>> amUserProperty = getamUserProperty(sID_Group, true);
        return amUserProperty.size() > 0 ? amUserProperty.get(0) : null;
    }

    public List<Map<String, Object>> getamUserProperty(String sID_Group, boolean bFilterGroup) throws NotFoundException {
        List<User> aUser = oIdentityService.createUserQuery().memberOfGroup(sID_Group).list();
        LOG.info("getDocumentStepLogins sID_Group={}, aUser={}", sID_Group, aUser);
        List<Map<String, Object>> amUserProperty = new LinkedList();
        for (User oUser : aUser) {
            if (!bFilterGroup || oUser.getId().equalsIgnoreCase(sID_Group)) {
                String sLogin = oUser.getId();
                LOG.info("oUser.getId() is {}", sLogin);
                String sSubjectHumanPosition = null, sCompany = null;
                SubjectGroup oSubjectGroup = oSubjectGroupDao.findBy("sID_Group_Activiti", sLogin).orNull();
                if (oSubjectGroup != null) {
                    sSubjectHumanPosition = oSubjectGroup.getoSubjectHumanPositionCustom().getsNote();
                    SubjectGroup oSubjectGroup_Company = oSubjectGroupTreeService.getCompany(sLogin); //получаем компанию, в котором пользователь
                    sCompany = oSubjectGroup_Company.getoSubject().getsLabel();
                }
                Map<String, Object> mUser = new HashMap();
                mUser.put("sLogin", sLogin);
                mUser.put("sID_Group", sID_Group);
                mUser.put("sFIO", oUser.getFirstName() + " " + oUser.getLastName());
                mUser.put("sSubjectHumanPosition", sSubjectHumanPosition);
                mUser.put("sCompany", sCompany);
                amUserProperty.add(mUser);
            }
        }
        return amUserProperty;
    }
    
    public String getUserInitials(String sFIO) {
        String sInitial = "";
        try {
            String[] aFIO = sFIO.split(" ");
            String sSurname = aFIO[0];
            String sFirstInitial = aFIO[1].substring(0, 1);
            String sMiddleInitial = aFIO[2].substring(0, 1);
            sInitial = sSurname + " " + sFirstInitial + ". " + sMiddleInitial + ".";            
        } catch (Exception e) {
            LOG.info("Error in getUserInitials: ", e);
        }        
        return sInitial;
    }
    /**
     * @author Belichenko Oleksandr
     * Парсинг тела запроса при добавлении/редактировании пользователя. Возвращает мапу данных
     *     
     * @param sBody       JSON-объект, с параметрами sPassword, sName, sDescription и опциональным sEmail
     * @return Map<String, Object> mResult
     */
    public Map<String, Object> parsingUserBody(String sBody) throws Exception {
        Map<String, Object> mResult = new HashMap<String, Object>();        
        if(sBody != null){
            Map<String, Object> mBody;
            try {
                mBody = (Map<String, Object>) JSONValue.parse(sBody);
            } catch (Exception e){
                throw new IllegalArgumentException("Error parse JSON body: " + e.getMessage());
            }
            if(mBody != null){
                if (mBody.containsKey("sPassword") && (mBody.get("sPassword") != null)) {                    
                    mResult.put("sPassword", (String) mBody.get("sPassword"));
                }
                if (mBody.containsKey("sName") && (mBody.get("sName") != null)) {                    
                    mResult.put("sName", (String) mBody.get("sName"));
                } else {
                    throw new Exception("The sName in RequestBody is not defined");
                }
                if (mBody.containsKey("sDescription") && (mBody.get("sDescription") != null)) {                    
                    mResult.put("sDescription", (String) mBody.get("sDescription"));
                } else {
                    throw new Exception("The sDescription in RequestBody is not defined");
                }
                if (mBody.containsKey("sEmail") && (mBody.get("sEmail") != null)) {                   
                    mResult.put("sEmail", (String) mBody.get("sEmail"));
                } 
            }
        }
        return mResult;
    }
    
    /**
     * @author Belichenko Oleksandr
     * Добавление пользователя. Вернет ошибку, если он уже существует.
     *
     * @param sLogin       строка текст, логин пользователя для определения наличия пользователя в базе
     * @param sPassword    строка текст, логин пользователя для определения наличия пользователя в базе
     * @param sName        строка текст, имя пользователя
     * @param sDescription строка текст, фамилия пользователя
     * @param sEmail       строка текст, имейл пользователя, опциональный параметр
     */
    public void setUser(String sLogin, String sPassword, String sName, String sDescription, String sEmail) throws CommonServiceException {
        User oUser = oIdentityService.createUserQuery().userId(sLogin).singleResult();
        if (oUser == null) {
            LOG.info("Creating new user");
            oUser = oIdentityService.newUser(sLogin);
            if (sPassword == null || sPassword.equals("")) {
                throw new CommonServiceException(new Exception().getMessage(), "The password for new User is not defined");                
            } else {
                oUser.setPassword(sPassword);
            }
            oUser.setFirstName(sName);
            oUser.setLastName(sDescription);
            if (sEmail != null) {
                oUser.setEmail(sEmail);
            }
            LOG.info("Saving user to database");
            oIdentityService.saveUser(oUser);
        }
        else {
            throw new CommonServiceException(new Exception().getMessage(), "Цей користувач вже існує!"); 
        }
    }
    
    /**
     * @author Belichenko Oleksandr
     * Редактирование пользователя. Вернет ошибку, если он не существует.
     *
     * @param sLogin       строка текст, логин пользователя для определения наличия пользователя в базе
     * @param sPassword    строка текст, логин пользователя для определения наличия пользователя в базе
     * @param sName        строка текст, имя пользователя
     * @param sDescription строка текст, фамилия пользователя
     * @param sEmail       строка текст, имейл пользователя, опциональный параметр
     */
    public void updateUser(String sLogin, String sPassword, String sName, String sDescription, String sEmail) throws CommonServiceException {
        
        User oUser = oIdentityService.createUserQuery().userId(sLogin).singleResult();
        if (oUser != null) {
            LOG.info("Updating user");
            if (sPassword == null || sPassword.equals("")) {
                throw new CommonServiceException(new Exception().getMessage(), "The password for new User is not defined");
            } else {
                oUser.setPassword(sPassword);
            }
            oUser.setFirstName(sName);
            oUser.setLastName(sDescription);
            oUser.setId(sLogin);
            if (sEmail != null) {
                oUser.setEmail(sEmail);
            }
            LOG.info("Saving user to database");
            oIdentityService.saveUser(oUser);
        }  
        else {
            throw new CommonServiceException(new Exception().getMessage(), "Цей користувач не існує, ви не можете його змінити!"); 
        }
    }
    
    /**
     * @author Belichenko Oleksandr
     * Добавление группы. Вернет ошибку, если она уже существует.
     *     
     * @param sID    строка, которая содержит число, id групы
     * @param sName  строка текст, название групы    
     */
    public void setGroup(String sID, String sName) throws CommonServiceException {
        
        Group oGroup = oIdentityService.createGroupQuery().groupId(sID).singleResult();
        if (oGroup == null) {
            LOG.info("Creating new group");
            oGroup = oIdentityService.newGroup(sID);
            oGroup.setName(sName);
            LOG.info("Saving to database");
            oIdentityService.saveGroup(oGroup);
        }
        else {
            throw new CommonServiceException(new Exception().getMessage(), "Ця група вже існує!"); 
        }
    }
    
    /**
     * @author Belichenko Oleksandr
     * Редактирование группы. Вернет ошибку, если она не существует.
     *     
     * @param sID    строка, которая содержит число, id групы
     * @param sName  строка текст, название групы    
     */
    public void updateGroup(String sID, String sName) throws CommonServiceException {
        
        Group oGroup = oIdentityService.createGroupQuery().groupId(sID).singleResult();
        if (oGroup != null) {
            LOG.info("Updating group");            
            oGroup.setName(sName);
            LOG.info("Saving to database");
            oIdentityService.saveGroup(oGroup);
        }
        else {
            throw new CommonServiceException(new Exception().getMessage(), "Ця група не існує, ви не можете її змінити"); 
        }
    }

    /**
     * @author Belichenko Oleksandr
     * Добавление роли админа пользователю
     * 
     * @param sLogin логин пользователя, которого необходимо сделать админом   
     */
    public void setAdminRole(String sLogin) {
        if (!StringUtils.isAnyEmpty(sLogin)) {
            oIdentityService.createMembership(sLogin, "admin");
            LOG.info(sLogin + " is admin now");
        }
    } 
}
