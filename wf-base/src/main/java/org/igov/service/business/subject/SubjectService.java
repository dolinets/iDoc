/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.business.subject;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.igov.model.server.ServerDao;
import org.igov.model.subject.*;
import org.igov.model.subject.organ.SubjectOrganDao;
import org.igov.model.subject.wrapper.SubjectContactWrapper;
import org.igov.service.business.action.task.core.UsersService;
import org.igov.service.business.action.task.form.TaskForm;
import org.igov.service.business.document.DocumentStepService;
import org.igov.service.business.serverEntitySync.ServerEntitySyncService;
import org.igov.service.business.subject.criteria.HierarchyCriteria;
import org.igov.service.business.util.SubjectUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static org.igov.service.business.subject.SubjectGroupTreeService.ALL;

/**
 *
 * @author Belyavtsev Vladimir Vladimirovich (BW)
 */
//@Service
@Component("subjectService")
public class SubjectService {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectService.class);

    @Autowired
    private SubjectDao subjectDao;
    @Autowired
    private SubjectOrganDao subjectOrganDao;
    @Autowired
    private SubjectContactDao subjectContactDao;
    @Autowired
    private SubjectHumanDao subjectHumanDao;
    @Autowired
    private TaskForm oTaskForm;
    @Autowired
    private SubjectContactTypeDao subjectContactTypeDao;
    @Autowired
    private SubjectHumanRoleDao subjectHumanRoleDao;
    @Autowired
    private SubjectAccountDao subjectAccountDao;
    @Autowired
    private DocumentStepService oDocumentStepService;
    @Autowired
    private SubjectGroupTreeService oSubjectGroupTreeService;
    @Autowired
    private SubjectGroupService oSubjectGroupService;
    @Autowired
    private SubjectGroupDao oSubjectGroupDao;
    @Autowired
    private SubjectContactDao oSubjectContactDao;
    @Autowired
    private SubjectGroupTreeService subjectGroupTreeService;
    @Autowired
    private SubjectHumanPositionCustomDao subjectHumanPositionCustomDao;
    @Autowired
    private SubjectGroupDao subjectGroupDao;
    @Autowired
    private SubjectGroupTreeDao subjectGroupTreeDao;
    @Autowired
    private ServerDao serverDao;
    @Autowired
    private SubjectStatusDao subjectStatusDao;
    @Autowired
    private UsersService usersService;
    @Autowired
    SubjectAccountTypeDao oSubjectAccountTypeDao;
    @Autowired
    private ServerEntitySyncService oServerEntitySyncService;
    
    public Subject getSubjectByLoginAccount(String sLogin) {
        Subject result = null;
        Optional<SubjectAccount> subjectAccount = subjectAccountDao.findBy("sLogin", sLogin);
        if (subjectAccount.isPresent()) {
            LOG.info("subjectAccount: " + subjectAccount);
            Long nID_Subject = subjectAccount.get().getnID_Subject();
            LOG.info("nID_Subject: " + nID_Subject);
            if (nID_Subject != null) {
                result = subjectDao.findByIdExpected(nID_Subject);
            }
        }
        return result;
    }

    public Subject syncSubject_Upload(String sID_Subject_Upload) {
        Subject subject_Upload = subjectDao.getSubject(sID_Subject_Upload);
        if (subject_Upload == null) {
            subject_Upload = subjectOrganDao.setSubjectOrgan(sID_Subject_Upload).getoSubject();
        }
        return subject_Upload;
    }

    public List<SubjectContact> syncContactsService(String snID_Subject, String sMail, String sPhone) {
        LOG.info("(Вход в syncContactsService snID_Subject {}, sMail {})", snID_Subject, sMail);
        List<SubjectContact> listContacts = new ArrayList();

        Long nID_Subject = convertStringToLong(snID_Subject);
        LOG.info("(before getSubject nID_Subject {})", nID_Subject);
        Subject subject = null;
        Subject subjectsID_Mail = null;
        Subject subjectsID_Phone = null;
        SubjectHuman oSubjectHuman = null;
        if (nID_Subject != null) {
            subject = this.getSubjectObjectBynID(nID_Subject);
            if (subject != null) {
                oSubjectHuman = getSubjectHuman(subject);
            }
        } else {
            if (sMail != null) {
                String sID = this.getsID(sMail, null);
                subjectsID_Mail = this.getSubjectObject(sID, sMail);
            }
            if (sPhone != null) {
                String sID = this.getsID(null, sPhone);
                subjectsID_Phone = this.getSubjectObject(sID, sPhone);
            }

            if (sMail != null && subjectsID_Mail == null) {
                if (subjectsID_Phone != null && !subjectsID_Phone.getsID().startsWith("_")) {
                    subjectsID_Mail = subjectsID_Phone;
                } else {
                    String sID = this.getsID(sMail, null);
                    subjectsID_Mail = this.createSubject(sID);
                }
            }
            if (sPhone != null && subjectsID_Phone == null) {
                if (subjectsID_Mail != null && !subjectsID_Mail.getsID().startsWith("_")) {
                    subjectsID_Phone = subjectsID_Mail;
                } else {
                    String sID = this.getsID(null, sPhone);
                    subjectsID_Phone = this.createSubject(sID);
                }
            }

        }

        if (subject != null) {
            List<SubjectContact> list_contacts = subjectContactDao.findContacts(subject);
            LOG.info("(получаем список контактов субьекта)");
            if (sMail != null) {
                List<SubjectContact> list_mail = subjectContactDao.findAllBy("sValue", sMail);
                LOG.info("(получаем список контактов по sMail {})", sMail);
                SubjectContactType typeContact = subjectContactTypeDao.getEmailType();
                SubjectContact oSubjectContact = this.synchronizationContacts(list_contacts, list_mail, subject, sMail, typeContact);
                if (oSubjectHuman != null) {
                    oSubjectHuman.setDefaultEmail(oSubjectContact);
                    subjectHumanDao.saveOrUpdateHuman(oSubjectHuman);
                }
                listContacts.add(oSubjectContact);
            }
            if (sPhone != null) {
                List<SubjectContact> list_phone = subjectContactDao.findAllBy("sValue", sPhone);
                LOG.info("(получаем список контактов по sPhone {})", sPhone);
                SubjectContactType typeContact = subjectContactTypeDao.getPhoneType();
                SubjectContact oSubjectContact = this.synchronizationContacts(list_contacts, list_phone, subject, sPhone, typeContact);
                listContacts.add(oSubjectContact);
                if (oSubjectHuman != null) {
                    oSubjectHuman.setDefaultPhone(oSubjectContact);
                    subjectHumanDao.saveOrUpdateHuman(oSubjectHuman);
                }

            }
        } else {

            if (sMail != null) {
                List<SubjectContact> list_contacts = subjectContactDao.findContacts(subjectsID_Mail);
                LOG.info("(получаем список контактов субьекта)");
                List<SubjectContact> list_mail = subjectContactDao.findAllBy("sValue", sMail);
                LOG.info("(получаем список контактов по sMail {})", sMail);
                SubjectContactType typeContact = subjectContactTypeDao.getEmailType();
                SubjectContact oSubjectContact = this.synchronizationContacts(list_contacts, list_mail, subjectsID_Mail, sMail, typeContact);
                listContacts.add(oSubjectContact);

            }
            if (sPhone != null) {
                List<SubjectContact> list_contacts = subjectContactDao.findContacts(subjectsID_Phone);
                LOG.info("(получаем список контактов субьекта)");
                List<SubjectContact> list_phone = subjectContactDao.findAllBy("sValue", sPhone);
                LOG.info("(получаем список контактов по sPhone {})", sPhone);
                SubjectContactType typeContact = subjectContactTypeDao.getPhoneType();
                SubjectContact oSubjectContact = this.synchronizationContacts(list_contacts, list_phone, subjectsID_Phone, sPhone, typeContact);
                listContacts.add(oSubjectContact);

            }
        }

        return listContacts;
    }

    private SubjectContact synchronizationContacts(List<SubjectContact> list_contacts_subject, List<SubjectContact> list_contacts, Subject subject, String sContact, SubjectContactType typeContact) {
        SubjectContact res = null;
        boolean bIsContact = this.isContact(list_contacts_subject, sContact);
        boolean bIsDataBase = this.isContact(list_contacts, sContact);
        if (bIsContact) {
            res = this.updateContact(subject, sContact);
            LOG.info("(апдейтим контакт в списке контактов субьекта)");
        } else {
            if (bIsDataBase) {
                res = this.updateContact(subject, sContact);
                LOG.info("(апдейтим контакт в списке контактов базы, переопределяя субьекта)");
            } else {
                res = this.createSubjectContact(sContact, subject, typeContact);
                LOG.info("(создаем контакт)");

            }
        }

        return res;
    }

    private SubjectHuman getSubjectHuman(Subject subject) {
        return subjectHumanDao.findByExpected("oSubject", subject);
    }

    private SubjectContact createSubjectContact(String sContact, Subject subject, SubjectContactType typeContact) {
        SubjectContact contact = new SubjectContact();
        contact.setSubject(subject);
        contact.setSubjectContactType(typeContact);
        contact.setsDate();
        contact.setsValue(sContact);
        subjectContactDao.saveOrUpdate(contact);
        SubjectContact res = subjectContactDao.findByExpected("sValue", sContact);

        LOG.info("(создаем контакт subject Id {}, subject Label {}, subjectContact sValue {})", subject.getsID(), subject.getsLabel(), contact.getsValue());

        return res;
    }

    private SubjectContact updateContact(Subject subject, String sContact) {

        SubjectContact res = null;
        try {
            SubjectContact contact = subjectContactDao.findByExpected("sValue", sContact);
            contact.setSubject(subject);
            contact.setsDate();
            subjectContactDao.saveOrUpdate(contact);
            res = subjectContactDao.findByIdExpected(contact.getId());

            LOG.info("(апдейт контакта subject Id {}, subject Label {}, subjectContact sValue {})", subject.getsID(), subject.getsLabel(), contact.getsValue());
        } catch (Exception ex) {
            LOG.warn("(Fail update contact {})", ex.getMessage());
        }

        return res;
    }

    private boolean isContact(List<SubjectContact> list, String sContact) {

        for (SubjectContact contact : list) {
            if (contact.getsValue().equals(sContact)) {
                return true;
            }

        }

        return false;
    }

    private Subject getSubjectObject(String sID, String sContact) {

        LOG.info("(sID {})", sID);
        Subject subject = subjectDao.getSubject(sID);

        try {
            List<SubjectContact> listContact = subjectContactDao.findAllBy("sValue", sContact);
            for (SubjectContact oSubjectContact : listContact) {
                if (oSubjectContact.getsValue().equals(sContact)) {
                    Subject subject_time = oSubjectContact.getSubject();
                    if (!subject_time.getsID().startsWith("_")) {
                        subject = subject_time;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            LOG.warn("({})", e.getMessage());
        }

        /* if(subject == null)
            {
               subject = new Subject();
               subject.setsID(sID);
               subjectDao.saveOrUpdateSubject(subject);
               subject = subjectDao.getSubject(sID);
               LOG.info("(Создаем subject Id {}, sID {})", subject.getId(), subject.getsID());
            }*/
        return subject;
    }

    private Subject createSubject(String sID) {
        Subject subject = new Subject();
        subject.setsID(sID);
        subjectDao.saveOrUpdateSubject(subject);
        subject = subjectDao.getSubject(sID);
        LOG.info("(Создаем subject Id {}, sID {})", subject.getId(), subject.getsID());

        return subject;
    }

    private String getsID(String sMail, String sPhone) {
        return (sMail != null) ? SubjectHuman.getSubjectId(SubjectHumanIdType.Email, sMail) : ((sPhone != null) ? SubjectHuman.getSubjectId(SubjectHumanIdType.Phone, sPhone) : null);
    }

    private Subject getSubjectObjectBynID(Long nID_Subject) {
        LOG.info("(subject Id {})", nID_Subject);
        if (subjectDao == null) {
            LOG.info("(subjectDao null)");
        } else {
            LOG.info("(subjectDao not null)");
        }
        return subjectDao.getSubject(nID_Subject);

    }

    private Long convertStringToLong(String snID) {
        Long nID = null;
        try {
            nID = Long.valueOf(snID);
            LOG.info("(convertStringToLong nID {}, snID {})", nID, snID);
        } catch (Exception ex) {
            LOG.warn("(Exception for converting string to long {})", ex.getMessage());
        }

        return nID;
    }

    public String getLoginSubjectAccountByLoginIgovAccount(
            String sID_Login_Activiti, String sID_SubjectAccountType) {
        String result = null;
        try {
            if (sID_Login_Activiti != null) {
                Optional<SubjectAccount> subjectAccount = subjectAccountDao.findBy("sLogin", sID_Login_Activiti);
                if (subjectAccount.isPresent()) {
                    LOG.info("subjectAccount: " + subjectAccount);
                    Long nID_Subject = subjectAccount.get().getnID_Subject();
                    LOG.info("nID_Subject: " + nID_Subject);
                    if (nID_Subject != null) {
                        List<SubjectAccount> aSubjectAccount = subjectAccountDao.findAllBy("nID_Subject", nID_Subject);
                        if (aSubjectAccount.size() > 0) {
                            LOG.info("aSubjectAccount: " + aSubjectAccount);
                            for (SubjectAccount oSubjectAccount : aSubjectAccount) {
                                LOG.info("oSubjectAccount.getSubjectAccountType().getId(): " + oSubjectAccount.getSubjectAccountType().getId());
                                if (oSubjectAccount.getSubjectAccountType().getsID().equals(sID_SubjectAccountType)) {
                                    result = oSubjectAccount.getsLogin();
                                    LOG.info("result: " + result);
                                    break;
                                } else {
                                    LOG.error("Can't find 1C account");
                                }
                            }
                        } else {
                            LOG.error("Can't find SubjectAccount by Subject");
                        }
                    } else {
                        LOG.error("Subject is null ");
                    }
                } else {
                    LOG.error("Can't find SubjectAccount by Login");
                }
            } else {
                LOG.error("Can't find assigneeUser");
            }
        } catch (Exception ex) {
            LOG.error("getLoginSubjectAccountByLoginIgovAccount: ", ex);
        }
        return result;
    }

    /**
     * contacts of whole hierarchy
     */
    public List<String> getSubjectContacts(String sID_Group_Activiti, String sSubjectType, String sSubjectContactType, Integer nLevel) {
        if (Strings.isNullOrEmpty(sSubjectType)) {
            sSubjectType = ALL;
        }
        if (nLevel == null) {
            nLevel = HierarchyCriteria.TREE;
        }
    
        SubjectContactType oSubjectContactType = subjectContactTypeDao.findByExpected("sName_EN", sSubjectContactType);
        
        HierarchyCriteria oCriteria = new HierarchyCriteria()
                .setsRoot(sID_Group_Activiti)
                .setbIncludeRoot(true)
                .setsType(sSubjectType)
                .setnDirection(HierarchyCriteria.BOTTOM)
                .setnLevel(nLevel);
        
        List<Long> anSubject = subjectGroupTreeService
                .getHierarchy(oCriteria)
                .stream()
                .map(SubjectGroup::getoSubject)
                .map(Subject::getId)
                .distinct()
                .collect(Collectors.toList());
        
        return getSubjectContactValues(anSubject, oSubjectContactType.getId());
    }
    
    /**
     * Получение контактов. По sID_Field и snID_Process_Activiti вытаскивааем
     * все логины, для каждого логина получаем дерево, для всего дерева ищем
     * контакты.
     *
     * @param snID_Process_Activiti ид процесса
     * @param sID_Field ид поля
     * @param sID_FieldTable ид поля в таблице
     * @param sSubjectType тип SubjectGroup
     * @param sSubjectContactType тип контакта, который нужно получить
     * @return лист контактов заданного типа
     * @throws Exception
     */
    public List<String> getSubjectContacts(String snID_Process_Activiti, String sID_Field, String sID_FieldTable,
            String sSubjectType, String sSubjectContactType) throws Exception {
        //Login = sID_Group_Activiti
        List<String> asLogin = oTaskForm.getValuesFromTableField(snID_Process_Activiti, sID_Field, sID_FieldTable);
        LOG.info("getSubjectContacts: asLogin={}", asLogin);

        SubjectContactType oSubjectContactType = subjectContactTypeDao.findByExpected("sName_EN", sSubjectContactType);
        
        Set<SubjectGroup> aoAllSubjectGroup = new HashSet<>();
        boolean bNeedToFindHumanAndOrgan = false;

        for (String sID_Group_Activiti : asLogin) {

            if (sSubjectType == null) {
                sSubjectType = "Human";
                bNeedToFindHumanAndOrgan = true;
            }
            //находим SubjectGroup рутового элемент
            SubjectGroup oSubjectGroupRoot = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sID_Group_Activiti);
            String sSubjectGroupRootType = oSubjectGroupService.getSubjectType(sID_Group_Activiti);
            LOG.info("oSubjectGroupRoot={} type={}", oSubjectGroupRoot, sSubjectGroupRootType);
            //рут кладем, если совпал тип или на вход получили sSubjectType == null
            if (sSubjectGroupRootType.equals(sSubjectType) || bNeedToFindHumanAndOrgan) {
                aoAllSubjectGroup.add(oSubjectGroupRoot);
            }
            //находим всех детей
            aoAllSubjectGroup.addAll(oSubjectGroupTreeService
                    .getCatalogSubjectGroupsTree(sID_Group_Activiti, 0l, null, false, 0l, sSubjectType)
                    .getaSubjectGroupTree());
            if (bNeedToFindHumanAndOrgan) {
            //находим всех детей для другого типа
            aoAllSubjectGroup.addAll(oSubjectGroupTreeService
                    .getCatalogSubjectGroupsTree(sID_Group_Activiti, 0l, null, false, 0l, "Organ")
                    .getaSubjectGroupTree());
            }
            LOG.info("aoAllSubjectGroup={}", aoAllSubjectGroup);
        }
        
        List<Long> anSubject = aoAllSubjectGroup
                .stream()
                .map(SubjectGroup::getoSubject)
                .map(Subject::getId)
                .distinct()
                .collect(Collectors.toList());

        return getSubjectContactValues(anSubject, oSubjectContactType.getId());
    }

    public List<String> getSubjectContactValues(List<Long> anSubject, Long nID_SubjectContactType) {
        return oSubjectContactDao
                       .findContactsByInSubjectAndContactType(anSubject, nID_SubjectContactType)
                       .stream()
                       .map(SubjectContact::getsValue)
                       .filter(s -> !s.isEmpty())
                       .distinct()
                       .collect(Collectors.toList());
    }
    
    public String getEmailByLogin(String sID_Group_Activiti){
        SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sID_Group_Activiti);
        List<SubjectContact> aoSubjectContact = oSubjectContactDao
                    .findContactsBySubjectAndContactType(oSubjectGroup.getoSubject(), 1L);
        if (aoSubjectContact != null && !aoSubjectContact.isEmpty()) {
            return aoSubjectContact.get(0).getsValue();
        }
        return "";
    }

    public List<String> getPhonesByLogin(String sID_Group_Activiti) {
        SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sID_Group_Activiti);
        Long nPhoneType = subjectContactTypeDao.getPhoneType().getId();

        List<String> aoSubjectContact = oSubjectContactDao
                .findContactsBySubjectAndContactType(oSubjectGroup.getoSubject(), nPhoneType)
                .stream()
                .map(SubjectContact::getsValue)
                .collect(Collectors.toList());
        if (aoSubjectContact.isEmpty()) {
            aoSubjectContact.add("");
        }
        return aoSubjectContact;
    }

    public List<SubjectContact> getaSubjectContact(String sID_Group_Activiti) {
        SubjectGroup oSubjectGroup = subjectGroupDao.findByExpected("sID_Group_Activiti", sID_Group_Activiti);
        return subjectContactDao.findContacts(oSubjectGroup.getoSubject());
    }

    public SubjectGroupTree setParentDepart(SubjectGroup oSubjectGroup, SubjectGroup oSubjectGroupParent, String type) {
        oSubjectGroup.setsChain(oSubjectGroupParent.getsChain());
        oSubjectGroup.setsSubjectGroup_Company(oSubjectGroupParent.getsSubjectGroup_Company());
        subjectGroupDao.saveOrUpdate(oSubjectGroup);
        SubjectGroupTree oSubjectGroupTreeSaved = subjectGroupTreeService
                .setSubjectGroupParent(oSubjectGroup, oSubjectGroupParent, type);
        if (oSubjectGroupTreeSaved != null) {
            SubjectUtils.checkID(oSubjectGroupTreeSaved);
            LOG.debug("set SubjectGroupTree, childId: {}, parentId: {}", oSubjectGroup.getId(), oSubjectGroupParent.getId());
        }
        return oSubjectGroupTreeSaved;
    }

    public Subject getOrCreateSubject(SubjectGroup oSubjectGroup) {
        Subject oSubject = oSubjectGroup.getoSubject();
        if (oSubject == null) {
            oSubject = new Subject();
        }
        return oSubject;
    }

    public SubjectContact getOrCreateSubjectEmailContact(Subject oSubject) {
        SubjectContact oSubjectEmailContact = subjectContactDao.findContacts(oSubject).stream()
                .filter(subjectContact -> subjectContact.getSubjectContactType()
                        .equals(subjectContactTypeDao.getEmailType())).findFirst().orElse(new SubjectContact());
        oSubjectEmailContact.setSubject(oSubject);
        oSubjectEmailContact.setSubjectContactType(subjectContactTypeDao.getEmailType());
        return oSubjectEmailContact;
    }

    public void setSubjectContacts(String sID_Group_Activiti, List<SubjectContactWrapper> aoSubjectContactWrapper, Boolean isSync) {
        SubjectGroup oSubjectGroup = subjectGroupDao.findByExpected("sID_Group_Activiti", sID_Group_Activiti);
        Subject oSubject = oSubjectGroup.getoSubject();
        Set<SubjectContact> aoSubjectContact = subjectContactDao.findContacts(oSubject).stream().collect(Collectors.toSet());
        List<SubjectContact> aoSubjectContactTarget = aoSubjectContactWrapper.stream()
            .map(this::createSubjectContact)
            .filter(Objects::nonNull)
            .filter(c -> !aoSubjectContact.contains(c))
            .collect(Collectors.toList());
        aoSubjectContactTarget.forEach(c -> c.setSubject(oSubject));
        List<SubjectContact> aoSubjectContactSaved = oSubjectContactDao.saveOrUpdate(aoSubjectContactTarget);
        aoSubjectContactSaved.forEach(SubjectUtils::checkID);
        
        for(SubjectContact oSubjectContact : aoSubjectContactTarget){
            LOG.info("oSubjectContact to add is {}", oSubjectContact.getId());
        }
        
        String sKey_Row = buildServerEntitySync_ContactBody(sID_Group_Activiti, aoSubjectContactWrapper);
        
        if(isSync == null || isSync == false){
            oServerEntitySyncService.addRecordToServerEntitySync(sKey_Row, oServerEntitySyncService.INSERT_ACTION, "SubjectContact");
              
            new Thread(new Runnable() {
                public void run() {
                    oServerEntitySyncService.runServerEntitySync("SubjectContact", sKey_Row);
                }
            }).start();
        }
    }

    private SubjectContact createSubjectContact(SubjectContactWrapper subjectContactWrapper) {
        Optional<SubjectContactType> oSubjectContactTypeWrapper =
                subjectContactTypeDao.findByName(subjectContactWrapper.getsType());
        if (oSubjectContactTypeWrapper.isPresent()) {
            SubjectContact oSubjectContact = new SubjectContact();
            oSubjectContact.setSubjectContactType(oSubjectContactTypeWrapper.get());
            oSubjectContact.setsValue(subjectContactWrapper.getsValue());
            oSubjectContact.setsDate();
            return oSubjectContact;
        } else {
            throw new RuntimeException("SubjectContactType = '" + subjectContactWrapper.getsType() + "' not exist");
        }
    }
    
    public List<String> deleteSubjectContacts(String sID_Group_Activiti, List<SubjectContactWrapper> aoSubjectContactWrapper, Boolean isSync) {
        List<String> aoSubjectContactDeleted = new ArrayList<>();

        SubjectGroup oSubjectGroup = subjectGroupDao.findByExpected("sID_Group_Activiti", sID_Group_Activiti);
        Subject oSubject = oSubjectGroup.getoSubject();

        List<SubjectContact> aoSubjectContact = subjectContactDao.findContacts(oSubject);
        if (aoSubjectContact.size() > 0) {
            Set<String> aoType = aoSubjectContactWrapper.stream().map(SubjectContactWrapper::getsType).collect(Collectors.toSet());
            Set<String> aoValue = aoSubjectContactWrapper.stream().map(SubjectContactWrapper::getsValue).collect(Collectors.toSet());

            List<SubjectContact> aoSubjectContactTarget = aoSubjectContact.stream()
                    .filter(contact -> aoType.contains(contact.getSubjectContactType().getsName_EN())
                            || aoType.contains(contact.getSubjectContactType().getsName_UA())
                            || aoType.contains(contact.getSubjectContactType().getsName_RU()))
                    .filter(contact -> aoValue.contains(contact.getsValue()))
                    .collect(Collectors.toList());

            if (aoSubjectContactTarget.size() > 0) {
                SubjectHuman oSubjectHuman = subjectHumanDao.getSubjectHuman(oSubject);
                if (oSubjectHuman != null) {
                    SubjectContact oSubjectHumanEmail = oSubjectHuman.getDefaultEmail();
                    SubjectContact oSubjectHumanPhone = oSubjectHuman.getDefaultPhone();
                    if (oSubjectHumanEmail != null && oSubjectHumanPhone != null) {
                        SubjectContactType oSubjectContactTypeEmail = oSubjectHumanEmail.getSubjectContactType();
                        SubjectContactType oSubjectContactTypePhone = oSubjectHumanPhone.getSubjectContactType();

                        boolean primaryEmail = aoSubjectContactTarget.contains(oSubjectHumanEmail);

                        if (primaryEmail) {
                            java.util.Optional<SubjectContact> first = aoSubjectContact.stream()
                                    .filter(c -> c.getSubjectContactType().equals(oSubjectContactTypeEmail))
                                    .filter(c -> !c.getsValue().equals(oSubjectHumanEmail.getsValue()))
                                    .filter(c -> !aoSubjectContactTarget.contains(c))
                                    .findFirst();
                            if (!first.isPresent()) {
                                throw new RuntimeException("Can't delete primary email, value: " + oSubjectHumanEmail.getsValue());
                            }
                            oSubjectHuman.setDefaultEmail(first.get());
                            LOG.warn("primary email auto change");
                        }
                        boolean primaryPhone = aoSubjectContactTarget.contains(oSubjectHumanPhone);
                        if (primaryPhone) {
                            java.util.Optional<SubjectContact> first = aoSubjectContact.stream()
                                    .filter(c -> c.getSubjectContactType().equals(oSubjectContactTypePhone))
                                    .filter(c -> !c.getsValue().equals(oSubjectHumanPhone.getsValue()))
                                    .filter(c -> !aoSubjectContactTarget.contains(c))
                                    .findFirst();
                            if (!first.isPresent()) {
                                throw new RuntimeException("Can't delete primary phone, value: " + oSubjectHumanPhone.getsValue());
                            }
                            oSubjectHuman.setDefaultPhone(first.get());
                            LOG.warn("primary phone auto change");
                        }
                    }
                }
                
                for(SubjectContact oSubjectContact : aoSubjectContactTarget){
                    LOG.info("oSubjectContact to delete is {}", oSubjectContact.getId());
                }
                
                Set<String> aoSubjectContactNotDeleted = subjectContactDao.delete(aoSubjectContactTarget).stream()
                        .map(SubjectContact::getsValue)
                        .collect(Collectors.toSet());
                aoSubjectContactDeleted = aoSubjectContactTarget.stream()
                        .map(SubjectContact::getsValue)
                        .filter(c -> !aoSubjectContactNotDeleted.contains(c))
                        .collect(Collectors.toList());
            }
        }        
        String sKey_Row = buildServerEntitySync_ContactBody(sID_Group_Activiti, aoSubjectContactWrapper);
        
        if(isSync == null || isSync == false){
            oServerEntitySyncService.addRecordToServerEntitySync(sKey_Row,  oServerEntitySyncService.REMOVE_ACTION, "SubjectContact");
              
            new Thread(new Runnable() {
                public void run() {
                    oServerEntitySyncService.runServerEntitySync("SubjectContact", sKey_Row);
                }
            }).start();
        }
        
        return aoSubjectContactDeleted;
    }

    private String buildServerEntitySync_ContactBody(String sID_Group_Activiti, List<SubjectContactWrapper> aoSubjectContactWrapper){
        
        JSONArray oJSONArray = new JSONArray();
        JSONObject oJSONObject_Res = new JSONObject();
        for(SubjectContactWrapper oSubjectContactWrapper : aoSubjectContactWrapper){
            JSONObject oJSONObject = new JSONObject();
            oJSONObject.put("sType", oSubjectContactWrapper.getsType());
            oJSONObject.put("sValue", oSubjectContactWrapper.getsValue());
            oJSONArray.add(oJSONObject);
        }
            
        LOG.info("oJSONArray is {}", oJSONArray.toJSONString());
        oJSONObject_Res.put("oArray", oJSONArray.toJSONString());
        oJSONObject_Res.put("sLogin", sID_Group_Activiti);
        LOG.info("oJSONObject_Res is {}", oJSONObject_Res.toJSONString());
        
        return oJSONObject_Res.toJSONString();
    }
    
}
