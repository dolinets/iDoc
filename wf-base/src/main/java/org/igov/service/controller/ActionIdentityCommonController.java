package org.igov.service.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.igov.service.business.action.task.core.UsersService;
import org.igov.service.exception.CommonServiceException;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.igov.service.business.serverEntitySync.ServerEntitySyncService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import org.igov.model.action.vo.UserDataVO;
import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;

@Controller
@Api(tags = { "ActionIdentityCommonController" })
@RequestMapping(value = "/action/identity")
public class ActionIdentityCommonController {

    private static final Logger LOG = LoggerFactory.getLogger(ActionIdentityCommonController.class);

    @Autowired
    private IdentityService identityService;

    @Autowired
    private TaskService taskService;
    
    @Autowired
    private UsersService usersService;
    
    @Autowired 
    private ServerEntitySyncService oServerEntitySyncService;

    /**
     * @modified Belichenko Oleksandr
     * Добавление пользователя.
     *
     * @param sLogin       строка текст, логин пользователя для определения наличия пользователя в базе
     * //@param sPassword    строка текст, логин пользователя для определения наличия пользователя в базе
     * //@param sName        строка текст, имя пользователя
     * //@param sDescription строка текст, фамилия пользователя
     * //@param sEmail       строка текст, имейл пользователя, опциональный параметр
     */
    @ApiOperation(value = "Добавление пользователя. Если пользователь с указаным логином "
            + "существует, - то произойдет ошибка."
            + "Если же пользователь с указанным логином не найден, - будет создана новая запись.")
    @RequestMapping(value = "/setUser", method = { /*RequestMethod.GET,*/ RequestMethod.POST })
    @ResponseBody
    public void setUser(
            @ApiParam(value = "строка текст, логин пользователя для определения наличия пользователя в базе", required = true) @RequestParam(value = "sLogin", required = true) String sLogin,
            //@ApiParam(value = "строка текст, пароль для пользователя", required = true) @RequestParam(value = "sPassword", required = true) String sPassword,
            //@ApiParam(value = "строка текст, имя пользователя", required = true) @RequestParam(value = "sName", required = true) String sName,
            //@ApiParam(value = "строка текст, фамилия пользователя", required = true) @RequestParam(value = "sDescription", required = true) String sDescription,
            //@ApiParam(value = "строка текст, имейл пользователя, опциональный параметр", required = false) @RequestParam(value = "sEmail", required = false) String sEmail,
            @ApiParam(value = "JSON-объект с параметрами: " +
                    "sPassword - (обязательный при создании нового пользователя) строка текст, логин пользователя для определения наличия пользователя в базе; " +
                    "sName - (обязательный) строка текст, имя пользователя; " +
                    "sDescription - (обязательный) строка текст, фамилия пользователя; " +
                    "sEmail - строка текст, имейл пользователя, опциональный параметр", required = true) @RequestBody String body)
            throws Exception {

        LOG.info("Method setUser started");
        String sPassword = null;
        String sName = null;
        String sDescription = null;
        String sEmail = null;
        
        Map<String, Object> mParams = usersService.parsingUserBody(body);
        
        if(!mParams.isEmpty() && mParams != null){
          
            if (mParams.containsKey("sPassword")) {
                sPassword = mParams.get("sPassword").toString();
            }
             
            sName = mParams.get("sName").toString();
            sDescription = mParams.get("sDescription").toString();
            
            if (mParams.containsKey("sEmail")) {
                sEmail = mParams.get("sEmail").toString();
            }
            
            usersService.setUser(sLogin, sPassword, sName, sDescription, sEmail);
        }
        else {
            throw new RuntimeException("Bad data for creating a new user");
        }
    }
    
    /**
     * @author Belichenko Oleksandr
     * Обновление пользователя.
     *
     * @param sLogin строка текст, логин пользователя для определения наличия
     * пользователя в базе 
     */
    @ApiOperation(value = "Перезапись пользователя. Если пользователь с указаным логином "
            + "существует, - то произойдет перезапись пользователя."
            + "Если же пользователь с указанным логином не найден, - будет ошибка.")
    @RequestMapping(value = "/updateUser", method = { RequestMethod.POST})
    @ResponseBody
    public void updateUser(
            @ApiParam(value = "строка текст, логин пользователя для определения наличия пользователя в базе", required = true) @RequestParam(value = "sLogin", required = true) String sLogin,            
            @ApiParam(value = "JSON-объект с параметрами: "
                    + "sPassword - (обязательный при обновлении пользователя) строка текст, логин пользователя для определения наличия пользователя в базе; "
                    + "sName - (обязательный) строка текст, имя пользователя; "
                    + "sDescription - (обязательный) строка текст, фамилия пользователя; "
                    + "sEmail - строка текст, имейл пользователя, опциональный параметр", required = true) @RequestBody String body)
            throws Exception {
        
        LOG.info("Method updateUser started");
        String sPassword = null;
        String sName = null;
        String sDescription = null;
        String sEmail = null;

        Map<String, Object> mParams = usersService.parsingUserBody(body);

        if (!mParams.isEmpty() && mParams != null) {
            
            if (mParams.containsKey("sPassword")) {
                sPassword = mParams.get("sPassword").toString();
            }  
            
            sName = mParams.get("sName").toString();
            sDescription = mParams.get("sDescription").toString();
            
            if (mParams.containsKey("sEmail")) {
                sEmail = mParams.get("sEmail").toString();
            }

            usersService.updateUser(sLogin, sPassword, sName, sDescription, sEmail);
        } else {
            throw new RuntimeException("Bad data for updating a user");
        }
    }

    /**
     * @modified Belichenko Oleksandr
     * Добавление групы.
     *
     * @param sID   строка, которая содержит число, id групы
     * @param sName строка текст, название групы
     */
    @ApiOperation(value = "Добавление/обновление групы. Если група с указаным id "
            + "существует, - то произойдет ошибка."
            + "Если же група с указанным id не найдена, - будет создана новая запись.")
    @RequestMapping(value = "/setGroup", method = { RequestMethod.GET })
    @ResponseBody
    public void setGroup(
            @ApiParam(value = "строка, которая содержит число, id групы", required = true) @RequestParam(value = "sID", required = true) String sID,
            @ApiParam(value = "строка текст, название групы", required = true) @RequestParam(value = "sName", required = true) String sName)
            throws Exception {

        LOG.info("Method setGroup startred");
        if ((!sID.isEmpty() && sID != null) && (!sName.isEmpty() && sName != null)) {
             usersService.setGroup(sID, sName);
        }else {
            throw new RuntimeException("Bad data for creating a new group");
        }
    }
    
    /**
     * @author Belichenko Oleksandr
     * Обновление групы.
     *
     * @param sID строка, которая содержит число, id групы
     * @param sName строка текст, название групы
     */
    @ApiOperation(value = "Обновление групы. Если група с указаным id "
            + "существует, - то происходит перезапись существующих данных указанными."
            + "Если же група с указанным id не найдена, - будет ошибка.")
    @RequestMapping(value = "/updateGroup", method = {RequestMethod.GET})
    @ResponseBody
    public void updateGroup(
            @ApiParam(value = "строка, которая содержит число, id групы", required = true) @RequestParam(value = "sID", required = true) String sID,
            @ApiParam(value = "строка текст, название групы", required = true) @RequestParam(value = "sName", required = true) String sName)
            throws Exception {

        LOG.info("Method updateGroup startred");
        if ((!sID.isEmpty() && sID != null) && (!sName.isEmpty() && sName != null)) {
            usersService.updateGroup(sID, sName);
        } else {
            throw new RuntimeException("Bad data for updating a group");
        }
    }

    /**
     * Возвращает список груп, если указан логин пользователя, - выводит все его групы, иначе, по умолчанию - все групы.
     *
     * @param sLogin строка текст, логин пользователя, опциональный параметр
     */
    @ApiOperation(value = "Возвращает список груп, если указан логин пользователя, - выводит все его групы, иначе, по умолчанию"
                    + " возвращает все существующие групы.")
    @RequestMapping(value = "/getGroups", method = RequestMethod.GET)
    @ResponseBody
    public List<Group> getGroups(
            @ApiParam(value = "строка текст, логин пользователя, опциональный параметр", required = false) 
            @RequestParam(value = "sLogin", required = false) String sLogin,
            @ApiParam(value = "фильтр по названию, опциональный параметр", required = false) 
            @RequestParam(value = "sFind", required = false) String sFind) 
    {
        
        LOG.info("Method getGroups startred");
        if(sLogin != null){
            return identityService.createGroupQuery().groupMember(sLogin).list();
        }
        else if (sFind != null){
           //List<Group> aGroup = identityService.createGroupQuery().groupNameLike("%" + sFind + "%").list();
           
           //if(aGroup == null || aGroup.isEmpty()){
            List<Group> aGroup = new ArrayList<>();
               //aGroup.addAll(identityService.createGroupQuery().groupMember(sFind).list());
               //aGroup.addAll(identityService.createNativeGroupQuery().sql("SELECT * FROM \"ACT_ID_GROUP\" where ").list());
           //}
           aGroup.addAll(identityService.createNativeGroupQuery().sql("SELECT * FROM \"act_id_group\" where UPPER(\"name_\") "
                   + "LIKE UPPER('%" + sFind + "%') OR UPPER(\"id_\") LIKE UPPER('%" + sFind + "%')").list());
           return aGroup;
        }
        else{
            return identityService.createGroupQuery().listPage(0, 10);
        }
        
    }

    /**
     * Возвращает список пользователей, если указан id групы, - выводит всех ее пользователей, иначе, по умолчанию - всех пользователей.
     *
     * @param sID_Group строка, которая содержит число, id групы, опциональный параметр
     */
    @ApiOperation(value = "Возвращает список пользователей, если указан id групы, - выводит всех ее пользователей, иначе, по умолчанию"
                    + " возвращает всех существующих пользователей.")
    @RequestMapping(value = "/getUsers", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, String>> getUsers(
            @ApiParam(value = "строка, которая содержит число, id групы, опциональный параметр", required = false) 
            @RequestParam(value = "sID_Group", required = false) String sID_Group,
            @ApiParam(value = "фильтр по ФИО, опциональный параметр", required = false) 
            @RequestParam(value = "sFind", required = false) String sFind) {

        LOG.info("Method getUsers startred");
        List<Map<String, String>> amsUsers = null;
        try {
        	amsUsers = usersService.getUsersByGroup(sID_Group, sFind);
    		
    	} catch (Exception e) {
    		LOG.error("FAIL: ", e);
        }

        return amsUsers;
    }

    /**
     * Удаляет пользователя с указанным логином
     *
     * @param sLogin строка текст, логин пользователя, которого необходимо удалить
     */
    @ApiOperation(value = "Удаляет пользователя с указанным логином")
    @RequestMapping(value = "/removeUser", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeUser(
            @ApiParam(value = "строка текст, логин пользователя, которого необходимо удалить", required = true) @RequestParam(value = "sLogin", required = true) String sLogin)
            throws Exception {

        identityService.deleteUser(sLogin);
    }

    /**
     * Удаляет групу с указаным id. Если група содержит пользователей, - будет выброшена ошибка
     * которая будет содержать данные о списке пользователей в этой групе. Если же група имеет задание (таску)
     * то при попытке ее удалить будет получена ошибка, которая будет содержать данные о списке доступных заданий.
     *
     * @param sID строка, которая содержит число, id групы, которую необходимо удалить
     */
    @ApiOperation(value = "Удаляет групу с указаным id. Если група содержит пользователей, - будет выброшена ошибка "
            + "которая будет содержать данные о списке пользователей в этой групе. Если же група имеет задание (таску) "
            + "то при попытке ее удалить будет получена ошибка, которая будет содержать данные о списке доступных заданий.")
    @RequestMapping(value = "/removeGroup", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeGroup(
            @ApiParam(value = "строка, которая содержит число, id групы", required = true) @RequestParam(value = "sID", required = true) String sID)
            throws CommonServiceException {

        LOG.info("Method removeGroup startred");
        List<User> aoUsers = identityService.createUserQuery().memberOfGroup(sID).list();
        if (aoUsers.size() != 0) {
            List<String> asLogins = new ArrayList<>();
            aoUsers.forEach(u -> asLogins.add(u.getId()));
            LOG.warn("Can not remove group { } because it contains users { }", sID, asLogins);
            throw new CommonServiceException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Can not remove group " + sID +
                    " because it contains users " + asLogins);
        }
        List<Task> aoTasks = taskService.createTaskQuery().taskCandidateGroup(sID).list();
        if (aoTasks.size() != 0) {
            List<String> asTasks = new ArrayList<>();
            aoTasks.forEach(t -> asTasks.add(t.getId()));
            LOG.warn("Can not remove group { } because it has accessible tasks { }", sID, aoTasks);
            throw new CommonServiceException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Can not remove group " + sID + " because it has accessible tasks " + aoTasks);
        }

        identityService.deleteGroup(sID);
    }

    /**
     * Добавляет пользователя как члена групы
     *
     * @param sID_Group строка текст, айди групы, в которую нужно добавить пользователя
     * @param sLogin    строка текст, логин пользователя, которого необходимо добавить
     */
    @ApiOperation(value = "Добавляет пользователя как члена групы")
    @RequestMapping(value = "/setUserGroup", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public void setUserGroup(
            @ApiParam(value = "строка текст, айди групы, в которую нужно добавить пользователя", required = true) @RequestParam(value = "sID_Group", required = true) String sID_Group,
            @ApiParam(value = "строка текст, логин пользователя, которого необходимо удалить (админка)", required = true) @RequestParam(value = "sLoginStaff", required = false) String sLoginStaff,
            @ApiParam(value = "строка текст, логин пользователя, которого необходимо добавить", required = true) @RequestParam(value = "sLogin", required = false) String sLogin)
            throws Exception {
        
        if(sLoginStaff != null){
            sLogin = sLoginStaff;
        }
        
        final String sLogin_final = sLogin;
        
        usersService.setUserGroup(sID_Group, sLogin_final, true);
        
        oServerEntitySyncService.addRecordToServerEntitySync(sLogin_final, oServerEntitySyncService.UPDATE_ACTION, "SubjectHuman");
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            LOG.info("cannot sleep thread..");
        }
        
        LOG.info("thread sleeped..");
        new Thread(new Runnable() {
            public void run() {
                oServerEntitySyncService.runServerEntitySync("SubjectHuman", sLogin_final);
            }
        }).start();
    }

    /**
     * Удаляет членство пользователя в групе
     *
     * @param sID_Group строка текст, айди групы, из которой необходимо удалить юзера
     * @param sLogin    строка текст, логин пользователя, которого необходимо удалить
     */
    @ApiOperation(value = "Удаляет членство пользователя в групе")
    @RequestMapping(value = "/removeUserGroup", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeUserGroup(
            @ApiParam(value = "строка текст, айди групы, из которой необходимо удалить юзера", required = true) @RequestParam(value = "sID_Group_Activiti", required = true) String sID_Group_Activiti,
            @ApiParam(value = "строка текст, логин пользователя, которого необходимо удалить", required = true) @RequestParam(value = "sLogin", required = false) String sLogin,
            @ApiParam(value = "строка текст, логин пользователя, которого необходимо удалить (админка)", required = true) @RequestParam(value = "sLoginStaff", required = false) String sLoginStaff,
            @ApiParam(value = "синхронизация", required = false) @RequestParam(value = "isSync", required = false) Boolean isSync)
            throws Exception 
    {
        if(sLoginStaff != null){
            sLogin = sLoginStaff;
        }
        final String sLogin_final = sLogin;
        
        usersService.removeUserGroup(sID_Group_Activiti, sLogin_final);
        
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            LOG.info("cannot sleep thread..");
        }
        
        LOG.info("thread sleeped..");
        if(isSync == null || isSync == false){
             oServerEntitySyncService.addRecordToServerEntitySync(sLogin_final, oServerEntitySyncService.UPDATE_ACTION, "SubjectHuman");
              
          new Thread(new Runnable() {
              public void run() {
                  oServerEntitySyncService.runServerEntitySync("SubjectHuman", sLogin_final);
                  }
              }).start();
          }
        
    }
    
    /**
     * Возвращает мапу, которыйсодержит ФИО и перечень групп, в которые входит логин.
     *
     * @param sLogin    строка текст, логин пользователя, которого необходимо удалить
     * @return 
     * @throws java.lang.Exception
     */
    @ApiOperation(value = "Возвращает обьект обвертку, которыйсодержит ФИО и перечень групп, в которые входит логин.")
    @RequestMapping(value = "/getUserGroupMember", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, String>> getUserGroupMember(
            @ApiParam(value = "логин пользователя", required = true) @RequestParam(value = "sLogin", required = false) String sLogin,
            @ApiParam(value = "логин пользователя (админка)", required = true) @RequestParam(value = "sLoginStaff", required = false) String sLoginStaff
    ) throws Exception {
        
        //UserDataVO oUserDataVO = new UserDataVO();
        if(sLoginStaff != null){
            sLogin = sLoginStaff;
        }
        
        return usersService.getFioUserGroupMember(sLogin);
        //oUserDataVO.setsFIO(usersService.getUserFIObyLogin(sLogin));
        //oUserDataVO.setAsGroupsMember(usersService.getUserGroupMember(sLogin));
        
        //return oUserDataVO;
    }
    
    @ApiOperation(value = "Перетереть референтнорсть")
    @RequestMapping(value = "/replaseUserGroupMember", method = RequestMethod.GET)
    @ResponseBody
    public void replaseUserGroupMember(
            @ApiParam(value = "логин пользователя", required = true) @RequestParam(value = "sLoginStaff", required = true) String sLoginStaff,
            @ApiParam(value = "джейсон-перечень групп", required = true) @RequestParam(value = "saGroup", required = true) String saGroup
    ) throws Exception {
        LOG.info("replaseUserGroupMember started...");
        List<Map<String, String>> amUserGroup_Current = usersService.getFioUserGroupMember(sLoginStaff);
        
        for(Map<String, String> mGroup_Old : amUserGroup_Current){
            usersService.removeUserGroup(mGroup_Old.get("sLogin"), sLoginStaff);
        }
        
        LOG.info("amUserGroup_Current is {}", amUserGroup_Current);
        
        JSONArray aJSONGroup_New = new JSONArray(saGroup);
        
        for (int i = 0; i < aJSONGroup_New.length(); i++) {
            JSONObject oJSONGroup_New = aJSONGroup_New.getJSONObject(i);
            String sLogin_New = oJSONGroup_New.getString("sLogin");
            usersService.setUserGroup(sLogin_New, sLoginStaff, false);
        }
    }
    
    @ApiOperation(value = "Получение списка email юзеров по ИД группы", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/action/identity/getUsersEmailByGroup?sID_Group=GrekD \n")
    @RequestMapping(value = "/getUsersEmailByGroup", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getUsersEmailByGroup(@ApiParam(value = "ИД группы", required = true) @RequestParam(value = "sID_Group", required = true) String sID_Group){
    	
    	List<String> usersByGroup = usersService.getUsersEmailByGroup(sID_Group);
		return usersByGroup;
    	
    }
    
     /**
     * Делает пользователя админом
     *
     * @param sLogin    строка текст, логин пользователя, которого необходимо сделать админом
     */
    @ApiOperation(value = "Делает пользователя админом")
    @RequestMapping(value = "/setAdminRole", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public void setAdminRole(            
            @ApiParam(value = "строка текст, логин пользователя, которого необходимо добавить", required = true) @RequestParam(value = "sLogin", required = true) String sLogin)
            throws Exception {
        
        usersService.setAdminRole(sLogin);
        
    }

}