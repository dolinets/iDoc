package org.igov.service.business.email;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.igov.io.GeneralConfig;
import org.igov.model.server.Server;
import org.igov.model.server.ServerDao;
import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectGroupDao;
import org.igov.service.business.action.task.core.UsersService;
import org.igov.service.business.subject.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Oleksandr Belichenko
 */
@Service
public class EmailProcessSubjectService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailProcessSubjectService.class);

    @Autowired
    private EmailService oEmalService;

    @Autowired
    private SubjectGroupDao oSubjectGroupDao;

    @Autowired
    private SubjectService oSubjectService;

    @Autowired
    private HistoryService oHistoryService;

    @Autowired
    private GeneralConfig oGeneralConfig;
    
    @Autowired
    private ServerDao oServerDao;
    
    @Autowired
    private UsersService oUsersService;

    private static final String GREETING = "Шановний колего";
    private static final String TASK_INFO = "<b>Текст завдання:</b>";
    private static final String TERM_INFO = "<b>Термін виконання:</b>";

    private static final String EXPERT_INFO = "Вас призначено  виконавцем по  завданню №";
    private static final String EXPERT_DUTY = "Протягом  зазначеного терміну необхідно виконати завдання та надати звіт про виконання.";
    private static final String CONTROLLER_INFO = "Вас призначено  контролюючим  по  завданню №";
    private static final String CONTROLLER_INFO_ABOUT_EXPERTS = "<b>Виконавці:</b>";
    private static final String CONTROLLER_DUTY = "Протягом  зазначеного  терміну необхідно проконтролювати виконання.";

    private static final String INFO = "По документу №";
    private static final String COMMENT_INFO = " виніс(-ла) зауваження.";
    private static final String ANSWER_INFO = " автор відповів на зауваження.";
    private static final String COMMENT_TEXT = "<b>Текст зауваження: </b>";
    private static final String ANSWER_TEXT = "<b>Відповідь: </b>";
    private static final String COMMENT_DUTY = "Просимо Вас надати відповідь на зауваження.";
    
    private static final String REFERENT = ", ви стали референтом.";
    private static final String REFERENT_BODY = "Тепер ви можете відпрацьовувати документи та завдання за ";
    private static final String HOST = ", ви надали роль референта.";
    private static final String HOST_BODY = " може відпрацьовувати за вас документи та завдання.";
    private static final String NOW = "Тепер  ";

    public void sendEmail_createTask(String sLoginTo, String sBodyTask, String sExecutors, Date dDate, Long nID_Process_Activiti) {
        LOG.info("Welcome to sendEmail_createTask");
        try {
            LOG.info("sLoginTo: {}, sBodyTask: {}, sExecutors: {}, dDate: {}, nID_Process_Activiti: {}", sLoginTo, sBodyTask, sExecutors, dDate, nID_Process_Activiti);

            String sNameTo = getsName(sLoginTo);

            /*String sEmailTo = " ";
            if (oGeneralConfig.isSelfTest()) {
            sEmailTo = oGeneralConfig.getsAddrClerk();
            LOG.info("sEmailTo: {}", sEmailTo);
            } else {
            sEmailTo = getsEmail(sLoginTo);
            LOG.info("sEmailTo: {}", sEmailTo);
            }    */
            String sEmailTo = getsEmail(sLoginTo);            
            LOG.info("sEmailTo: {}", sEmailTo);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            
            String sDate = null;
            
            if(dDate != null){
                sDate = dateFormat.format(dDate);
            }else{
                sDate = "";
            }

            //Long nId_Order = oGeneralConfig.getProtectedNumber_ByProcess(String.valueOf(nID_Process_Activiti));
            //LOG.info("nId_Order: {}", nId_Order);
            String sID_order = oGeneralConfig.getOrderId_ByProcess(Long.valueOf(nID_Process_Activiti));
            LOG.info("sID_order: " + sID_order);

            String sURL = oGeneralConfig.getsTaskURL(sID_order);

            String sHead = GREETING + ' ' + sNameTo;
            String sHeadToBody = "<b>" + GREETING + "</b> " + sNameTo;
            String sBody = " ";
            if (sEmailTo != null && !sEmailTo.isEmpty()) {
                if (sExecutors == null || sExecutors.equals("")) {
                    sHead = sHead + ' ' + EXPERT_INFO;
                    sBody = sHeadToBody + "<p>" + EXPERT_INFO + sURL + '.' + "<p>" + TASK_INFO + ' ' + sBodyTask + "<p>" + TERM_INFO + ' ' + sDate + "<p>" + EXPERT_DUTY;

                    oEmalService.sendEmail(sEmailTo, sHead, sBody, null);
                } else if (sExecutors != null) {
                    sExecutors = sExecutors.replace("; ", "<br>");

                    sHead = sHead + ' ' + CONTROLLER_INFO;
                    sBody = sHeadToBody + "<p>" + CONTROLLER_INFO + sURL + '.' + "<p>" + TASK_INFO + ' ' + sBodyTask + ' ' + TERM_INFO + ' '
                            + sDate + "<p>" + CONTROLLER_INFO_ABOUT_EXPERTS + "<p>" + sExecutors + "<p>" + CONTROLLER_DUTY;

                    oEmalService.sendEmail(sEmailTo, sHead, sBody, null);
                    LOG.info("Email successfully sent!!");
                } else {
                    LOG.info("Undefined sExecutors");
                }
            } else {
                LOG.info("sEmailTo is empty!");
            }
        } catch (Exception e) {
            LOG.info("Error in sendEmail_createTask: {}", e);
        }
    }

    public void sendEmail_comment(Long nID_Process_Activiti, String sKeyGroup, String sKeyGroup_Author, String sBodyTask) {
        try {
            LOG.info("Welcome to sendEmail_comment");
            LOG.info("setProcessChat: nID_Process_Activiti: {}, sKeyGroup: {}, sKeyGroup_Author: {}, sBodyTask: {}",
                    nID_Process_Activiti, sKeyGroup, sKeyGroup_Author, sBodyTask);

            Map<String, Object> mProcessVariable = new HashMap<>();

            HistoricProcessInstance oProcessInstance = oHistoryService.createHistoricProcessInstanceQuery()
                    .processInstanceId(String.valueOf(nID_Process_Activiti).trim()).includeProcessVariables().singleResult();

            mProcessVariable = oProcessInstance.getProcessVariables();

            String sLoginAuthor = mProcessVariable.containsKey("sLoginAuthor")
                    ? (String) mProcessVariable.get("sLoginAuthor") : null;
            LOG.info("sLoginAuthor: {}", sLoginAuthor);

            //Long nId_Order = oGeneralConfig.getProtectedNumber_ByProcess(String.valueOf(nID_Process_Activiti));
            //LOG.info("nId_Order: {}", nId_Order);
            String sID_order = oGeneralConfig.getOrderId_ByProcess(Long.valueOf(nID_Process_Activiti));
            LOG.info("sID_order: " + sID_order);

            String sURL = oGeneralConfig.getsDocumentURL(sID_order);

            String sNameTo = " ";
            String sEmailTo = " ";
            String sHead = " ";
            String sHeadToBody = " ";
            String sBody = " ";

            if (sKeyGroup.equalsIgnoreCase(sKeyGroup_Author)) {
                sNameTo = getsName(sLoginAuthor);

                /*if (oGeneralConfig.isSelfTest()) {
                sEmailTo = oGeneralConfig.getsAddrClerk();
                LOG.info("sEmailTo: {}", sEmailTo);
            } else {
                sEmailTo = getsEmail(sLoginAuthor);
                LOG.info("sEmailTo: {}", sEmailTo);
            }           
                 */
                sEmailTo = getsEmail(sLoginAuthor);                 
                LOG.info("sEmailTo: {}", sEmailTo);

                String sNameFrom = getsName(sKeyGroup);

                if (sEmailTo != null && !sEmailTo.isEmpty()) {
                    sHeadToBody = "<b>" + GREETING + "</b> " + sNameTo;
                    sHead = GREETING + ' ' + sNameTo;
                    sBody = sHeadToBody + "<p>" + INFO + sURL + ' ' + sNameFrom + COMMENT_INFO + "<p>" + COMMENT_TEXT + sBodyTask + "<p>" + COMMENT_DUTY;

                    oEmalService.sendEmail(sEmailTo, sHead, sBody, null);
                    LOG.info("Email successfully sent!!");
                } else {
                    LOG.info("sEmailTo is empty!");
                }
            } else {
                sNameTo = getsName(sKeyGroup);

                /*if (oGeneralConfig.isSelfTest()) {
                sEmailTo = oGeneralConfig.getsAddrClerk();
                LOG.info("sEmailTo: {}", sEmailTo);
                } else {
                sEmailTo = getsEmail(sKeyGroup);
                }*/
                sEmailTo = getsEmail(sKeyGroup);                  
                LOG.info("sEmailTo: {}", sEmailTo);

                if (sEmailTo != null && !sEmailTo.isEmpty()) {
                    sHead = GREETING + ' ' + sNameTo;
                    sHeadToBody = "<b>" + GREETING + "</b> " + sNameTo;
                    sBody = sHeadToBody + "<p>" + INFO + sURL + ANSWER_INFO + "<p>" + ANSWER_TEXT + sBodyTask;

                    oEmalService.sendEmail(sEmailTo, sHead, sBody, null);
                    LOG.info("Email successfully sent!!");
                } else {
                    LOG.info("sEmailTo is empty!");
                }
            }
        } catch (Exception e) {
            LOG.info("Error in sendEmail_comment: {}", e);
        }
    }
    
    public void sendEmail_referent(String sID_Group_Activiti, String sLogin) {
        String sReferent = getsName(sLogin);
        String sReferentEmail = getsEmail(sLogin);        
        String sHost = getsName(sID_Group_Activiti);
        String sHostEmail = getsEmail(sID_Group_Activiti);        

        String sReferentHead = sReferent + REFERENT;
        String sReferentBody = sReferentHead + "<p>" + REFERENT_BODY + sHost;

        if (sReferentEmail != null && !sReferentEmail.isEmpty()) {
            oEmalService.sendEmail(sReferentEmail, sReferentHead, sReferentBody, null);
            LOG.info("Email successfully sent!!");
        } else {
            LOG.info("sReferentEmail is empty!");
        }

        String sHostHead = sHost + HOST;
        String sHostBody = sHostHead + "<p>" + NOW + sReferent + HOST_BODY;

        if (sHostEmail != null && !sHostEmail.isEmpty()) {
            oEmalService.sendEmail(sHostEmail, sHostHead, sHostBody, null);
            LOG.info("Email successfully sent!!");
        } else {
            LOG.info("sHostEmail is empty!");
        }
    }

    public String getsName(String sLoginAuthor) {
        SubjectGroup oSubjectGroup = oSubjectGroupDao.findBy("sID_Group_Activiti", sLoginAuthor).orNull();
        String sName = oSubjectGroup.getName();
        String sNameTo = oUsersService.getUserInitials(sName);
        return sNameTo;
    }

    public String getsEmail(String sLoginAuthor) {
        String sEmailTo = oSubjectService.getEmailByLogin(sLoginAuthor);
        return sEmailTo;
    }
    
    /*private String getsURL(String sID_order) {         
        String sTemplateDoc = "/documents/sID_Order=";

        Long nID = oGeneralConfig.getSelfServerId().longValue();
        Server oServer = oServerDao.findByIdExpected(nID);
        String sServerURL = oServer.getsURL();
        LOG.info("sURL: {}", sServerURL);
        
        String sBody = sServerURL + sTemplateDoc + sID_order;
        String sURL = "<a href=\"" + sBody + "\">" + sID_order + "</a>";
        
        return sURL;
    }*/
    
}
