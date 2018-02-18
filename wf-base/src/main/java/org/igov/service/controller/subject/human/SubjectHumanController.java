package org.igov.service.controller.subject.human;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.identity.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.mail.EmailException;
import org.igov.io.GeneralConfig;
import org.igov.io.mail.Mail;
import org.igov.model.action.event.HistoryEvent;
import org.igov.model.action.event.HistoryEventDao;
import org.igov.model.document.Document;
import org.igov.model.document.DocumentDao;
import org.igov.model.relation.ObjectGroup;
import org.igov.model.relation.ObjectGroupDao;
import org.igov.model.relation.ObjectItem;
import org.igov.model.relation.ObjectItemDao;
import org.igov.model.server.ServerDao;
import org.igov.model.subject.*;
import org.igov.model.subject.message.SubjectMessage;
import org.igov.model.subject.message.SubjectMessagesDao;
import org.igov.model.subject.organ.SubjectOrgan;
import org.igov.model.subject.organ.SubjectOrganDao;
import org.igov.model.subject.vo.SubjectHumanVO;
import org.igov.model.subject.vo.SubjectHumanVO_Compact;
import org.igov.service.business.subject.SubjectHumanService;
import org.igov.service.controller.ExceptionCommonController;
import org.igov.service.exception.CommonServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Controller
@Api(description = "Управление сотрудниками", tags = {"SubjectHuman", "SubjectGroup", "Subject", "SubjectContact"})
@RequestMapping(value = "/subject")
public class SubjectHumanController {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectHumanController.class);

    @Autowired
    private SubjectHumanService oSubjectHumanService;

    @Autowired private SubjectHumanDao oSubjectHumanDao;
    @Autowired private SubjectGroupDao oSubjectGroupDao;
    @Autowired private SubjectGroupTreeDao oSubjectGroupTreeDao;
    @Autowired private SubjectDao oSubjectDao;
    @Autowired private SubjectContactDao oSubjectContactDao;
    @Autowired private SubjectContactTypeDao oSubjectContactTypeDao;
    @Autowired private ServerDao oServerDao;
    //@Autowired private SubjectMessageDao oSubjectMessageDao;
    @Autowired private SubjectMessagesDao oSubjectMessagesDao;
    @Autowired private SubjectOrganDao oSubjectOrganDao;
    @Autowired private HistoryEventDao oHistoryEventDao;
    @Autowired private DocumentDao oDocumentDao;
    @Autowired private ObjectItemDao oObjectItemDao;
    @Autowired private ObjectGroupDao oObjectGroupDao;
    //@Autowired private ServiceDataDao oServiceData;
    @Autowired private SubjectHumanRoleDao oSubjectHumanRoleDao;
    
    
    
    
    
    
    
    @Autowired
    public GeneralConfig generalConfig;
    @Autowired
    private ApplicationContext context;
    @Value("${general.Mail.sHost}")
    public String mailServerHost;
    @Value("${general.Mail.nPort}")
    public String mailServerPort;
    @Value("${general.Mail.sAddressDefaultFrom}")
    public String mailServerDefaultFrom;
    @Value("${general.Mail.sUsername}")
    public String mailServerUsername;
    @Value("${general.Mail.sPassword}")
    public String mailServerPassword;
    @Value("${general.Mail.sAddressNoreply}")
    public String mailAddressNoreplay;
    @Value("${general.Mail.bUseSSL}")
    private String sbSSL;
    @Value("${general.Mail.bUseTLS}")
    private String sbTLS;    
    
    
    @ApiOperation(value = "Создание данных о сотруднике", httpMethod = "POST", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/setSubjectHuman?sFamily=%D0%A4&sName=%D0%98&sSurname=%D0%9E&sLogin=test_human&sDateBirth=20.10.1990&sPhone=103&sEmail=test_human_email@test.com&sLogin=test_human&sPassword=test_pwd&sPosition=Programmer&sID_Group_Activiti_Organ=MJU_Dnipro_7")
    @ApiResponse(code = 200, message = "{\n"
            + "  \"sID_Group_Activiti\": \"test_human\",\n"
            + "  \"sChain\": \"MJU_Dnipro_\",\n"
            + "  \"aUser\": null,\n"
            + "  \"oSubject\": {\n"
            + "    \"sID\": null,\n"
            + "    \"sLabel\": \"Ф И О\",\n"
            + "    \"sLabelShort\": null,\n"
            + "    \"oSubjectStatus\": {\n"
            + "      \"sName\": \"Working\",\n"
            + "      \"sNote\": \"Работает\",\n"
            + "      \"nID\": 1\n"
            + "    },\n"
            + "    \"aSubjectAccountContact\": [],\n"
            + "    \"nID\": 1603769\n"
            + "  },\n"
            + "  \"oSubjectHumanPositionCustom\": {\n"
            + "    \"sNote\": \"Программист\",\n"
            + "    \"nID\": 1,\n"
            + "    \"sName\": \"Programmer\"\n"
            + "  },\n"
            + "  \"nID\": 1603773,\n"
            + "  \"sName\": \"Ф И О\",\n"
            + "  \"aSubjectGroupChilds\": null,\n"
            + "  \"sName_SubjectGroupCompany\": null\n"
            + "}"
    )
    @RequestMapping(value = "/setSubjectHuman", method = RequestMethod.POST)
    @ResponseBody
    public SubjectGroup createSubjectHuman(
            @ApiParam(required = true, value = "Фамилия") @RequestParam String sFamily,
            @ApiParam(required = true, value = "Имя") @RequestParam String sName,
            @ApiParam(required = true, value = "Отчество") @RequestParam String sSurname,
            @ApiParam(required = true, value = "Логин") @RequestParam String sLoginStaff,
            @ApiParam(required = true, value = "Пароль") @RequestParam String sPassword,
            @ApiParam(required = true, value = "Контактная почта") @RequestParam String sEmail,
            @ApiParam(required = true, value = "Контактный телефон") @RequestParam String sPhone,
            @ApiParam("Вызывается ли сервис через механизм синхронизации") @RequestParam(required = false) Boolean isSync,
            @ApiParam(required = true, value = "Должность") @RequestParam String sPosition,
            @ApiParam(required = true, value = "Строковый ID отдела, в котором состоит сотрудник") @RequestParam String sID_Group_Activiti_Organ,
            @ApiParam("Дата рожден`ия") @RequestParam(required = false) String sDateBirth,
            @ApiParam(value = "Флаг назначения в руководители отдела", defaultValue = "false") @RequestParam(required = false, defaultValue = "false") Boolean isHead,
            @ApiParam("Порядковый ID сервера, с которого осуществляется вход в систему") @RequestParam(required = false) Long nID_Server,
            @ApiParam(value = "sLogin", required = false) @RequestParam(required = false) String sLogin,
            @ApiParam(value = "sLoginReferent", required = false) @RequestParam(required = false) String sLoginReferent) 
    {
        return oSubjectHumanService.createSubjectHuman(sLoginStaff, sPassword, sFamily, sName, sSurname, sPosition, null, sDateBirth, sEmail, sPhone,
                sID_Group_Activiti_Organ, isHead, nID_Server, isSync, sLogin, sLoginReferent);
    }

    @ApiOperation(value = "Обновление данных о сотруднике", httpMethod = "GET", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/setSubjectHuman?sFamily=%D0%A4&sName=%D0%98&sSurname=%D0%9E&sLogin=test_human&sDateBirth=20.10.1990&sPhone=103&sEmail=test_human_email@test.com&sID_Group_Activiti=test_human&sPosition=Programmer&sID_Group_Activiti_Organ=MJU_Dnipro_7")
    @ApiResponse(code = 200, message = "{\n"
            + "  \"sID_Group_Activiti\": \"test_human\",\n"
            + "  \"sChain\": \"MJU_Dnipro_\",\n"
            + "  \"aUser\": null,\n"
            + "  \"oSubject\": {\n"
            + "    \"sID\": null,\n"
            + "    \"sLabel\": \"Ф И О\",\n"
            + "    \"sLabelShort\": null,\n"
            + "    \"oSubjectStatus\": {\n"
            + "      \"sName\": \"Working\",\n"
            + "      \"sNote\": \"Работает\",\n"
            + "      \"nID\": 1\n"
            + "    },\n"
            + "    \"aSubjectAccountContact\": [],\n"
            + "    \"nID\": 1603769\n"
            + "  },\n"
            + "  \"oSubjectHumanPositionCustom\": {\n"
            + "    \"sNote\": \"Программист\",\n"
            + "    \"nID\": 1,\n"
            + "    \"sName\": \"Programmer\"\n"
            + "  },\n"
            + "  \"nID\": 1603773,\n"
            + "  \"sName\": \"Ф И О\",\n"
            + "  \"aSubjectGroupChilds\": null,\n"
            + "  \"sName_SubjectGroupCompany\": null\n"
            + "}"
    )
    @RequestMapping(value = "/setSubjectHuman", method = RequestMethod.GET)
    @ResponseBody
    public SubjectGroup updateSubjectHuman(
            @ApiParam(required = true, value = "Логин") @RequestParam String sLogin,
            @ApiParam(required = true, value = "Логин редактируемого сотрудника") @RequestParam String sLoginStaff,
            @ApiParam(required = true, value = "Фамилия") @RequestParam String sFamily,
            @ApiParam(required = true, value = "Имя") @RequestParam String sName,
            @ApiParam(required = true, value = "Отчество") @RequestParam String sSurname,
            @ApiParam(required = true, value = "Строковый ID отдела") @RequestParam String sID_Group_Activiti_Organ,
            @ApiParam("Статус") @RequestParam(required = false) String sStatus,
            @ApiParam("Дата рождения") @RequestParam(required = false) String sDateBirth,
            @ApiParam("Должность") @RequestParam(required = false) String sPosition,
            @ApiParam("Контактная почта") @RequestParam(required = false) String sEmail,
            @ApiParam("Контактный телефон") @RequestParam(required = false) String sPhone,
            @ApiParam("Вызывается ли сервис через механизм синхронизации") @RequestParam(required = false) Boolean isSync,
            @ApiParam("Флаг назначения в руководители отдела") @RequestParam(required = false) Boolean isHead) {
        if (sLoginStaff != null) {
            sLogin = sLoginStaff;
        }
        return oSubjectHumanService.updateSubjectHuman(sLogin, null, sFamily, sName, sSurname, sPosition, sStatus, sDateBirth, sEmail, sPhone,
                sID_Group_Activiti_Organ, isHead, null, isSync);
    }

    @ApiOperation(value = "Получение данных о сотруднике", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/getSubjectHuman?sID_Group_Activiti=MJU_Dnipro_Top7_Dep1"
    )
    @ApiResponse(code = 200, message = "{\n"
            + "  \"oSubjectGroup\": {\n"
            + "    \"sID_Group_Activiti\": \"MJU_Dnipro_Top7_Dep1\",\n"
            + "    \"sChain\": \"MJU_Dnipro_\",\n"
            + "    \"aUser\": null,\n"
            + "    \"oSubject\": {},\n"
            + "    \"oSubjectHumanPositionCustom\": {},\n"
            + "    \"nID\": 253,\n"
            + "    \"sName\": \"БАБІЄНКО Тамара Олексіївна\",\n"
            + "    \"aSubjectGroupChilds\": [],\n"
            + "    \"sName_SubjectGroupCompany\": null\n"
            + "  },\n"
            + "  \"oSubjectGroupHead\": {},\n"
            + "  \"aSubjectGroupTreeUp\": [],\n"
            + "  \"mUserGroupMember\": {},\n"
            + "  \"oSubjectHuman\": {},\n"
            + "  \"sLogin\": null,\n"
            + "  \"bHead\": true\n"
            + "}"
    )
    @RequestMapping("/getSubjectHuman")
    @ResponseBody
    public SubjectHumanVO getSubjectHuman(
            @ApiParam(required = true, value = "Строковый ID сотрудника") @RequestParam String sID_Group_Activiti,
            @ApiParam("Включительно дети") @RequestParam(required = false) Boolean  bIncludeSubjectGroupChilds) {
        return oSubjectHumanService.getSubjectHumanVO(sID_Group_Activiti, bIncludeSubjectGroupChilds);
    }
    
    @RequestMapping("/getSubjectHuman_Compact")
    @ResponseBody
    public SubjectHumanVO_Compact getSubjectHuman_Compact(
            @ApiParam(required = true, value = "Строковый ID сотрудника") @RequestParam String sID_Group_Activiti
    ) {
        return oSubjectHumanService.getSubjectHumanVO_Compact(sID_Group_Activiti);
    }
    
    
    //https://idoc-develop.atlassian.net/browse/PLATFORM-406
    @RequestMapping(value = "/sendPasswordOfUser", method = RequestMethod.GET)
    @ResponseBody
    public String sendPasswordOfUser(
            @ApiParam(required = true, value = "Логин пользователя") @RequestParam(required = true) String sUserLogin,
            @ApiParam(required = true, value = "Электронный адрес пользователя") @RequestParam(required = true) String sUserMail
    ) throws CommonServiceException, EmailException {
        LOG.info("(sUserLogin={},sUserMail={})", sUserLogin, sUserMail);
        String sMessage=null;
        String sPassword=null;
        Subject oSubject=null;
        SubjectHuman oSubjectHuman=null;
        List<SubjectContact> aSubjectContact = oSubjectContactDao.findContactsByValueAndContactType(sUserMail, Long.valueOf("1"));
        if(aSubjectContact!=null && !aSubjectContact.isEmpty()){
            //SubjectContact oSubjectContact = aSubjectContact.get(0);
            for(SubjectContact oSubjectContact : aSubjectContact){
                if(oSubjectContact==null){
                    LOG.warn(sMessage="oSubjectContact for sUserMail="+sUserMail+" not found");
                    /*throw new CommonServiceException(
                            ExceptionCommonController.BUSINESS_ERROR_CODE,
                            "oSubjectContact for sUserMail="+sUserMail+" not found",
                            HttpStatus.FORBIDDEN
                    );*/
                }else{
                    oSubject = oSubjectContact.getSubject();
                    if(oSubject==null){
                        LOG.warn(sMessage="oSubject for sUserMail="+sUserMail+" not found");
                        /*throw new CommonServiceException(
                                ExceptionCommonController.BUSINESS_ERROR_CODE,
                                "oSubject for sUserMail="+sUserMail+" not found",
                                HttpStatus.FORBIDDEN
                        );*/
                    }else{
                        oSubjectHuman = oSubjectHumanDao.getSubjectHuman(oSubject);
                        if(oSubjectHuman==null){
                            LOG.warn(sMessage="oSubjectHuman for sUserMail="+sUserMail+" not found");
                            /*throw new CommonServiceException(
                                    ExceptionCommonController.BUSINESS_ERROR_CODE,
                                    sMessage,
                                    HttpStatus.FORBIDDEN
                            );*/
                        }else{
                            Long nID_SubjectHuman=oSubjectHuman.getId();
                            if(nID_SubjectHuman==null){
                                LOG.warn(sMessage="nID_SubjectHuman=null for sUserMail="+sUserMail);
                                /*throw new CommonServiceException(
                                        ExceptionCommonController.BUSINESS_ERROR_CODE,
                                        sMessage,
                                        HttpStatus.FORBIDDEN
                                );*/
                            }else{
                                Long nID_Subject = oSubject.getId();
                                SubjectGroup oSubjectGroup = oSubjectGroupDao.findSubjectById(nID_Subject);
                                if(oSubjectGroup==null){
                                    LOG.warn(sMessage="oSubjectGroup==null nID_SubjectHuman="+nID_SubjectHuman);
                                    /*throw new CommonServiceException(
                                            ExceptionCommonController.BUSINESS_ERROR_CODE,
                                            sMessage,
                                            HttpStatus.FORBIDDEN
                                    );*/
                                }else{
                                    String sLogin=oSubjectGroup.getsID_Group_Activiti();
                                    LOG.info("Find User by sLogin={} and compare with sUserLogin={}", sLogin, sUserLogin);
                                    if(sPassword!=null){
                                        LOG.info(sMessage="sPassword!=null for sUserMail="+sUserMail+" and sLogin="+sLogin);
                                    }else if(sLogin!=null&&!"".equals(sLogin)){
                                        if(!sUserLogin.equalsIgnoreCase(sLogin)){
                                            LOG.warn(sMessage="sUserLogin="+sUserLogin+" is alien to sUserMail="+sUserMail);
                                            /*throw new CommonServiceException(
                                                    ExceptionCommonController.BUSINESS_ERROR_CODE,
                                                    sMessage,
                                                    HttpStatus.FORBIDDEN
                                            );*/
                                        }else{
                                            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
                                            IdentityService oIdentityService = processEngine.getIdentityService();
                                            User oUser = null;
                                            oUser = oIdentityService.createUserQuery().userId(sLogin).singleResult();
                                            if (oUser == null) {
                                                LOG.warn(sMessage="Account not found for Login "+sLogin+",nID_SubjectHuman="+nID_SubjectHuman);
                                                throw new CommonServiceException(
                                                        ExceptionCommonController.BUSINESS_ERROR_CODE,
                                                        sMessage,
                                                        HttpStatus.FORBIDDEN
                                                );
                                            }
                                            sPassword = oUser.getPassword();
                                            if(sPassword==null){
                                                LOG.warn(sMessage="sPassword==null for nID_SubjectHuman="+nID_SubjectHuman);
                                                throw new CommonServiceException(
                                                        ExceptionCommonController.BUSINESS_ERROR_CODE,
                                                        sMessage,
                                                        HttpStatus.FORBIDDEN
                                                );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }else{
            LOG.warn(sMessage="aSubjectContact for sUserMail="+sUserMail+" not found");
            throw new CommonServiceException(
                    ExceptionCommonController.BUSINESS_ERROR_CODE,
                    sMessage,
                    HttpStatus.FORBIDDEN
            );
        }
        
        if(sPassword==null){
            LOG.warn(sMessage="sPassword not found for sUserMail="+sUserMail+" and sUserLogin="+sUserLogin+"\n<br>"+sMessage);
            throw new CommonServiceException(
                    ExceptionCommonController.BUSINESS_ERROR_CODE,
                    sMessage,
                    HttpStatus.FORBIDDEN
            );
        }else{
            String sBody="";
            StringBuilder osBody = new StringBuilder();
            osBody.append("sUserLogin:").append(sUserLogin).append(", sUserMail:").append(sUserMail).append("\n<br>");
            osBody.append("sHost=").append(generalConfig.getSelfHost()).append(" (nID_Server=").append(generalConfig.getSelfServerId()).append(")\n<br>");
            sBody=osBody.toString();
            String sHead = "Пароль для входа в систему iDoc на логин " + sUserLogin + " сервера " + generalConfig.getSelfServerId();
            try {
                    System.setProperty("mail.mime.address.strict", "false");
                    LOG.info("sUserLogin={},sHead={}", sUserLogin, sHead);
                    Multipart oMultiparts = new MimeMultipart();
                    Mail oMail = context.getBean(Mail.class);
                    oMail._To(sUserMail)._Head(sHead)._Body(sBody+"\n<br>Password: "+sPassword)
                            ._oMultiparts(oMultiparts);
                    oMail.send();
                    LOG.info("Sent password to User ok! (sUserMail={})", sUserMail);
                return "Sent password to User:\n "+sBody;
            } catch (RuntimeException e) {
                LOG.warn(sMessage="Cant send password to User(sUserMail="+sUserMail+"): " + e.getMessage());
                throw new CommonServiceException(
                        ExceptionCommonController.BUSINESS_ERROR_CODE,
                        sMessage,
                        HttpStatus.FORBIDDEN
                );
            }        
        }
    }
    
    static String sa="";

    //https://idoc-develop.atlassian.net/browse/PLATFORM-636
    @ApiOperation(value = "Поиск и убирание всех дублей субьектов(и отсязывание от других сущностей) с единым логином в сущности SubjectGroup (и перепривязка SubjectGroupTree)", httpMethod = "GET", notes = "##### Пример:\n"
            + "/wf/service/subject/mergeSubjectDoublesAll")
    @RequestMapping(value = "/mergeSubjectDoublesAll", method = RequestMethod.GET)
    @ResponseBody
    public String mergeSubjectDoublesAll_Service(
            @ApiParam(required = false, value = "Электронные эдреса администраторов (через запятую)") @RequestParam(required = false)  String saAdminMail,
            @ApiParam(required = false, value = "Отработать не только основной список но и проанализировать мерж!", defaultValue = "false") @RequestParam(required = false, defaultValue = "false") Boolean bMerge,
            @ApiParam(required = false, value = "Применить изменения в базе по ВСЕМУ найденному! (боевой режим, иначи только симуляция)", defaultValue = "false") @RequestParam(required = false, defaultValue = "false") Boolean bProdAll
    ) throws CommonServiceException {
        saAdminMail=saUserMailWithAdmin(saAdminMail);
        String sMessage="";
        String sMessages="";
        Integer nLogin=0;
        //Long nID_Subject=null;
        List<String> asLogin = oSubjectGroupDao.findLoginDubles();
        List<Long> anID_Subject_Max = new LinkedList();
        
        LOG.info(sMessage="All SubjectGroups Begin! (bProd="+bProdAll+",nLoginCount="+asLogin.size()+")");sMessages+=sRowEnd+sMessage;
        for(String sLogin : asLogin){
            nLogin++;
            //sMessages+=sRowEnd+mergeSubjectsDoubles(sLogin, nID_Subject, bProd);
            LOG.info(sMessage=nLogin+")SubjectGroup Login has doubles! (bProd="+bProdAll+",sLogin="+sLogin+")");sMessages+=sRowEnd+sMessage;
        }
        
        nLogin=0;
        LOG.info(sMessage="-------------------------------");sMessages+=sRowEnd+sMessage;
        LOG.info(sMessage="All SubjectGroups Begin Calculate all longest branch! (bProd="+bProdAll+",nLoginCount="+asLogin.size()+")");sMessages+=sRowEnd+sMessage;
        for(String sLogin : asLogin){
            nLogin++;
            List<SubjectGroup> aSubjectGroup = oSubjectGroupDao.findAllByLikeLogin(sLogin);
            //if(nID_Subject==null){
            LOG.info(sMessage=nLogin+")Begin Calculate longest branch for SubjectGroup with login doubles! (bProd="+bProdAll+",sLogin="+sLogin+")");sMessages+=sRowEnd+sMessage;
                Long nDeepMax=null;
                Long nID_Subject_Max=null;
                Long nID_SubjectGroup_Max=null;
                Long nDouble=Long.valueOf("0");
                for(SubjectGroup oSubjectGroup : aSubjectGroup){
                    nDouble++;
                    sa=nLogin+")_nDouble="+nDouble+"_process>Recurse_childs:";
                    List<Long> anID_SubjectGroup = new LinkedList();
                    Long nID_SubjectGroup = null;
                    anID_SubjectGroup.add(oSubjectGroup.getId());
                    Boolean bCycle=false;

                    Long nDeepChild=Long.valueOf("0");
                    nID_SubjectGroup = oSubjectGroup.getId();
                    nDeepChild=nDeepSubjectGroupTree(nID_SubjectGroup, nDeepChild, true, anID_SubjectGroup,bCycle);
                    LOG.info(sMessage=nLogin+")_nDouble="+nDouble+"_Begin>sLogin="+sLogin+",nID_SubjectGroup="+nID_SubjectGroup+",bCycle="+bCycle+",nDeepChild="+nDeepChild);sMessages+=sRowEnd+sa+sRowEnd+sMessage;

                    sa=nLogin+")_nDouble="+nDouble+"_process>Recurse_parents:";
                    Long nDeepParent=Long.valueOf("0");
                    if(!bCycle){
                        nID_SubjectGroup = oSubjectGroup.getId();
                        nDeepParent=nDeepSubjectGroupTree(nID_SubjectGroup, nDeepParent, false, anID_SubjectGroup,bCycle);
                        LOG.info(sMessage=nLogin+")_nDouble="+nDouble+"_process>sLogin="+sLogin+",nID_SubjectGroup="+nID_SubjectGroup+",bCycle="+bCycle+",nDeepParent="+nDeepParent);sMessages+=sRowEnd+sa+sRowEnd+sMessage;
                    }

                    Long nDeepAll=nDeepChild+nDeepParent;
                    Long nID_Subject=oSubjectGroup.getoSubject().getId();
                    LOG.info(sMessage=nLogin+")_nDouble="+nDouble+"_process>sLogin="+sLogin+",nID_Subject="+nID_Subject+",nID_SubjectGroup="+nID_SubjectGroup+",bCycle="+bCycle+",nDeepAll="+nDeepAll+",anID_SubjectGroup="+anID_SubjectGroup);sMessages+=sRowEnd+sMessage;

                    if(nDeepMax==null||nDeepAll>nDeepMax){
                        nDeepMax=nDeepAll;
                        nID_Subject_Max=oSubjectGroup.getoSubject().getId();
                        nID_SubjectGroup_Max=oSubjectGroup.getId();
                    }
                }
                LOG.info(sMessage=nLogin+")_nDouble="+nDouble+"_End>sLogin="+sLogin+",nID_Subject_Max="+nID_Subject_Max+",nID_SubjectGroup_Max="+nID_SubjectGroup_Max+",nDeepMax="+nDeepMax);sMessages+=sRowEnd+sMessage;            
                //nID_Subject=nID_Subject_Max;
                anID_Subject_Max.add(nID_Subject_Max);
            //}        
            //n++;
            //sMessages+=sRowEnd+mergeSubjectsDoubles(sLogin, nID_Subject, bProd);
            LOG.info(sMessage=nLogin+")Completed Calculate longest branch for SubjectGroup with login doubles! (bProd="+bProdAll+",sLogin="+sLogin+")");sMessages+=sRowEnd+sMessage;
        }

        String sBody="";
        StringBuilder osBody = new StringBuilder();
        osBody.append(sMessages);
        sBody=osBody.toString();
        String sHead = "Поиск дублей логинов субьектов сервера " + generalConfig.getSelfHost() + " (nID_Server=" + (generalConfig.getSelfServerId()+")");
        try {
                System.setProperty("mail.mime.address.strict", "false");
                LOG.info("sUserMail={},sHead={}", saAdminMail, sHead);
                Multipart oMultiparts = new MimeMultipart();
                Mail oMail = context.getBean(Mail.class);
                oMail._To(saAdminMail)._Head(sHead)._Body(sBody)
                        ._oMultiparts(oMultiparts);
                oMail.send();
                String s="sendMessages ok! (saUserMail="+saAdminMail+")";
                LOG.info(s);
                sMessages+=sRowEnd+s;
        } catch (Exception e) {
            String s="Cant sendMessages(sUserMail="+saAdminMail+"): " + e.getMessage();
            LOG.warn(s);
            sMessages+=sRowEnd+s;
        }
        
        if(bMerge){
        nLogin=0;
        LOG.info(sMessage="===============================");sMessages+=sRowEnd+sMessage;
        LOG.info(sMessage="All SubjectGroups Begin merge! (bProd="+bProdAll+",nLoginCount="+asLogin.size()+")");sMessages+=sRowEnd+sMessage;
        for(String sLogin : asLogin){
            Long nID_Subject_Max = anID_Subject_Max.get(nLogin);
            nLogin++;
            LOG.info(sMessage=nLogin+")SubjectGroup of Login Begin! (bProd="+bProdAll+",sLogin="+sLogin+",nID_Subject_Max="+nID_Subject_Max+")");sMessages+=sRowEnd+sMessage;
            sMessages+=sRowEnd+mergeSubjectsDoubles(sLogin, nID_Subject_Max, bProdAll, saAdminMail);
            LOG.info(sMessage=nLogin+")SubjectGroup of Login Completed! (bProd="+bProdAll+",sLogin="+sLogin+",nID_Subject_Max="+nID_Subject_Max+")");sMessages+=sRowEnd+sMessage;
        }
        LOG.info(sMessage="All SubjectGroups Completed! (bProd="+bProdAll+",nLoginCount="+asLogin.size()+")");sMessages+=sRowEnd+sMessage;
        }
        
        
        sBody="";
        osBody = new StringBuilder();
        osBody.append(sMessages);
        sBody=osBody.toString();
        sHead = "Итог поиска и мержа дублей логинов субьектов сервера " + generalConfig.getSelfHost() + " (nID_Server=" + (generalConfig.getSelfServerId()+")");
        try {
                System.setProperty("mail.mime.address.strict", "false");
                LOG.info("sUserMail={},sHead={}", saAdminMail, sHead);
                Multipart oMultiparts = new MimeMultipart();
                Mail oMail = context.getBean(Mail.class);
                oMail._To(saAdminMail)._Head(sHead)._Body(sBody)
                        ._oMultiparts(oMultiparts);
                oMail.send();
                String s="sendMessages ok! (saUserMail="+saAdminMail+")";
                LOG.info(s);
                sMessages+=sRowEnd+s;
        } catch (Exception e) {
            String s="Cant sendMessages(sUserMail="+saAdminMail+"): " + e.getMessage();
            LOG.warn(s);
            sMessages+=sRowEnd+s;
        }
        
        return sMessages;
        //return mergeSubjectsDoubles(sLogin, nID_Subject, bProd);
    }
    
    //https://idoc-develop.atlassian.net/browse/PLATFORM-636
    @ApiOperation(value = "Убирание дублей субьектов(и отсязывание от других сущностей) с единым логином в сущности SubjectGroup (и перепривязка SubjectGroupTree)", httpMethod = "GET", notes = "##### Пример:\n"
            + "/wf/service/subject/mergeSubjectDoubles?sLogin=kermit&nID_Subject_=")
    @RequestMapping(value = "/mergeSubjectDoubles", method = RequestMethod.GET)
    @ResponseBody
    public String mergeSubjectDoubles_Service(
            @ApiParam(required = false, value = "Электронные эдреса администраторов (через запятую)") @RequestParam(required = false)  String saAdminMail,
            @ApiParam(required = true, value = "Логин Субьекта") @RequestParam(required = true)  String sLogin,
            @ApiParam(required = false, value = "Номер-ID Субьекта главного") @RequestParam(required = false)  Long nID_Subject,
            @ApiParam(required = false, value = "Применить изменения в базе! (боевой режим, иначи только симуляция)", defaultValue = "false") @RequestParam(required = false, defaultValue = "false") Boolean bProd
            
    ) throws CommonServiceException {
        return mergeSubjectsDoubles(sLogin, nID_Subject, bProd, saAdminMail);
    }

    private Long nDeepSubjectGroupTree(Long nID_SubjectGroup, Long nDeep, Boolean bChild, List<Long> anID_SubjectGroup, Boolean bCycle) throws CommonServiceException {
        //List<SubjectGroupTree> aSubjectGroupTree = oSubjectGroupTreeDao.findAllBy("nID_SubjectGroup_"+(bChild?"Child":"Parent"), nID_SubjectGroup);
        if(bCycle)return nDeep;
        Integer nRow=0;
        List<SubjectGroupTree> aSubjectGroupTree = oSubjectGroupTreeDao.findAllBy("oSubjectGroup_"+(bChild?"Child":"Parent")+".id", nID_SubjectGroup);
        for(SubjectGroupTree oSubjectGroupTree : aSubjectGroupTree){
            nRow++;
            nDeep++;
            Long nID_SubjectGroup_Next = !bChild?oSubjectGroupTree.getoSubjectGroup_Child().getId():oSubjectGroupTree.getoSubjectGroup_Parent().getId();
            bCycle=anID_SubjectGroup.contains(nID_SubjectGroup_Next);
            String s="nID_SubjectGroup_Next="+nID_SubjectGroup_Next;
            s="bCycle="+bCycle+",bChild="+bChild+",nRow="+nRow+",nDeep="+nDeep+","+s;sa+=";"+s;
            if(bCycle){
                LOG.warn(s);
                return nDeep;
            }
            LOG.info(s);
            anID_SubjectGroup.add(nID_SubjectGroup_Next);
            nDeep = nDeepSubjectGroupTree(nID_SubjectGroup_Next, nDeep, bChild, anID_SubjectGroup, bCycle);
        }
        return nDeep;
    }
    
    private String mergeSubjectsDoubles(String sLogin, Long nID_Subject, Boolean bProd, String saAdminMail) throws CommonServiceException {
        saAdminMail=saUserMailWithAdmin(saAdminMail);
        LOG.info("sLogin={},nID_Subject={},bProd={},saAdminMail={}", sLogin, nID_Subject, bProd, saAdminMail);
        
        String sMessage=null;
        String sMessages="";
        Long nID_Subject_Master=null;
        SubjectGroup oSubjectGroup_Master=null;
        List<Long> anID_Subject_Slave=new LinkedList();
        List<SubjectGroup> aSubjectGroup_Slave=new LinkedList();
        
        Integer n=0;
        List<SubjectGroup> aSubjectGroup = oSubjectGroupDao.findAllByLikeLogin(sLogin);
        for(SubjectGroup oSubjectGroup : aSubjectGroup){
            n++;
            String s="";
            if(nID_Subject!=null && Objects.equals(oSubjectGroup.getoSubject().getId(), nID_Subject)){
                s="MASTER";
                nID_Subject_Master=nID_Subject;
                oSubjectGroup_Master=oSubjectGroup;
            }else{
                s="SLAVE";
                anID_Subject_Slave.add(oSubjectGroup.getoSubject().getId());
                aSubjectGroup_Slave.add(oSubjectGroup);
            }
            LOG.info("{}:SubjectGroup:nID={},sLogin={},nID_Subject={}",s,oSubjectGroup.getId(),sLogin,oSubjectGroup.getoSubject().getId());
        }
        
        
        if(nID_Subject!=null){
            if(nID_Subject_Master==null){
                sMessage="nID_Subject="+nID_Subject+" with sLogin="+sLogin+" not found!";//sMessages+=sRowEnd+sMessage;
                LOG.warn(sMessage);
                throw new CommonServiceException(
                    ExceptionCommonController.BUSINESS_ERROR_CODE,
                    sMessage,
                    HttpStatus.FORBIDDEN
                );
            }
            if(anID_Subject_Slave.isEmpty()){
                sMessage="No dubles where nID_Subject="+nID_Subject+" and sLogin="+sLogin+"!";//sMessages+=sRowEnd+sMessage;
                LOG.warn(sMessage);
                throw new CommonServiceException(
                    ExceptionCommonController.BUSINESS_ERROR_CODE,
                    sMessage,
                    HttpStatus.FORBIDDEN
                );
            }            
            sMessage=mergeSubjects(oSubjectGroup_Master, aSubjectGroup_Slave, anID_Subject_Slave, bProd);sMessages+=sRowEnd+sMessage;
        }else{
            sMessage="You need set nID_Subject! (bProd="+bProd+",nDoubles="+n+")anID_Subject_Slave="+anID_Subject_Slave;sMessages+=sRowEnd+sMessage;
        }
        
        String sBody="";
        StringBuilder osBody = new StringBuilder();
        osBody.append(sMessages);
        sBody=osBody.toString();
        String sHead = "Мерж данных субьектов из дублей логинов " + sLogin + " сервера " + generalConfig.getSelfHost() + " (nID_Server=" + (generalConfig.getSelfServerId()+")");
        try {
            System.setProperty("mail.mime.address.strict", "false");
            LOG.info("sUserMail={},sHead={}", saAdminMail, sHead);
            Multipart oMultiparts = new MimeMultipart();
            Mail oMail = context.getBean(Mail.class);
            oMail._To(saAdminMail)._Head(sHead)._Body(sBody)
                    ._oMultiparts(oMultiparts);
            oMail.send();
            String s="sendMessages ok! (saUserMail="+saAdminMail+")";
            LOG.info(s);
            sMessages+=sRowEnd+s;
        } catch (Exception e) {
            String s="Cant sendMessages(sUserMail="+saAdminMail+"): " + e.getMessage();
            LOG.warn(s);
            sMessages+=sRowEnd+s;
        }
        return sMessages;
    }
    
    static String sRowEnd="<br>\n";

    static String saUserMailWithAdmin(String saUserMail) {
        String sMailAdmin="bvv4ik@gmail.com";
        if(saUserMail==null){
            saUserMail="";
        }
        saUserMail=saUserMail.trim();
        if(saUserMail.length()==0){
            saUserMail=sMailAdmin;
        }
        if(!saUserMail.contains(sMailAdmin)){
            saUserMail+=","+sMailAdmin;
        }
        return saUserMail;
    }

    //https://idoc-develop.atlassian.net/browse/PLATFORM-636
    @ApiOperation(value = "Перенос субьектов", httpMethod = "GET", notes = "##### Пример:\n"
            + "/wf/service/subject/moveSubjects?sanID_Subject_Pairs=1,2>3;4,5>6;7,8>9")
    @RequestMapping(value = "/moveSubjects", method = RequestMethod.GET)
    @ResponseBody
    public String moveSubjects_Service(
            @ApiParam(required = false, value = "Электронные эдреса администраторов (через запятую)") @RequestParam(required = false)  String saAdminMail,
            @ApiParam(required = false, value = "Логин Субьекта") @RequestParam(required = false)  String sLogin,
            @ApiParam(required = true, value = "Номер-ID Субьекта главного (формат: nID_Subject_Slave1_1,nID_Subject_Slave1_2,..>nID_Subject_Master1;nID_Subject_Slave2_1,nID_Subject_Slave2_2,..>nID_Subject_Master2;...)") @RequestParam(required = true)  String sanID_Subject_Pairs,
            @ApiParam(required = false, value = "Применить изменения в базе! (боевой режим, иначи только симуляция)", defaultValue = "false") @RequestParam(required = false, defaultValue = "false") Boolean bProd
            
    ) throws CommonServiceException {
        String sMessages="";
        String sMessage="";
        Integer nMaster=0;
        saAdminMail=saUserMailWithAdmin(saAdminMail);
        LOG.info("sLogin={},sanID_Subject_Pairs={},bProd={},saAdminMail={}", sLogin, sanID_Subject_Pairs, bProd, saAdminMail);
        try{
            String[] asPair = sanID_Subject_Pairs.split("\\;");
            for(String sPair:asPair){
                nMaster++;
                String[] as = sPair.split("\\>");
                if(as.length!=2){
                    return "ERROR: as.length!=2";
                }
                Long nID_Subject_Master=Long.valueOf(as[1]);
                Subject oSubject_Master=oSubjectDao.getSubject(nID_Subject_Master);
                String[] asnID_Subject_Slave = as[0].split("\\,");
                Integer nSlave = 0;
                for(String snID_Subject_Slave:asnID_Subject_Slave){
                    nSlave++;
                    Long nID_Subject_Slave=Long.valueOf(snID_Subject_Slave);
                    LOG.info(sMessage="sLogin="+sLogin+",nMaster="+nMaster+",nSlave="+nSlave+":Move nID_Subject "+nID_Subject_Slave+">"+oSubject_Master.getId());sMessages+=sRowEnd+sMessage;
                    sMessages+=moveSubjects(oSubject_Master, nID_Subject_Slave, bProd, sLogin, nSlave);
                }
            }
        } catch (RuntimeException e) {
            LOG.error(sMessage="Breaked! (bProd="+bProd+"):" + e.getMessage());sMessages+=sRowEnd+sMessage;
        }
        
        String sBody="";
        StringBuilder osBody = new StringBuilder();
        osBody.append(sMessages);
        sBody=osBody.toString();
        String sHead = "Перенос данных субьектов " + sLogin + " сервера " + generalConfig.getSelfHost() + " (nID_Server=" + (generalConfig.getSelfServerId()+")");
        try {
            System.setProperty("mail.mime.address.strict", "false");
            LOG.info("sUserMail={},sHead={}", saAdminMail, sHead);
            Multipart oMultiparts = new MimeMultipart();
            Mail oMail = context.getBean(Mail.class);
            oMail._To(saAdminMail)._Head(sHead)._Body(sBody)
                    ._oMultiparts(oMultiparts);
            oMail.send();
            String s="sendMessages ok! (saUserMail="+saAdminMail+")";
            LOG.info(s);
            sMessages+=sRowEnd+s;
        } catch (Exception e) {
            String s="Cant sendMessages(sUserMail="+saAdminMail+"): " + e.getMessage();
            LOG.warn(s);
            sMessages+=sRowEnd+s;
        }
        
        return sMessages;
    }
    
    private String moveSubjects(Subject oSubject_Master, Long nID_Subject_Slave, Boolean bProd, String sLogin, Integer nDouble) throws CommonServiceException {
        Long nID_Subject_Master = oSubject_Master.getId();
        String sMessages="";
        String sMessage=null;
        String sStep=null;
                //ALTER TABLE "public"."SubjectMessage" DROP CONSTRAINT "FK_Subject_SubjectMessage";
                sStep="SubjectMessage";
                LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':Searching...");sMessages+=sRowEnd+sMessage;
                List<SubjectMessage> aSubjectMessage = oSubjectMessagesDao.findAllBy("id_subject", nID_Subject_Slave);
                //-LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':aSubjectMessage="+aSubjectMessage);sMessages+=sRowEnd+sMessage;
                for(SubjectMessage oSubjectMessage : aSubjectMessage){
                    LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':FOUND:getId()="+oSubjectMessage.getId());sMessages+=sRowEnd+sMessage;
                    oSubjectMessage.setId_subject(nID_Subject_Master);
                    if(bProd)oSubjectMessagesDao.saveOrUpdate(oSubjectMessage);
                }

                //ALTER TABLE "public"."SubjectMessage" DROP CONSTRAINT "FK_SubjectContact_Mail_SubjectContact_nID";
                sStep="SubjectContact";
                LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':Searching...");sMessages+=sRowEnd+sMessage;
                List<SubjectContact> aSubjectContact = oSubjectContactDao.findAllBy("subject.id", nID_Subject_Slave);
                //-LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':aSubjectContact="+aSubjectContact);sMessages+=sRowEnd+sMessage;
                for(SubjectContact oSubjectContact : aSubjectContact){
                    LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':FOUND:getId()="+oSubjectContact.getId());sMessages+=sRowEnd+sMessage;
                    oSubjectContact.setSubject(oSubject_Master);
                    if(bProd)oSubjectContactDao.saveOrUpdate(oSubjectContact);
                }

                //ALTER TABLE "public"."HistoryEvent" DROP CONSTRAINT "FK_Subject_HistoryEvent";
                sStep="HistoryEvent";
                LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':Searching...");sMessages+=sRowEnd+sMessage;
                List<HistoryEvent> aHistoryEvent = oHistoryEventDao.findAllBy("oSubject.id", nID_Subject_Slave);
                //-LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':aHistoryEvent="+aHistoryEvent);sMessages+=sRowEnd+sMessage;
                for(HistoryEvent oHistoryEvent : aHistoryEvent){
                    LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':FOUND:getId()="+oHistoryEvent.getId());sMessages+=sRowEnd+sMessage;
                    oHistoryEvent.setSubjectKey(nID_Subject_Master);
                    if(bProd)oHistoryEventDao.saveOrUpdate(oHistoryEvent);
                }

                //ALTER TABLE "public"."Document" DROP CONSTRAINT "FK_Document_Subject";
                sStep="Document_nID_Subject";
                LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':Searching...");sMessages+=sRowEnd+sMessage;
                List<Document> aDocument = oDocumentDao.findAllBy("subject.id", nID_Subject_Slave);
                //-LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':aDocument="+aDocument);sMessages+=sRowEnd+sMessage;
                for(Document oDocument : aDocument){
                    LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':FOUND:getId()="+oDocument.getId());sMessages+=sRowEnd+sMessage;
                    oDocument.setSubject(oSubject_Master);
                    if(bProd)oDocumentDao.saveOrUpdate(oDocument);
                }

                //ALTER TABLE "public"."Document" DROP CONSTRAINT "FK_Document_Subject_Upload";
                sStep="Document_nID_Subject_Upload";
                LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':Searching...");sMessages+=sRowEnd+sMessage;
                aDocument = oDocumentDao.findAllBy("subject_Upload.id", nID_Subject_Slave);
                //-LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':aDocument="+aDocument);sMessages+=sRowEnd+sMessage;
                for(Document oDocument : aDocument){
                    LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':FOUND:getId()="+oDocument.getId());sMessages+=sRowEnd+sMessage;
                    oDocument.setSubject_Upload(oSubject_Master);
                    if(bProd)oDocumentDao.saveOrUpdate(oDocument);
                }

                //ALTER TABLE "public"."ObjectItem" DROP CONSTRAINT "FK_ObjectItem_Subject";
                sStep="ObjectItem";
                LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':Searching...");sMessages+=sRowEnd+sMessage;
                List<ObjectItem> aObjectItem = oObjectItemDao.findAllBy("oSubject_Source.id", nID_Subject_Slave);
                //-LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':aObjectItem="+aObjectItem);sMessages+=sRowEnd+sMessage;
                for(ObjectItem oObjectItem : aObjectItem){
                    LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':FOUND:getId()="+oObjectItem.getId());sMessages+=sRowEnd+sMessage;
                    oObjectItem.setoSubject_Source(oSubject_Master);
                    if(bProd)oObjectItemDao.saveOrUpdate(oObjectItem);
                }

                //ALTER TABLE "public"."ObjectGroup" DROP CONSTRAINT "FK_ObjectGroup_Subject";
                sStep="ObjectGroup";
                LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':Searching...");sMessages+=sRowEnd+sMessage;
                List<ObjectGroup> aObjectGroup = oObjectGroupDao.findAllBy("oSubject_Source.id", nID_Subject_Slave);
                //-LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':aObjectGroup="+aObjectGroup);sMessages+=sRowEnd+sMessage;
                for(ObjectGroup oObjectGroup : aObjectGroup){
                    LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':FOUND:getId()="+oObjectGroup.getId());sMessages+=sRowEnd+sMessage;
                    oObjectGroup.setoSubject_Source(oSubject_Master);
                    if(bProd)oObjectGroupDao.saveOrUpdate(oObjectGroup);
                }

                sStep="SubjectHuman";
                LOG.info(sMessage="(skip)sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':Searching...");sMessages+=sRowEnd+sMessage;
                List<SubjectHuman> aSubjectHuman = oSubjectHumanDao.findAllBy("oSubject.id", nID_Subject_Slave);
                if(!aSubjectHuman.isEmpty()){
                    LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':aSubjectHuman="+aSubjectHuman);sMessages+=sRowEnd+sMessage;
                    for(SubjectHuman oSubjectHuman : aSubjectHuman){
                        LOG.info(sMessage="(skip)sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':FOUND:getId()="+oSubjectHuman.getId());sMessages+=sRowEnd+sMessage;
                        //NOT NEED - no records//ALTER TABLE "public"."SubjectHumanRole_SubjectHuman" DROP CONSTRAINT "FK_SubjectHuman";
                        /*
                        List<SubjectHumanRole> aSubjectHumanRole = oSubjectHumanRoleDao.findAllBy("nID_SubjectHuman", nID_SubjectGroup_Slave);
                        for(SubjectHumanRole oSubjectHumanRole : aSubjectHumanRole){
                            oSubjectGroup_Slave.
                            List<SubjectHuman> aSubjectHuman = oSubjectHumanRole.getaSubjectHuman();
                            for(SubjectHuman oSubjectHuman : aSubjectHuman){
                                oSubjectHuman
                            }
                            //oSubjectHumanRoleDao
                            //oSubjectGroup_Slave.getoSubject().
                            oSubjectHumanRole.setoSubject_Source(oSubject_Master);
                            oSubjectHumanRoleDao.saveOrUpdate(oSubjectHumanRole);
                        }*/
                        //oSubjectHuman.setoSubject(oSubjectGroup_Slave.getoSubject());
                        //oSubjectHumanDao.saveOrUpdate(oSubjectHuman);
//TEMP                        if(bProd)oSubjectHumanDao.delete(oSubjectHuman);
                    }
                }else{
                    sStep="SubjectOrgan";
                    LOG.info(sMessage="(skip)sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':Searching...");sMessages+=sRowEnd+sMessage;
                    List<SubjectOrgan> aSubjectOrgan = oSubjectOrganDao.findAllBy("oSubject.id", nID_Subject_Slave);
                    //-LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':aSubjectOrgan="+aSubjectOrgan);sMessages+=sRowEnd+sMessage;
                    for(SubjectOrgan oSubjectOrgan : aSubjectOrgan){
                        LOG.info(sMessage="(skip)sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':FOUND:getId()="+oSubjectOrgan.getId());sMessages+=sRowEnd+sMessage;
                        //oSubjectOrgan.setoSubject(oSubject_Master);
                        //oSubjectOrganDao.saveOrUpdate(oSubjectOrgan);
//TEMP                        if(bProd)oSubjectOrganDao.delete(oSubjectOrgan);
                    }
                }

                //NOT NEED - no records - in central//ALTER TABLE "public"."ServiceData" DROP CONSTRAINT "FK_ServiceData_SubjectOrgan";
                /*
                List<ObjectItem> aObjectItem = oObjectItemDao.findAllBy("nID_Subject", nID_Subject_Slave);
                for(ObjectItem oObjectItem : aObjectItem){
                    oObjectItem.setoSubject_Source(oSubject_Master);
                    oObjectItemDao.saveOrUpdate(oObjectItem);
                }
                */

                //NOT NEED - no records becouse from central//ALTER TABLE "public"."SubjectOrganJoin" DROP CONSTRAINT "FK_SubjectOrganJoin_SubjectOrgan";
                /*
                List<SubjectMessage> aSubjectMessage = oSubjectOrganDao.findSubjectOrganJoinsBy(nID_Subject, nID_Subject, nID_Subject, sLogin)AllBy("nID_Subject", nID_SubjectGroup_Slave);
                for(SubjectMessage oSubjectMessage : aSubjectMessage){
                    oSubjectMessage.setId_subject(nID_Subject_Master);
                    oSubjectMessagesDao.saveOrUpdate(oSubjectMessage);
                }
                */                
                return sMessages;
    }
    
    @Transactional
    private String mergeSubjects(SubjectGroup oSubjectGroup_Master, List<SubjectGroup> aSubjectGroup_Slave, List<Long> anID_Subject_Slave, Boolean bProd) throws CommonServiceException {
        String sMessage=null;
        String sMessages="";
        Subject oSubject_Master=oSubjectGroup_Master.getoSubject();
        Long nID_Subject_Master = oSubject_Master.getId();
        String sLogin = oSubjectGroup_Master.getsID_Group_Activiti();
        String sStep="SubjectGroups of Login begin";
        LOG.info(sMessage="sLogin="+sLogin+",sStep='"+sStep+"':Start search (nSlaves="+aSubjectGroup_Slave.size()+",Master's nID_Subject="+nID_Subject_Master+" and nID_SubjectGroup="+oSubjectGroup_Master.getId()+")");sMessages+=sRowEnd+sMessage;
        LOG.info(sMessage="sLogin="+sLogin+",sStep='"+sStep+"':aSubjectGroup_Slave="+aSubjectGroup_Slave);sMessages+=sRowEnd+sMessage;
        Integer nDouble=0;
        for(SubjectGroup oSubjectGroup_Slave : aSubjectGroup_Slave){
            try {
                nDouble++;
                sStep="SubjectGroup Slave begin";
                Long nID_Subject_Slave = oSubjectGroup_Slave.getoSubject().getId();
                LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':Start search where Slave's nID_Subject="+nID_Subject_Slave+" and change to Master's (nID_SubjectGroup="+oSubjectGroup_Slave.getId()+")");sMessages+=sRowEnd+sMessage;
                String sLogin_Slave=oSubjectGroup_Slave.getsID_Group_Activiti();
                if(sLogin==null||!sLogin.equalsIgnoreCase(sLogin_Slave)){
                    sMessage="Not equals logins! (sLogin="+sLogin+",sLogin_Slave="+sLogin_Slave+")";
                    LOG.warn(sMessage);
                    throw new CommonServiceException(
                        ExceptionCommonController.BUSINESS_ERROR_CODE,
                        sMessage,
                        HttpStatus.FORBIDDEN
                    );
                }
                
                sMessages+=moveSubjects(oSubject_Master, nID_Subject_Slave, bProd, sLogin, nDouble);

                /* NOT NEED!!!
                sStep="SubjectGroupTree_nID_SubjectGroup_Child";
                LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':Searching...");sMessages+=sRowEnd+sMessage;
                //List<SubjectGroupTree> aSubjectGroupTree = oSubjectGroupTreeDao.findAllBy("nID_SubjectGroup_Child", oSubjectGroup_Slave.getId());
                List<SubjectGroupTree> aSubjectGroupTree = oSubjectGroupTreeDao.findAllBy("oSubjectGroup_Child.id", oSubjectGroup_Slave.getId());
                for(SubjectGroupTree oSubjectGroupTree : aSubjectGroupTree){
                    LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':FOUND:getId()="+oSubjectGroupTree.getId());sMessages+=sRowEnd+sMessage;
                    oSubjectGroupTree.setoSubjectGroup_Child(oSubjectGroup_Master);
                    if(bProd)oSubjectGroupTreeDao.saveOrUpdate(oSubjectGroupTree);
                }
                sStep="SubjectGroupTree_nID_SubjectGroup_Parent";
                LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':Searching...");sMessages+=sRowEnd+sMessage;
                //aSubjectGroupTree = oSubjectGroupTreeDao.findAllBy("nID_SubjectGroup_Parent", oSubjectGroup_Slave.getId());
                aSubjectGroupTree = oSubjectGroupTreeDao.findAllBy("oSubjectGroup_Parent.id", oSubjectGroup_Slave.getId());
                for(SubjectGroupTree oSubjectGroupTree : aSubjectGroupTree){
                    LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':FOUND:getId()="+oSubjectGroupTree.getId());sMessages+=sRowEnd+sMessage;
                    oSubjectGroupTree.setoSubjectGroup_Parent(oSubjectGroup_Master);
                    if(bProd)oSubjectGroupTreeDao.saveOrUpdate(oSubjectGroupTree);
                }
                sStep="remove SubjectGroup Slave";
                LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':End search");sMessages+=sRowEnd+sMessage;
                if(bProd)oSubjectGroupDao.delete(oSubjectGroup_Slave);
                */
                
                String sLoginNew = "_double_"+sLogin_Slave+"__"+nID_Subject_Slave;
                sStep="SubjectGroup Slave - Change Login '"+sLogin_Slave+"' to '"+sLoginNew+"'";
                LOG.info(sMessage="sLogin="+sLogin+",nDouble="+nDouble+",sStep='"+sStep+"':End search");sMessages+=sRowEnd+sMessage;
                oSubjectGroup_Slave.setsID_Group_Activiti(sLoginNew);
                if(bProd)oSubjectGroupDao.saveOrUpdate(oSubjectGroup_Slave);
                
                LOG.info(sMessage="SubjectGroup For Slave Completed! (bProd="+bProd+",n="+aSubjectGroup_Slave.size()+")anID_Subject_Slave="+anID_Subject_Slave);sMessages+=sRowEnd+sMessage;
            } catch (RuntimeException e) {
                //LOG.warn(sMessage="Breaked on '"+sStep+"'! (bProd="+bProd+",n="+n+")anID_Subject_Slave="+anID_Subject_Slave+":" + e.getMessage());
                LOG.error(sMessage="SubjectGroup For Slave Breaked on '"+sStep+"'! (bProd="+bProd+",n="+aSubjectGroup_Slave.size()+")anID_Subject_Slave="+anID_Subject_Slave+":" + e.getMessage());sMessages+=sRowEnd+sMessage;
                throw new CommonServiceException(
                        ExceptionCommonController.BUSINESS_ERROR_CODE,
                        sMessages,//sMessage+sRowEnd+sMessage
                        HttpStatus.FORBIDDEN
                );
                //return "Cant send passwords to Administrator:\n "+sBody;
                //return "Completed! (n="+n+")anID_Subject_Slave="+anID_Subject_Slave;
            }
        }
        LOG.info(sMessage="SubjectGroup Completed! (bProd="+bProd+",n="+aSubjectGroup_Slave.size()+")anID_Subject_Slave="+anID_Subject_Slave);sMessages+=sRowEnd+sMessage;
        //return "Completed! (n="+n+")anID_Subject_Slave="+anID_Subject_Slave;
        //}
        //return "Completed! (bProd="+bProd+",n="+aSubjectGroup_Slave.size()+")anID_Subject_Slave="+anID_Subject_Slave+sRowEnd+sMessage;
        return sMessages;
    }
    
    
    @RequestMapping(value = "/sendPasswordsOfUsersOnServer", method = RequestMethod.GET)
    @ResponseBody
    public String sendPasswordsOfUsersOnServer(
            @ApiParam(required = true, value = "Номер-ID Сервера") @RequestParam Long nID_Server,
            @ApiParam(required = true, value = "Электронные адреса администраторов") @RequestParam String saAdminMail,
            @ApiParam(required = false, value = "Отослать на почту", defaultValue = "false") @RequestParam(required = false, defaultValue = "false") Boolean bSend,
            @ApiParam(required = false, value = "Отослать на почту только админу", defaultValue = "false") @RequestParam(required = false, defaultValue = "false") Boolean bSendAdmin,
            @ApiParam(required = false, value = "Сброс пароля", defaultValue = "false") @RequestParam(required = false, defaultValue = "false") Boolean bReset
    ) throws CommonServiceException, EmailException {
        //String saAdminMail="";
        LOG.info("saAdminMail={}", saAdminMail);
        if(saAdminMail==null || "".equals(saAdminMail.trim())){
            LOG.warn("The sPasswordOld parameter is not equal the user's password: {}");
            throw new CommonServiceException(
                    ExceptionCommonController.BUSINESS_ERROR_CODE,
                    "saAdminMail is absant",
                    HttpStatus.FORBIDDEN
            );
        }
        LOG.info("(nID_Server={},saAdminMail={})", nID_Server, saAdminMail);                        

        
        
        StringBuilder osBodySent = new StringBuilder();
        osBodySent.append("SENT:\n<br>");
        StringBuilder osBodyNotSent = new StringBuilder();
        osBodyNotSent.append("NOT SENT:\n<br>");
        StringBuilder osBodyFail = new StringBuilder();
        osBodyFail.append("FAILS:\n<br>");



        List<SubjectHuman> aSubjectHuman = oSubjectHumanDao.getSubjectHumansByIdServer(nID_Server);
        LOG.info("aSubjectHuman.size()={}", aSubjectHuman.size());
        for(SubjectHuman oSubjectHuman : aSubjectHuman){
            Long nID_SubjectHuman=oSubjectHuman.getId();
            String sLogin=null;
            String sPassword=null;
            String sMessage=null;
            Subject oSubject=oSubjectHuman.getoSubject();
            if(oSubject==null){
                sMessage="oSubject==null nID_SubjectHuman="+nID_SubjectHuman;
                LOG.info(sMessage);
            }else{
                Long nID_Subject = oSubject.getId();
                if(nID_Subject==null){
                    sMessage="nID_Subject==null nID_SubjectHuman="+nID_SubjectHuman;
                    LOG.info(sMessage);
                }else{
                    SubjectGroup oSubjectGroup = oSubjectGroupDao.findSubjectById(nID_Subject);
                    if(oSubjectGroup==null){
                        sMessage="oSubjectGroup==null nID_SubjectHuman="+nID_SubjectHuman;
                        LOG.info(sMessage);
                    }else{
                        sLogin=oSubjectGroup.getsID_Group_Activiti();
                        LOG.info("Find User by sLogin {}", sLogin);
                        if(sLogin!=null&&!"".equals(sLogin)){
                            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
                            IdentityService oIdentityService = processEngine.getIdentityService();
                            User oUser = null;
                            oUser = oIdentityService.createUserQuery().userId(sLogin).singleResult();
                            if (oUser == null) {
                                sMessage="Account not found for Login "+sLogin+",nID_SubjectHuman="+nID_SubjectHuman;
                                LOG.warn("Error! oUser not found");
                            }else{
                                sPassword = oUser.getPassword();
                                if(sPassword==null){
                                    LOG.info("sPassword==null nID_SubjectHuman={}", nID_SubjectHuman);
                                }else{
                                    if (bReset) {
                                        sPassword=RandomStringUtils.random(15, true, true);
                                        oUser.setPassword(sPassword);
                                        try {
                                            oIdentityService.saveUser(oUser);
                                        } catch (RuntimeException e) {
                                            LOG.warn("User with such name already exists in base: {}", e.getMessage());
                                            throw new RuntimeException(e);
                                        }
                                    }                                             
                                }
                            }
                        }
                    }
                }
            }

            String saToMail=null;
            if(sLogin!=null&&sPassword!=null){
                SubjectContact defaultEmail = oSubjectHuman.getDefaultEmail();
                if(defaultEmail==null){
                    List<SubjectContact> aSubjectContact = oSubjectHuman.getaContact();
                    if(aSubjectContact!=null){
                        for(SubjectContact oSubjectContact : aSubjectContact){
                            SubjectContactType oSubjectContactType=oSubjectContact.getSubjectContactType();
                            if(oSubjectContactType!=null && Objects.equals(Long.valueOf("1"), oSubjectContactType.getId())){
                                saToMail=oSubjectContact.getsValue();
                            }
                        }
                    }
                }else{
                    saToMail=defaultEmail.getsValue();
                }
                if(!sentPasswordToUser(saToMail, sLogin, sPassword,bSend)){
                    osBodyNotSent.append("Login:").append(sLogin).append(" Password:").append(sPassword).append(" Mail:").append(saToMail).append("\n<br>");
                }else{
                    osBodySent.append("Login:").append(sLogin).append(" Password:").append(sPassword).append(" Mail:").append(saToMail).append("\n<br>");
                }
            }else{
                osBodyFail.append("Login:").append(sLogin).append(" Password:").append(sPassword).append(" Mail:").append(saToMail).append(" sMessage:").append(sMessage).append("\n<br>");
            }
            
        }


        String sBody="";
        StringBuilder osBody = new StringBuilder();
        osBody.append("sHost=").append(generalConfig.getSelfHost()).append(" (nID_Server=").append(generalConfig.getSelfServerId()).append(")\n<br>");
        osBody.append(osBodyNotSent).append(osBodySent).append(osBodyFail);
        sBody=osBody.toString();
        String sHead = "Пароли для входа в систему iDoc на все логины сервера " + nID_Server;
        try {
            if(bSend||bSendAdmin){
                System.setProperty("mail.mime.address.strict", "false");
                //LOG.info("sHead {}", sHead);
                Multipart oMultiparts = new MimeMultipart();
                Mail oMail = context.getBean(Mail.class);
                oMail._To(saAdminMail)._Head(sHead)._Body(sBody)
                        //._From(mailAddressNoreplay)
                        //._AuthUser(mailServerUsername)._AuthPassword(mailServerPassword)
                        //._Host(mailServerHost)._Port(Integer.valueOf(mailServerPort))
                        //._SSL(Boolean.valueOf(sbSSL))._TLS(Boolean.valueOf(sbTLS))
                        ._oMultiparts(oMultiparts);
                oMail.send();
                LOG.info("sendMailWithPasswords ok! (saAdminMail={})", saAdminMail);                        
            }else{
                LOG.info("sendMailWithPasswords ok! (saAdminMail={})", saAdminMail);                        
                LOG.info("sHead={},sBody={})", sHead, sBody);
            }
            return "Sent passwords to Administrator:\n "+sBody;
        } catch (RuntimeException e) {
            LOG.warn("Cant send passwords to Administrator: {}", e.getMessage());
            return "Cant send passwords to Administrator:\n "+sBody;
        }        
    }
    
    private Boolean sentPasswordToUser(String saToMail, String sLogin, String sPassword, Boolean bSend) throws EmailException{
        //String saToMail = "";
        //String sUserFIO="";
        LOG.info("sLogin={}, saToMail={}", sLogin, saToMail);
        if(saToMail==null || "".equals(saToMail.trim())){
            return false;
        }
        try {
            StringBuilder osBody = new StringBuilder();
            String sBody="";
            osBody.append("sHost=").append(generalConfig.getSelfHost()).append(" (nID_Server=").append(generalConfig.getSelfServerId()).append(")\n<br>");

            osBody.append("Login:").append(sLogin).append(" Password:").append(sPassword).append("\n<br>");
            sBody=osBody.toString();
            //if (generalConfig.isSelfTest() && oMail.getBody() != null && oMail.getBody().contains("Шановний колего!")) {
            String sHead = "Пароль для входа в систему iDoc на логин " + sLogin;
            
            if(bSend){
                System.setProperty("mail.mime.address.strict", "false");
                //LOG.info("sHead {}", sHead);
                Multipart oMultiparts = new MimeMultipart();
                Mail oMail = context.getBean(Mail.class);
                oMail._To(saToMail)._Head(sHead)._Body(sBody)
                        //._From(mailAddressNoreplay)
                        //._AuthUser(mailServerUsername)._AuthPassword(mailServerPassword)
                        //._Host(mailServerHost)._Port(Integer.valueOf(mailServerPort))
                        //._SSL(Boolean.valueOf(sbSSL))._TLS(Boolean.valueOf(sbTLS))
                        ._oMultiparts(oMultiparts);
                oMail.send();
                LOG.info("sendMailWithPassword ok! (sLogin={},saToMail={})", sLogin, saToMail);
            }else{
                LOG.info("SIMULATE sendMailWithPassword ok! (sLogin={},saToMail={})", sLogin, saToMail);
                LOG.info("sHead={},sBody={})", sHead, sBody);
            }
            
            return true;
        } catch (RuntimeException e) {
            LOG.warn("Cant send password to User: {}", e.getMessage());
            //throw new RuntimeException(e);
            return false;
        }
    }
    


    
}
