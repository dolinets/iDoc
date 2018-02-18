package org.igov.service.business.escalation;

import com.google.common.base.Optional;
import com.mongodb.util.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.igov.service.business.escalation.handler.EscalationHandler;
import org.igov.util.ToolJS;
import com.mongodb.BasicDBList;
import java.util.ArrayList;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.task.Task;
import org.igov.io.GeneralConfig;
import org.igov.model.action.event.HistoryEvent_Service_StatusType;
import org.igov.model.document.DocumentStep;
import org.igov.model.document.DocumentStepDao;
import org.igov.model.document.DocumentStepSubjectRight;
import org.igov.model.process.processChat.ProcessChat;
import org.igov.model.process.processChat.ProcessChatDao;
import org.igov.model.process.processChat.ProcessChatMessage;
import org.igov.model.process.processChat.ProcessChatMessageDao;
import org.igov.model.process.processChat.ProcessChatMessageTree;
import org.igov.model.process.processChat.ProcessChatMessageTreeDao;
import org.igov.model.subject.SubjectContact;
import org.igov.model.subject.SubjectContactDao;
import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectGroupDao;
import org.igov.service.business.action.event.ActionEventHistoryService;
import org.igov.service.business.action.task.bp.handler.BpServiceHandler;
import org.igov.service.business.action.task.form.TaskForm;
import org.igov.service.business.document.DocumentStepService;
import org.igov.service.business.process.processLink.ProcessLinkService;
import org.igov.service.business.subject.SubjectGroupService;
import org.igov.service.business.subject.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class EscalationHelper implements ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(EscalationHelper.class);

    @Autowired
    GeneralConfig oGeneralConfig;
    
    @Autowired
    private TaskService taskService;
    
    private ApplicationContext applicationContext;
    
    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskForm oTaskForm;
    
    @Autowired
    private ProcessLinkService oProcessLinkService; 
       
    @Autowired
    private DocumentStepService oDocumentStepService;
    
    @Autowired
    private DocumentStepDao oDocumentStepDao;
    
    
    @Autowired
    private RuntimeService runtimeService;   
    
    @Autowired
    private ActionEventHistoryService oActionEventHistoryService;

    @Autowired
    private SubjectContactDao oSubjectContactDao;
    
    @Autowired
    private SubjectGroupDao oSubjectGroupDao;
    
    @Autowired
    private ProcessChatDao oProcessChatDao;
    
    @Autowired
    private ProcessChatMessageDao oProcessChatMessageDao;
    
    @Autowired
    private ProcessChatMessageTreeDao oProcessChatMessageTreeDao;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Modify by Yegor Kovylin
     * check if task escalated and send mail or submit it (or both)
     * @param mTaskParam - data about task
     * @param sCondition - condition of escalation
     * @param soData - rule-data from csv
     * @param sPatternFile - way to html-patter of email body (may contains email head template)
     * @param sBeanHandler - the name of EscalationHandler
     * @param sJobName
     * @throws Exception 
     */
    public void checkTaskOnEscalation(Map<String, Object> mTaskParam,
            String sCondition, String soData,
            String sPatternFile, String sBeanHandler, String sJobName) throws Exception {
        LOG.info("checkTaskOnEscalation started...");
        LOG.info("sJobName is {}", sJobName);
        //1 -- result of condition
        Map<String, Object> mDataParam = parseJsonData(soData);//from json
        mTaskParam = mTaskParam != null ? mTaskParam : new HashMap<String, Object>();

        try {
            
            List<HistoricVariableInstance> aoHistoryVariable = historyService.createHistoricVariableInstanceQuery()
                                                    .processInstanceId((String)mTaskParam.get("sProcessInstanceId")).list();
            
            //if we need any of this condition-variable coud be set from a process wariable (or we will be coun elapsed-value from create task date)                                       
            String[] asConditionToReplace = {"nElapsedDays", "nDays", "nElapsedHours", "nDueElapsedHours", "nDueElapsedDays", "nCreateElapsedHours", "nCreateElapsedDays"};
            
            for(int i = 0; i < asConditionToReplace.length; i++){
                if(mDataParam.containsKey(asConditionToReplace[i])){
                    String sVariableName = (String) mDataParam.get(asConditionToReplace[i]);
                    for(HistoricVariableInstance oHistoricVariableInstance : aoHistoryVariable){
                        if(oHistoricVariableInstance.getVariableName().equals(sVariableName)){
                            mTaskParam.replace(asConditionToReplace[i], oHistoricVariableInstance.getValue());
                            mDataParam.remove(asConditionToReplace[i]);
                            break;
                        }
                    }
                }
            }
            
            LOG.info("mDataParam is {}", mDataParam);
            LOG.info("mTaskParam is {}", mTaskParam);
            LOG.info("sCondition is {}", sCondition);
            Boolean bConditionAccept = new ToolJS().getResultOfCondition(mDataParam, mTaskParam, sCondition);
            mTaskParam.putAll(mDataParam); //concat of rule-csv data and task data

            //2 - check beanHandler
            try {
                //LOG.info("(bConditionAccept={})", bConditionAccept);
                if (bConditionAccept) {
                    EscalationHandler oEscalationHandler = getHandlerClass(sBeanHandler);
                    if (oEscalationHandler != null) {
                        //LOG.info("(basicDBList={})", mTaskParam.get("asRecipientMail"));
                        List<String> asRecipientMail = new ArrayList<>();
                        BasicDBList basicDBList = (BasicDBList) mTaskParam.get("asRecipientMail");
                        BpServiceHandler.mGuideTaskParamKey.put("asRecipientMail", "Почта получателя"); 
                        BpServiceHandler.mGuideTaskParamKey.put("nDaysLimit", "Удалить");
                        
                        if (basicDBList != null && !basicDBList.isEmpty()) {
                            //asRecipientMail = new String[basicDBList.size()]; 
                            for (int i = 0; i < basicDBList.size(); i++) {
                                if(((String)basicDBList.get(i)).contains("@")){
                                    //we set email like a constant in rule
                                    asRecipientMail.add((String)basicDBList.get(i));
                                }else{
                                    //we set email like a variable in process
                                    for(HistoricVariableInstance oHistoryVariable : aoHistoryVariable){
                                        LOG.info("oHistoryVariable name is {}", oHistoryVariable.getVariableName());
                                        String variableName = "";
                                        String tableColumnName = "";
                                        
                                        if(((String)basicDBList.get(i)).contains(","))
                                        {
                                            //if variable is a table with logins
                                            variableName = ((String)basicDBList.get(i)).split(",")[0].trim();
                                            tableColumnName = ((String)basicDBList.get(i)).split(",")[1].trim();
                                        }else{
                                            variableName = ((String)basicDBList.get(i)).trim();
                                        }
                                        
                                        LOG.info("variableName {} tableColumnName {}", variableName, tableColumnName);
                                        
                                        if(oHistoryVariable.getVariableName().equals(variableName)){
                                            if(!tableColumnName.equals("")){
                                                // if it table - we get emails by logins and add it to result email list
                                                List<String> asLogin = oTaskForm.getValuesFromTableField((String)mTaskParam.get("sProcessInstanceId"), variableName, tableColumnName);
                                                
                                                for(String sLogin : asLogin){
                                                    Optional<SubjectGroup> oSubjectGroup = oSubjectGroupDao.findBy("sID_Group_Activiti", sLogin);
                                                    if(oSubjectGroup.isPresent() && oSubjectGroup.get().getoSubject() != null){
                                                        List<String> asContacts = new ArrayList<>();
                                                        List<SubjectContact> aoSubjectContactToAdd = oSubjectContactDao.findContactsBySubjectAndContactType(oSubjectGroup.get().getoSubject(), 1L);
                                                        aoSubjectContactToAdd.forEach(oSubjectContact -> {
                                                            if (oSubjectContact.getsValue() != null && !"".equals(oSubjectContact.getsValue().trim())) {
                                                                asContacts.add(oSubjectContact.getsValue());
                                                            }
                                                        });                                     
                                                        LOG.info("asContacts is {}", asContacts);
                                                        asRecipientMail.addAll(asContacts);
                                                    }
                                                }
                                            }else{
                                                asRecipientMail.add((String)oHistoryVariable.getValue());
                                            }
                                            
                                        }
                                    }
                                }
                            }
                        }
                        LOG.info("mTaskParam in checkTaskOnEscalation is {}", mTaskParam);
                        
                        boolean bSheduleContinule = true;
                        
                        LOG.info("sNameScheduler is {}", mTaskParam.get("sNameScheduler"));
                        
                        if((mTaskParam.get("sNameScheduler") != null && sJobName!= null)
                            && !(((String)mTaskParam.get("sNameScheduler")).equals(sJobName)))
                        {
                            bSheduleContinule = false;
                        }  
                        
                        LOG.info("bSheduleContinule is {}", bSheduleContinule);
                        if(bSheduleContinule && mTaskParam.get("sEscalationType") != null && ((String)mTaskParam.get("sEscalationType")).contains("submit")){
                            //in case if rule was made to submit
                            LOG.info("submit started");
                            String sKey_Step = oDocumentStepService.getActiveStepName((String)mTaskParam.get("sProcessInstanceId"));
                            String sProcessInstanceId_Submit = (String)mTaskParam.get("sProcessInstanceId");
                            LOG.info("sKey_Step is {}", sKey_Step);
                            LOG.info("sProcessInstanceId_Submit is {}", sProcessInstanceId_Submit);
                            
                            if(sKey_Step != null && oDocumentStepDao.getDocumentStepByID_ProcessAndName(sProcessInstanceId_Submit, sKey_Step) != null){
                                runtimeService.setVariable(sProcessInstanceId_Submit, sKey_Step + "_autoSubmit", true);
                                runtimeService.setVariable(sProcessInstanceId_Submit, "sLogin_LastSubmited", "kermit");
                                runtimeService.setVariable(sProcessInstanceId_Submit, "bAuthorEdit", "false");
                                taskService.complete((String)mTaskParam.get("sTaskId"));
                                setEscalationInHistory(mTaskParam, "submit");
                                oProcessLinkService.syncProcessLinks(sProcessInstanceId_Submit, null);
                            }
                        }
                        
                        if(bSheduleContinule &&(!mTaskParam.containsKey("sEscalationType") || ((String)mTaskParam.get("sEscalationType")).contains("sendMail"))) {
                            //in case if rule was made to send mail
                            LOG.info("send mail started");
                            String[] asRecipientMail_ToArray = new String[asRecipientMail.size()]; 
                            
                            if(!sPatternFile.contains("||")){
                                oEscalationHandler.execute(mTaskParam, asRecipientMail.toArray(asRecipientMail_ToArray), sPatternFile.trim(), null);
                            }else{
                                //then in rule was added a mail-head (last parameter of execute-function)
                                String[] asPatternFile = sPatternFile.split("\\|\\|");
                                if(asPatternFile.length > 1){
                                    oEscalationHandler.execute(mTaskParam, asRecipientMail.toArray(asRecipientMail_ToArray), asPatternFile[0].trim(), asPatternFile[1].trim());
                                }
                            }
                        }
                        
                        if(bSheduleContinule && mTaskParam.get("sEscalationType") != null && ((String)mTaskParam.get("sEscalationType")).contains("setUrgent")){
                            String sProcessInstanceId = (String)mTaskParam.get("sProcessInstanceId");
                            List<HistoricVariableInstance> aHistoricVariableInstance = historyService.createHistoricVariableInstanceQuery()
                                            .processInstanceId(sProcessInstanceId).list();
                            for(HistoricVariableInstance oHistoricVariableInstance : aHistoricVariableInstance){
                                if(oHistoricVariableInstance.getVariableName().startsWith("sKey_Step"))
                                {
                                    String sKey_Step_Escalation = (String)oHistoricVariableInstance.getValue();
                                    DocumentStep oDocumentStep = oDocumentStepService.getDocumentStep(sProcessInstanceId, sKey_Step_Escalation);
                                    List<String> asLogin_Step = new ArrayList<>();
                                    
                                    for(DocumentStepSubjectRight oDocumentStepSubjectRight : oDocumentStep.aDocumentStepSubjectRight()){
                                        asLogin_Step.add(oDocumentStepSubjectRight.getsKey_GroupPostfix());
                                    }
                                    
                                    Boolean bChatSeting = true;
                                    
                                    Optional<ProcessChat> oProcessChat_Optional = oProcessChatDao.findBy("nID_Process_Activiti", Long.parseLong(sProcessInstanceId));
                                    if(oProcessChat_Optional.isPresent()){
                                        ProcessChat oProcessChat = oProcessChat_Optional.get();
                                        List<ProcessChatMessage> aProcessChatMessage = oProcessChatMessageDao.findAllBy("oProcessChat", oProcessChat);
                                        LOG.info("asLogin_Step is {}", asLogin_Step);
                                        
                                        for(String sLogin_Step : asLogin_Step){
                                            for(ProcessChatMessage oProcessChatMessage : aProcessChatMessage){
                                                if(sLogin_Step.equals(oProcessChatMessage.getsKeyGroup_Author()))
                                                {
                                                    LOG.info("We don't set urgent");
                                                    Optional<ProcessChatMessageTree> oProcessChatMessageTree = 
                                                                oProcessChatMessageTreeDao.findBy("processChatMessageParent", oProcessChatMessage);
                                                    if(!oProcessChatMessageTree.isPresent()){
                                                           bChatSeting = false;
                                                           //break logins;
                                                    }
                                                }
                                            }
                                        }
                                        
                                    }
                                    if(bChatSeting){
                                        oDocumentStepService.setDocumentUrgent(sProcessInstanceId, sKey_Step_Escalation, null, null, true);
                                        setEscalationInHistory(mTaskParam, "setUrgent");
                                        oProcessLinkService.syncProcessLinks(sProcessInstanceId, null);
                                    }
                                    
                                    break;
                                }
                            }
                        }
                    }

                } else {
                    String sHead = String.format((oGeneralConfig.isSelfTest() ? "(TEST)" : "") + "Заявка № %s:%s!",
                            mTaskParam.get("sID_BP"),
                            mTaskParam.get("nID_task_activiti") + "");
                    LOG.info("Escalation not need! (sBeanHandler={},sHead={},sCondition={})", sBeanHandler, sHead, sCondition);
                }
            } catch (Exception e) {
                LOG.error("Can't execute hendler: {} (mTaskParam={})", e.getMessage(), mTaskParam);
                throw e;
            }
        } catch (ClassNotFoundException e) {
            //LOG.error("Error: {}, wrong parameters!", e.getMessage());
            LOG.error("Can't calculate condition, because wrong parameters: {}", e.getMessage());
            LOG.error("!!!!!!Error: ", e);
            throw e;
        } catch (ScriptException e) {
            /*LOG.error("Error: {}, wrong sCondition or parameters! (condition={}, params_json={})",
             e.getMessage(), sCondition, soData);*/
            LOG.error("Can't calculate condition, because wrong sCondition or parameters: {} (sCondition={}, soData={}, mTaskParam={})",
                    e.getMessage(), sCondition, soData, mTaskParam);
            LOG.error("!!!!!!Error: ", e);
            throw e;
        } catch (NoSuchMethodException e) {
            //LOG.error("Error: {}, error in script", e.getMessage());
            LOG.error("Can't calculate condition, because error in script: {} (sCondition={}, soData={}, mTaskParam={})",
                    e.getMessage(), sCondition, soData, mTaskParam);
            LOG.error("!!!!!!Error: ", e);
            throw e;
        } catch (Exception e) {
            //LOG.error("Error: {}, wrong parameters!", e.getMessage());
            LOG.error("Can't calculate condition, because unknown error: {} (sCondition={}, soData={}, mTaskParam={})",
                    e.getMessage(), sCondition, soData, mTaskParam);
            LOG.error("!!!!!!Error: ", e);
            throw e;
        }
    }

    private EscalationHandler getHandlerClass(String sBeanHandler) {
        EscalationHandler oEscalationHandler = (EscalationHandler) applicationContext
                .getBean(sBeanHandler);//"EscalationHandler_SendMailAlert");
        //LOG.info("Retrieved EscalationHandler component : {}", oEscalationHandler);
        return oEscalationHandler;
    }

    private Map<String, Object> parseJsonData(String soData) {
        Map<String, Object> json = (Map<String, Object>) JSON.parse(soData);
        return json;
    }
    
    private void setEscalationInHistory(Map<String, Object> mTaskParam, String sEscalationType) {
        try {
            String sProcessInstanceId = (String) mTaskParam.get("sProcessInstanceId");
            String sID_Order = oGeneralConfig.getOrderId_ByProcess(Long.parseLong(sProcessInstanceId));

            List<Task> aTask = taskService.createTaskQuery().processInstanceId(sProcessInstanceId).active().list();
            boolean bProcessClosed = aTask == null || aTask.size() == 0;
            String sUserTaskName = bProcessClosed ? "закрита" : aTask.get(0).getName();

            LOG.info("sProcessInstanceId in setEscalationInHistory {}", sProcessInstanceId);
            LOG.info("sID_Order in setEscalationInHistory {}", sID_Order);
            LOG.info("sUserTaskName in setEscalationInHistory {}", sUserTaskName);

            Map<String, String> mParam = new HashMap<>();
            mParam.put("sID_Process", sProcessInstanceId);
            mParam.put("sID_Process", sID_Order);
            mParam.put("nID_StatusType", HistoryEvent_Service_StatusType.CREATED.getnID().toString());
            mParam.put("sUserTaskName", sUserTaskName);
            mParam.put("sID_Order", sID_Order);
                
            if (sEscalationType.equals("submit")) {
                Pattern patternDate = Pattern.compile("(.+?) :: (.+)");
                Matcher matcherDate = patternDate.matcher(sUserTaskName);

                String sStatus = " ";

                while (matcherDate.find()) {
                    sStatus = matcherDate.group(1);
                }              

                if (sUserTaskName.equals("закрита")) {
                    sStatus = "документ закрито.";
                }
                mParam.put("sName", sStatus.toLowerCase());
                
                LOG.info("sStatus = {}", sStatus);
                mParam.put("nID_HistoryEventType", "52");
                oActionEventHistoryService.doRemoteRequestRegion("/wf/service/history/document/event/addHistoryEvent", mParam);
                mParam.replace("nID_HistoryEventType", "16");
                oActionEventHistoryService.doRemoteRequestRegion("/wf/service/history/document/event/addHistoryEvent", mParam);
                
                //oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 52L);
                //oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 16L);               
            } else if (sEscalationType.equals("setUrgent")) {
                mParam.put("nID_HistoryEventType", "51");
                oActionEventHistoryService.doRemoteRequestRegion("/wf/service/history/document/event/addHistoryEvent", mParam);
                //oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 51L);
            }

        } catch (Exception e) {
             LOG.info("Error in setEscalationInHistory: {}", e);
        }

    }

}
