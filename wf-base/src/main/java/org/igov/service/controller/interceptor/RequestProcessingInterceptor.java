/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.controller.interceptor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.apache.commons.mail.EmailException;
import org.igov.io.GeneralConfig;
import org.igov.io.Log;
import org.igov.io.mail.NotificationPatterns;
import org.igov.io.web.HttpRequester;
import org.igov.model.action.event.HistoryEvent_Service_StatusType;
import org.igov.model.core.GenericEntityDao;
import org.igov.model.document.*;
import org.igov.model.process.ProcessSubject;
import org.igov.model.process.ProcessSubjectDao;
import org.igov.model.server.Server;
import org.igov.model.server.ServerDao;
import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectGroupDao;
import org.igov.service.business.action.event.ActionEventHistoryService;
import org.igov.service.business.action.event.CloseTaskEvent;
import org.igov.service.business.action.event.HistoryEventService;
import org.igov.service.business.action.task.bp.handler.BpServiceHandler;
import org.igov.service.business.action.task.core.ActionTaskService;
import org.igov.service.business.action.task.core.UsersService;
import org.igov.service.business.document.DocumentStepService;
import org.igov.service.business.escalation.EscalationHistoryService;
import org.igov.service.business.process.processLink.ProcessLinkService;
import org.igov.service.exception.DocumentAccessException;
import org.igov.service.exception.ExceptionMessage;
import org.igov.service.exception.TaskAlreadyUnboundException;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author olya
 */
public class RequestProcessingInterceptor extends HandlerInterceptorAdapter implements ConstantsInterceptor, ExceptionMessage{


    private static final Logger LOG = LoggerFactory.getLogger(RequestProcessingInterceptor.class);
    private static final Logger LOG_BIG = LoggerFactory.getLogger("ControllerBig");
    
    @Autowired
    protected RuntimeService runtimeService;
    @Autowired
    GeneralConfig generalConfig;
    @Autowired
    HttpRequester httpRequester;
    @Autowired
    NotificationPatterns oNotificationPatterns;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ActionTaskService actionTaskService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private HistoryEventService historyEventService;
    @Autowired
    private EscalationHistoryService escalationHistoryService;
    @Autowired
    private CloseTaskEvent closeTaskEvent;
    @Autowired
    private ActionEventHistoryService oActionEventHistoryService;
    @Autowired
    @Qualifier("documentStepDao")
    private GenericEntityDao<Long, DocumentStep> documentStepDao;
    @Autowired
    private DocumentStepSubjectRightDao oDocumentStepSubjectRightDao;
    
    @Autowired
    private DocumentStepSubjectSignTypeDao oDocumentStepSubjectSignTypeDao;

    @Autowired
    private ProcessLinkService oProcessLinkService;

    @Autowired
    private SubjectGroupDao oSubjectGroupDao;

    @Autowired
    private UsersService usersService;
    
    @Autowired
    private DocumentStepService oDocumentStepService;
    
    @Autowired
    private DocumentStepDao oDocumentStepDao;
    
    @Autowired
    private HistoryService oHistoryService;
    
    @Autowired
    private ProcessSubjectDao oProcessSubjectDao;  
    
    @Autowired
    private ServerDao oServerDao;   
           
    
    @Value("${asID_BP_SendMail}")
    private String[] asID_BP_SendMail;
    
    private JSONParser oJSONParser = new JSONParser();

    @Override
    public boolean preHandle(HttpServletRequest oRequest,
            HttpServletResponse response, Object handler) throws Exception {

        long startTime = System.currentTimeMillis();
        LOG.info("(getMethod()={}, getRequestURL()={})", oRequest.getMethod().trim(), oRequest.getRequestURL().toString());
        LOG_BIG.info("(getMethod()={}, getRequestURL()={})", oRequest.getMethod().trim(), oRequest.getRequestURL().toString());
        oRequest.setAttribute("startTime", startTime);
        protocolize(oRequest, response, false);
        long start2 = System.currentTimeMillis();
        LOG.info("preHandle protocolize time {}", start2 - startTime);
        documentHistoryPreProcessing(oRequest, response);
        long start3 = System.currentTimeMillis();
        LOG.info("documentHistoryPreProcessing time {}", start3 - start2);
        processSubjectStatusHistoryWritingPreHandle(oRequest);
        LOG.info("processSubjectStatusHistoryWritingPreHandle time {}", System.currentTimeMillis() - start3);
        LOG.info("preHandle time {}", System.currentTimeMillis() - startTime);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest oRequest,
            HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        long start = System.currentTimeMillis();
        oRequest.setAttribute("sLogin", "testsLogin");
        //checkSessionLogin(oRequest);
        processSubjectStatusHistoryWritingPostHandle(oRequest); 
        long start2 = System.currentTimeMillis();
        LOG.info("processSubjectStatusHistoryWritingPostHandle time {}", start2 - start);
        taskHistoryProcessing(oRequest, response);
        long start3 = System.currentTimeMillis();
        LOG.info("taskHistoryProcessing time {}", start3 - start2);
        processIpayHistory(oRequest);     
        LOG.info("processIpayHistory time {}", System.currentTimeMillis() - start3);
        setSessionLogin(oRequest);
        LOG.info("postHandle time {}", System.currentTimeMillis() - start);
    }
        
    @Override
    public void afterCompletion(HttpServletRequest oRequest,
            HttpServletResponse oResponse, Object handler, Exception ex)
            throws Exception {
        long start = System.currentTimeMillis();
        oRequest.setAttribute("sLogin", "testsLogin");
        LOG.info("(nElapsedMS={})", System.currentTimeMillis() - (Long) oRequest.getAttribute("startTime"));
        LOG_BIG.info("(nElapsedMS={})", System.currentTimeMillis() - (Long) oRequest.getAttribute("startTime"));
        oResponse = ((MultiReaderHttpServletResponse) oRequest.getAttribute("responseMultiRead") != null
                ? (MultiReaderHttpServletResponse) oRequest.getAttribute("responseMultiRead") : oResponse);
        protocolize(oRequest, oResponse, true);
        documentHistoryPostProcessing(oRequest, oResponse, true);  
        LOG.info("afterCompletion time {}", System.currentTimeMillis() - start);
    }
    
    private void setSessionLogin(HttpServletRequest oRequest){
        if(isAutentification(oRequest)){
            LOG.info("isAutentification started...");
            
            Map<String, String> mRequestParam = new HashMap<>();
            Enumeration<String> paramsName = oRequest.getParameterNames();

            while (paramsName.hasMoreElements()) {
                String sKey = (String) paramsName.nextElement();
                mRequestParam.put(sKey, oRequest.getParameter(sKey));
            }
            
            LOG.info("mRequestParam is {}", mRequestParam);
            oRequest.getSession().setAttribute("sLogin", mRequestParam.get("sLogin"));
        }
    }
    
    private void checkSessionLogin(HttpServletRequest oRequest){
        HttpSession oSession = oRequest.getSession(false);
        
        if(oSession != null){
            String sSessionsLogin = (String)oRequest.getSession(false).getAttribute("sLogin");
            LOG.info("sSessionsLogin checkSessionLogin {}", sSessionsLogin);    
            if(sSessionsLogin != null){
                    //oRequest. ("sLogin", sSessionsLogin);
            }
        }
    }
    
    private void documentHistoryPostProcessing(HttpServletRequest oRequest, HttpServletResponse oResponse, boolean bFinish) {
        try {           
            checksLoginRefernt(oRequest);
            
            Map<String, String> mRequestParam = new HashMap<>();
            Enumeration<String> paramsName = oRequest.getParameterNames();

            while (paramsName.hasMoreElements()) {
                String sKey = (String) paramsName.nextElement();
                mRequestParam.put(sKey, oRequest.getParameter(sKey));
            }
            
            String sResponseBody = !bFinish ? "" : oResponse.toString();

            if (isCloseTask(oRequest, sResponseBody) || isDocumentSubmit(oRequest) || isUpdateTask(oRequest)
                    || ((mRequestParam.containsKey("sID_BP") || mRequestParam.containsKey("snID_Process_Activiti"))
                    && mRequestParam.get("sID_BP") != null && mRequestParam.get("sID_BP").startsWith("_doc"))) {

                StringBuilder osRequestBody = new StringBuilder();
                BufferedReader oReader = oRequest.getReader();
                String line;

                if (oReader != null) {
                    while ((line = oReader.readLine()) != null) {
                        osRequestBody.append(line);
                    }
                }

                String sRequestBody = osRequestBody.toString();

                String sURL = oRequest.getRequestURL().toString();

                JSONObject omRequestBody = null;
                JSONObject omResponseBody = null;

                try {
                    if (!sRequestBody.trim().equals("")) {
                        omRequestBody = (JSONObject) oJSONParser.parse(sRequestBody);
                    }
                } catch (Exception ex) {
                    LOG.info("Error parsing sRequestBody: {}", ex);
                    //LOG.info("sRequestBody is: {}", sRequestBody);
                }

                try {
                    if (!sResponseBody.trim().equals("")) {
                        omResponseBody = (JSONObject) oJSONParser.parse(sResponseBody);
                    }
                } catch (Exception ex) {
                    LOG.debug("Error parsing sResponseBody: {}", ex);
                    //LOG.debug("sRequestBody is: {}", sResponseBody);
                }

                if (isCloseTask(oRequest, sResponseBody)) {
                    if (omRequestBody != null && omRequestBody.containsKey("taskId") && mRequestParam.isEmpty()) {
                        String sTaskId = (String) omRequestBody.get("taskId");
                        LOG.info("sTaskId is: {}", sTaskId);
                        HistoricTaskInstance oHistoricTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(sTaskId).singleResult();
                        String processInstanceId = oHistoricTaskInstance.getProcessInstanceId();

                        LOG.info("oHistoricTaskInstance.getProcessDefinitionId {}", oHistoricTaskInstance.getProcessDefinitionId());

                        if (oHistoricTaskInstance.getProcessDefinitionId().startsWith("_doc_")) {

                            LOG.info("Close document is started...");
                            Map<String, String> mParam = new HashMap<>();
                            String sID_Order = generalConfig.getOrderId_ByProcess(Long.parseLong(processInstanceId));
                            mParam.put("nID_StatusType", HistoryEvent_Service_StatusType.CREATED.getnID().toString());

                            List<Task> aTask = taskService.createTaskQuery().processInstanceId(processInstanceId).active().list();

                            boolean bProcessClosed = aTask == null || aTask.size() == 0;
                            String sUserTaskName = bProcessClosed ? "закрита" : aTask.get(0).getName();

                            if (aTask.isEmpty()) {
                                oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 18L);
                            }
                        }
                    }
                }

                if (isDocumentSubmit(oRequest)) {

                    sURL = oRequest.getRequestURL().toString();
                    LOG.info("--------------------isDocumentSubmit---------------------------");
                    LOG.info("protocolize sURL is: " + sURL);
                    LOG.info("-----------------------------------------------");
                    LOG.info("sRequestBody: {}", sRequestBody);
                    LOG.info("-----------------------------------------------");
                    LOG.info("sResponseBody: {}", sResponseBody);
                    LOG.info("-----------------------------------------------");
                    LOG.info("mRequestParam {}", mRequestParam);
                    LOG.info("-----------------------------------------------");

                    if (omRequestBody != null && omRequestBody.containsKey("taskId") && mRequestParam.isEmpty()) {

                        String sTaskId = (String) omRequestBody.get("taskId");
                        LOG.info("sTaskId is: {}", sTaskId);

                        HistoricTaskInstance oHistoricTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(sTaskId).singleResult();
                        String processInstanceId = oHistoricTaskInstance.getProcessInstanceId();

                        if (oHistoricTaskInstance.getProcessDefinitionId().startsWith("_doc_")) {
                            runtimeService.setVariable(processInstanceId, "sLogin_LastSubmited", oHistoricTaskInstance.getAssignee());
                        }
                    }
                }

                if (((mRequestParam.containsKey("sID_BP") || mRequestParam.containsKey("snID_Process_Activiti"))
                        && mRequestParam.get("sID_BP") != null && mRequestParam.get("sID_BP").startsWith("_doc"))) {
                    LOG.info("--------------ALL REQUEST DOCUMENT PARAMS (POSTPROCESSING)--------------");
                    sURL = oRequest.getRequestURL().toString();
                    LOG.info("protocolize sURL is: " + sURL);
                    LOG.info("-----------------------------------------------");
                    LOG.info("sRequestBody: {}", sRequestBody);
                    LOG.info("-----------------------------------------------");
                    LOG.info("sResponseBody: {}", sResponseBody);
                    LOG.info("-----------------------------------------------");
                    LOG.info("mRequestParam {}", mRequestParam);
                    LOG.info("-----------------------------------------------");

                    String sID_Process = null;
                    //String sID_Order = null;

                    if (omResponseBody != null) {
                        sID_Process = (String) omResponseBody.get("snID_Process");
                        if (sID_Process != null) {
                            String sID_Order = generalConfig.getOrderId_ByProcess(Long.parseLong(sID_Process));
                            HistoricProcessInstance oHistoricProcessInstance
                                    = historyService.createHistoricProcessInstanceQuery().processInstanceId(sID_Process).singleResult();
                            ProcessDefinition oProcessDefinition = repositoryService.createProcessDefinitionQuery()
                                    .processDefinitionId(oHistoricProcessInstance.getProcessDefinitionId()).singleResult();
                            String sProcessName = oProcessDefinition.getName() != null ? oProcessDefinition.getName() : "";

                            List<Task> aTask = taskService.createTaskQuery().processInstanceId(sID_Process).active().list();
                            boolean bProcessClosed = aTask == null || aTask.size() == 0;
                            String sUserTaskName = bProcessClosed ? "закрита" : aTask.get(0).getName();

                            Map<String, String> mParam = new HashMap<>();

                            LOG.info("document nID_StatusType in interceptor {}", HistoryEvent_Service_StatusType.CREATED.getnID());
                            mParam.put("nID_StatusType", HistoryEvent_Service_StatusType.CREATED.getnID().toString());
                            LOG.info("document sID_Process in interceptor {}", sID_Process);
                            LOG.info("document sID_Order in interceptor {}", sID_Order);
                            LOG.info("document sUserTaskName in interceptor {}", sUserTaskName);

                            if (!(oResponse.getStatus() < 200 || oResponse.getStatus() >= 300
                                    || (sResponseBody != null && sResponseBody.contains(SYSTEM_ERR)))) {
                                if (isSetDocumentService(oRequest, sResponseBody, bFinish)) {
                                    oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 11L);
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            LOG.info("Error during document processing in interceptor: {} ", ex);
        }
    }

    private void documentHistoryPreProcessing(HttpServletRequest oRequest, HttpServletResponse oResponse) {
        try {
            checksLoginRefernt(oRequest);
            Map<String, String> mRequestParam = new HashMap<>();
            Enumeration<String> paramsName = oRequest.getParameterNames();

            while (paramsName.hasMoreElements()) {
                String sKey = (String) paramsName.nextElement();
                mRequestParam.put(sKey, oRequest.getParameter(sKey));
            }
                    
            if(isSetUrgent(oRequest)){
                LOG.info("mRequestParam in isSetUrgent {}", mRequestParam);
                Map<String, String> mParam = new HashMap<>();
                String sID_Order = generalConfig.getOrderId_ByProcess(generalConfig.getSelfServerId(), Long.parseLong(mRequestParam.get("snID_Process_Activiti")));
                mParam.put("nID_StatusType", HistoryEvent_Service_StatusType.CREATED.getnID().toString());
                
                String sInitials = "";
                if (mRequestParam.get("sKey_Group_Editor") != null) {
                    String sLogin = (String) mRequestParam.get("sKey_Group_Editor");
                    SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLogin);
                    String sFIO = oSubjectGroup.getoSubject().getsLabel();
                    sInitials = usersService.getUserInitials(sFIO);
                    mParam.put("sLogin", sInitials);
                }
                
                String sReferentInitials = "";
                String sLoginReferent = (String) mRequestParam.get("sLoginReferent");
                if (sLoginReferent != null && !sLoginReferent.isEmpty()) {                    
                    SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLoginReferent);
                    String sFIO = oSubjectGroup.getoSubject().getsLabel();
                    sReferentInitials = usersService.getUserInitials(sFIO);
                    mParam.put("sBody", sReferentInitials);
                }
                else {
                    mParam.put("sBody", sInitials);
                }
                
                //mParam.put("sLogin", mRequestParam.get("sKey_Group_Editor"));
                
                if (mRequestParam.get("bUrgent") == null){
                    oActionEventHistoryService.addHistoryEvent(sID_Order, "", mParam, 50L);
                }
                else if(Boolean.parseBoolean(mRequestParam.get("bUrgent"))){
                    oActionEventHistoryService.addHistoryEvent(sID_Order, "", mParam, 48L);
                }else if(!Boolean.parseBoolean(mRequestParam.get("bUrgent"))){
                    oActionEventHistoryService.addHistoryEvent(sID_Order, "", mParam, 49L);
                }
            }
            
            LOG.info("mRequestParam pre {}", mRequestParam);
            
            if (isUpdateTask(oRequest) || isDocumentSubmit(oRequest)) {

                StringBuilder osRequestBody = new StringBuilder();
                BufferedReader oReader = oRequest.getReader();
                String line;

                if (oReader != null) {
                    while ((line = oReader.readLine()) != null) {
                        osRequestBody.append(line);
                    }
                }

                String sRequestBody = osRequestBody.toString();
                //String sResponseBody = !bFinish ? "" : oResponse.toString();

                String sURL = oRequest.getRequestURL().toString();

                JSONObject omRequestBody = null;
                //JSONObject omResponseBody = null;

                try {
                    if (!sRequestBody.trim().equals("")) {
                        omRequestBody = (JSONObject) oJSONParser.parse(sRequestBody);
                    }
                } catch (Exception ex) {
                    LOG.error("Error parsing sRequestBody: {}", ex);
                }

                if (isUpdateTask(oRequest)) {
                    LOG.info("--------------ALL PARAMS IN SUBMIT DOCUMENT (PREPROCESSING)--------------");
                    LOG.info("protocolize sURL is: " + sURL);
                }

                if (isDocumentSubmit(oRequest)) {
                    LOG.info("--------------ALL PARAMS IN SUBMIT(REGION - PreProcessing)--------------");
                    LOG.info("protocolize sURL is: " + sURL);
                    LOG.info("-----------------------------------------------");
                    LOG.info("sRequestBody: {}", sRequestBody);
                    LOG.info("-----------------------------------------------");
                    LOG.info("-----------------------------------------------");
                    LOG.info("mRequestParam {}", mRequestParam);
                    LOG.info("-----------------------------------------------");
                    LOG.info("oRequest.getSession(false): " + oRequest.getSession(false) + " sURL: " + sURL);
                    /*if(oRequest.getSession(false) != null && oRequest.getSession(false).getAttribute("sLoginReferent") != null){
                        LOG.info("oRequest.getSession(false).getAttribute(\"sLoginReferent\"): " + oRequest.getSession(false).getAttribute("sLoginReferent") + " sURL: " + sURL);
                        mRequestParam.put("sLoginReferent", (String)oRequest.getSession(false).getAttribute("sLoginReferent"));
                    }*/
                    String sLoginAssigne = mRequestParam.get("sLoginAssigne");
                    String sKey_Step = omRequestBody != null ? (String) omRequestBody.get("sKey_Step") : null;
                    String taskId = omRequestBody != null ? String.valueOf(omRequestBody.get("taskId")) : null;

                    //блок валидации вынесен перед асайном таски
                    if (sKey_Step != null && taskId != null) {
                        String processInstanceId = historyService.createHistoricTaskInstanceQuery()
                                .taskId(taskId)
                                .singleResult()
                                .getProcessInstanceId();
                        LOG.info("Pre validation var: sKey_Step={}, taskId={}, sLoginAssigne={}, processInstanceId={}",
                                sKey_Step, taskId, sLoginAssigne, processInstanceId);
                        String sLogin = mRequestParam.get("sLogin");
                        //валидация таски
                        actionTaskService.validateTask(processInstanceId, taskId, sLogin);
                        //валидация степа
                        actionTaskService.validateDocumentStep(processInstanceId, sKey_Step);
                        //если sAssignLogin == sLogin значит автор взял документ на редактирование
                        if (!sLogin.equals(sLoginAssigne)) {
                            //валидация права на подпись
                            oDocumentStepService.validateSubmitRights(processInstanceId, sKey_Step, sLogin);
                        }
                    }
                    try {
                        if(sLoginAssigne != null && taskId != null){
                            LOG.info("sLoginAssigne is {} and taskId is {} ", sLoginAssigne, taskId);
                            taskService.setAssignee(taskId, sLoginAssigne);
                        }
                        processDocumentSubmit(mRequestParam, omRequestBody);
                    }  catch (Exception oException){
                        LOG.info("Exception during assigne {}", oException.getMessage());
                        if (taskId != null){
                            actionTaskService.unclaimUserTask(taskId);
                          }
                        throw oException;
                    }
                }
            }
        } catch (DocumentAccessException oDocumentAccessException) {
            LOG.error("Validation fail {} ", oDocumentAccessException.getMessage());
            throw oDocumentAccessException;
        } catch (Exception ex) {
            LOG.error("Error during document processing in interceptor: {} ", ex);
        }
    }

    private void processDocumentSubmit(Map<String, String> mRequestParam, JSONObject omRequestBody)
            throws Exception {
        LOG.info("processDocumentSubmit start");
        long start = System.currentTimeMillis();
        if (omRequestBody != null && omRequestBody.containsKey("taskId")) {
            String sTaskId = String.valueOf(omRequestBody.get("taskId"));
            LOG.info("sTaskId is: {}", sTaskId);          
            HistoricTaskInstance oHistoricTaskInstance = historyService.createHistoricTaskInstanceQuery()
                    .taskId(sTaskId)
                    .singleResult();          
            String processInstanceId = oHistoricTaskInstance.getProcessInstanceId();
            String sAssignLogin = oHistoricTaskInstance.getAssignee();
            LOG.info("sAssignLogin in interceptor is {}", sAssignLogin);
            LOG.info("oHistoricTaskInstance.getProcessDefinitionId {}", oHistoricTaskInstance.getProcessDefinitionId());
            LOG.info("oHistoricTaskInstance.processInstanceId {}", processInstanceId);
            
            long start2 = System.currentTimeMillis();
            LOG.info("processDocumentSubmit prepare time {}", start2 - start);
            
            Boolean bAuthorEdit = null;
            
            if (oHistoricTaskInstance.getProcessDefinitionId().startsWith("_doc_")) {
                LOG.info("We catch document submit...");
                JSONArray properties = (JSONArray) omRequestBody.get("properties");
                LOG.info("properties size {}", properties.size());
                Iterator<JSONObject> iterator = properties.iterator();
                String sKey_Step_Document = null;
                while (iterator.hasNext()) {
                    LOG.info("iterator started...");
                    JSONObject jsonObject = iterator.next();

                    String sId = null;
                    String sValue = null;

                    try {
                        sId = (String) jsonObject.get("id");
                        if (jsonObject.get("value") instanceof Long) {
                            sValue = ((Long) jsonObject.get("value")).toString();
                        } else if (jsonObject.get("value") instanceof Double) {
                            sValue = ((Double) jsonObject.get("value")).toString();
                        } else {
                            sValue = (String) jsonObject.get("value");
                        }
                    } catch (Exception ex) {
                        LOG.info("sId field exception {}", sId, ex.getMessage());
                        continue;
                    }
                    LOG.info("sId field {}", sId);

                    if (sId.equals("sKey_Step_Document")) {
                        sKey_Step_Document = sValue;
                        //break;
                    }
                    
                    if (sId.equals("bAuthorEdit")) {
                        bAuthorEdit = Boolean.parseBoolean(sValue);
                    }

                    if (sId.startsWith("sID_Order_Relation")) {

                        LOG.info("sID_Order_Relation in {}", sId);
                        String sID_Order = generalConfig.getOrderId_ByProcess(Long.parseLong(processInstanceId));
                        String nID_Task_Linked = actionTaskService.getTaskIDbyProcess(null, sValue, Boolean.FALSE).toString();

                        LOG.info("sID_Order {}", sID_Order);
                        LOG.info("nID_Task_Linked {}", nID_Task_Linked);

                        List<Task> aTask = taskService.createTaskQuery().processInstanceId(processInstanceId).active().list();
                        boolean bProcessClosed = aTask == null || aTask.isEmpty();
                        String sUserTaskName = bProcessClosed ? "закрита" : aTask.get(0).getName();

                        Map<String, String> mParam = new HashMap<>();
                        mParam.put("new_BP_ID",
                                (taskService.createTaskQuery().taskId(nID_Task_Linked).list().get(0).getProcessDefinitionId()).split(":")[0]);
                        mParam.put("sID_Order_Link", sValue);
                        mParam.put("nID_StatusType", HistoryEvent_Service_StatusType.CREATED.getnID().toString());
                        LOG.info("mParam 1-st {}", mParam);
                        oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 29L);

                        mParam.replace("new_BP_ID", aTask.get(0).getProcessDefinitionId().split(":")[0]);
                        mParam.replace("sID_Order_Link", sID_Order);

                        LOG.info("mParam 2-nd {}", mParam);
                        oActionEventHistoryService.addHistoryEvent(sValue, sUserTaskName, mParam, 30L);
                    }
                }
                LOG.info("sKey_Step_Document is {}", sKey_Step_Document);
                long start3 = System.currentTimeMillis();
                LOG.info("big loop time {}", start3 - start2);

                if (sKey_Step_Document != null) {
                    String sLogin = mRequestParam.get("sLogin");
                    
                    List<DocumentStep> aDocumentStep = documentStepDao.findAllBy("snID_Process_Activiti", processInstanceId);
                    LOG.info("aDocumentStep in interceptor is {}", aDocumentStep);

                    DocumentStep oCurrDocumentStep = null;
                    DocumentStep oCommonDocumentStep = null;
                    
                    for (DocumentStep oDocumentStep : aDocumentStep) {
                        if (oDocumentStep.getsKey_Step().equals(sKey_Step_Document)) {
                            oCurrDocumentStep = oDocumentStep;
                        }
                        if (oDocumentStep.getsKey_Step().equals("_")) {
                            oCommonDocumentStep = oDocumentStep;
                            //break;
                        }
                    }
                    LOG.info("oCurrDocumentStep in interceptor is {}", oCurrDocumentStep);

                    List<Group> aUserGroup = identityService.createGroupQuery().groupMember(sAssignLogin).list();
                    LOG.info("aUserGroup is {}", aUserGroup);
                    //runtimeService.setVariable(executionId, "sLogin_LastSubmited", sAssignLogin);
                    if (oCurrDocumentStep != null && oCommonDocumentStep != null) {
                        List<DocumentStepSubjectRight> aDocumentStepSubjectRight = oCurrDocumentStep.aDocumentStepSubjectRight();
                        //aDocumentStepSubjectRight.addAll(oCommonDocumentStep.aDocumentStepSubjectRight());
                        taskService.setVariable(sTaskId, "sLogin_LastSubmited", sLogin);
                        
                        for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight) {
                            for (Group oGroup : aUserGroup) {
                                LOG.info("oGroup name: {}", oGroup.getName());
                                LOG.info("oGroup id: {}", oGroup.getId());
                                //oGroup.getId().equals(sLogin) - in case of refernt user on the step
                                if (oGroup.getId().equals(sLogin) && oGroup.getId().equals(oDocumentStepSubjectRight.getsKey_GroupPostfix())) {
                                    List<User> aUser = identityService.createUserQuery().memberOfGroup(oDocumentStepSubjectRight.getsKey_GroupPostfix()).list();
                                    LOG.info("oDocumentStepSubjectRight.getsKey_GroupPostfix {}", oDocumentStepSubjectRight.getsKey_GroupPostfix());
                                    for (User oUser : aUser) {
                                            LOG.info("oUser id is {}", oUser.getId());
                                        if (oUser.getId().equals(sAssignLogin)) {
                                            
                                            String sLoginReferent;
                                            if(mRequestParam.get("sLoginReferent") != null){
                                                sLoginReferent = mRequestParam.get("sLoginReferent");
                                            } else{
                                                sLoginReferent = sAssignLogin;
                                            }      
                                            LOG.info("bAuthorEdit before setting date {}", bAuthorEdit);
                                            
                                            if(bAuthorEdit == null || bAuthorEdit == false){
                                                LOG.info("We set date for login: {}", sAssignLogin);
                                                oDocumentStepSubjectRight.setsDate(new DateTime());
                                                oDocumentStepSubjectRight.setsLogin(sLoginReferent);  
                                                runtimeService.setVariable(processInstanceId, "bUrgent", null);
                                                runtimeService.setVariable(processInstanceId, "sLogin_LastSubmited", sLogin);

                                                String sID_SignType = mRequestParam.get("sName_DocumentStepSubjectSignType");
                                                LOG.info("sID: {}", sID_SignType);                                            
                                                if(sID_SignType == null){
                                                    sID_SignType = "sign";
                                                }           

                                                DocumentStepSubjectSignType oDocumentStepSubjectSignType = oDocumentStepSubjectSignTypeDao.findByExpected("sID", sID_SignType);                                                                                           
                                                oDocumentStepSubjectRight.setoDocumentStepSubjectSignType(oDocumentStepSubjectSignType);
                                            }
                                            
                                            oDocumentStepSubjectRight.setbUrgent(null);
                                            oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubjectRight);
                                            
                                            break;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        LOG.info("oCurrDocumentStep.getRights() in interceptor is {}", oCurrDocumentStep.aDocumentStepSubjectRight());
                        Map<String, String> mParam = new HashMap<>();
                        //String sID_Order = generalConfig.getOrderId_ByProcess(Long.parseLong(processInstanceId)); нигде не используется закомментировал
                        SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sAssignLogin);
                        String sName = oSubjectGroup.getoSubject().getsLabel();
                        mParam.put("nID_StatusType", HistoryEvent_Service_StatusType.CREATED.getnID().toString());
                        mParam.put("sLoginNew", sAssignLogin);
                        mParam.put("sName", sName);
                    }
                    oProcessLinkService.syncProcessLinks(processInstanceId, sLogin);
                }
            }
        }
        LOG.info("processDocumentSubmit time {}", System.currentTimeMillis() - start);
    }

    private void protocolize(HttpServletRequest oRequest, HttpServletResponse oResponse, boolean bFinish)
            throws IOException, TaskAlreadyUnboundException, Exception {
        
        LOG.info("Method 'protocolize' started");
        int nLen = generalConfig.isSelfTest() ? 300 : 200;
        checksLoginRefernt(oRequest);
        Map<String, String> mRequestParam = new HashMap<>();
        Enumeration<String> paramsName = oRequest.getParameterNames();
        while (paramsName.hasMoreElements()) {
            String sKey = (String) paramsName.nextElement();
            mRequestParam.put(sKey, oRequest.getParameter(sKey));
        }
                
        StringBuilder osRequestBody = new StringBuilder();
        BufferedReader oReader = oRequest.getReader();
        String line;
        if (oReader != null) {
            while ((line = oReader.readLine()) != null) {
                osRequestBody.append(line);
            }
        }
        String sURL = oRequest.getRequestURL().toString();
        LOG.info("protocolize sURL is: " + sURL);
        String snTaskId = null;
        //getting task id from URL, if URL matches runtime/tasks/{taskId} (#1234)
        String sRequestBody = osRequestBody.toString();
        LOG.info("oRequest.getRequestURL(): " + oRequest.getRequestURL() + " oRequest.getMethod(): " + oRequest.getMethod());
        if (TAG_PATTERN_PREFIX.matcher(oRequest.getRequestURL()).find()) {
            snTaskId = sURL.substring(sURL.lastIndexOf("/") + 1);
            LOG.info("snTaskId: " + snTaskId);
            LOG.info("Request.getMethod().trim(): " + oRequest.getMethod().trim());
            if (PUT.equalsIgnoreCase(oRequest.getMethod().trim()) && SREQUESTBODY_PATTERN.matcher(sRequestBody).find()) {
                LOG.info("URL is like runtime/tasks/{taskId}, getting task id from url, task id is " + snTaskId);
                Task task = taskService.createTaskQuery().taskId(snTaskId).singleResult();
                if (task != null && task.getAssignee() != null) {
                    LOG.info("task.getAssignee(): " + task.getAssignee());
                    throw new TaskAlreadyUnboundException(HttpStatus.FORBIDDEN + " Вибачте, звернення вже взято в роботу");
                }
            }
        }
        if (snTaskId != null && mRequestParam.get("taskId") == null) {
            mRequestParam.put("taskId", snTaskId);
        }

        if (!bFinish) {
            //LOG.info("(mRequestParam={})", mRequestParam);
            //LOG.info("(sRequestBody={})", sCut(nLen, sRequestBody));
            if (sURL.endsWith("/service/document/setDocumentFile")
                    || sURL.contains("/service/object/file/")) {
            } else {
               // LOG_BIG.debug("(sRequestBody={})", sRequestBody);
            }
        }

        String sResponseBody = !bFinish ? null : oResponse.toString();
        if (bFinish) {
            //LOG.info("(sResponseBody={})", sCut(nLen, sResponseBody));
            if (sURL.endsWith(SERVICE_ACTION_ITEM_GET_SERVICE)
                    || sURL.endsWith(SERVICE_ACTION_ITEM_GET_SERVICES_TREE)
                    || (sURL.endsWith(SERVICE_FORM_FORM_DATA)
                    && "GET".equalsIgnoreCase(oRequest.getMethod().trim()))
                    || sURL.endsWith(SERVICE_REPOSITORY_PROCESS_DEFINITIONS)
                    || sURL.endsWith(SERVICE_ACTION_TASK_GET_START_FORM_DATA)
                    || sURL.endsWith(SERVICE_ACTION_TASK_GET_ORDER_MESSAGES_LOCAL)
                    || sURL.endsWith(SERVICE_ACTION_FLOW_GET_FLOW_SLOTS_SERVICE_DATA)
                    || sURL.contains(SERVICE_RUNTIME_TASKS)
                    || sURL.endsWith(SERVICE_HISTORY_HISTORIC_TASK_INSTANCES)
                    || sURL.endsWith(SERVICE_ACTION_TASK_GET_LOGIN_B_PS)
                    || sURL.endsWith(SERVICE_SUBJECT_MESSAGE_GET_MESSAGES)
                    || sURL.endsWith(SERVICE_SUBJECT_MESSAGE_GET_SERVICE_MESSAGES)
                    || sURL.endsWith(SERVICE_OBJECT_PLACE_GET_PLACES_TREE)
                    || sURL.endsWith(SERVICE_ACTION_EVENT_GET_LAST_TASK_HISTORY)
                    || sURL.endsWith(SERVICE_ACTION_EVENT_GET_HISTORY_EVENTS_SERVICE)
                    || sURL.endsWith(SERVICE_ACTION_EVENT_GET_HISTORY_EVENTS)
                    || sURL.endsWith(SERVICE_DOCUMENT_GET_DOCUMENT_CONTENT)
                    || sURL.endsWith(SERVICE_DOCUMENT_GET_DOCUMENT_FILE)
                    || sURL.endsWith(SERVICE_DOCUMENT_GET_DOCUMENTS)
                    || sURL.endsWith(SERVICE_DOCUMENT_SET_DOCUMENT_FILE)
                    || sURL.contains(SERVICE_OBJECT_FILE)
                    || sURL.contains(SERVICE_DOCUMENT_GET_DOCUMENT_ABSTRACT)) {
            } else {
                //LOG_BIG.debug("(sResponseBody={})", sResponseBody);
            }
        }
        String sType = "";

        try {
            LOG.info("URL: {} method: {}", oRequest.getRequestURL(), oRequest.getMethod());
            if (!bFinish || !(oResponse.getStatus() >= HttpStatus.OK.value()
                    && oResponse.getStatus() < HttpStatus.BAD_REQUEST.value())) {
                LOG.info("returning from protocolize block: bSaveHistory:{} oResponse.getStatus():{}", bFinish, oResponse.getStatus());
            }

            //LOG.info("isSaveTask(oRequest, sResponseBody): " + isSaveTask(oRequest, sResponseBody));
            //LOG.info("oRequest.getRequestURL: " + oRequest.getRequestURL().toString());
            //LOG.info("sResponseBody before SaveTask: " + sResponseBody);
            if (isSaveTask(oRequest, sResponseBody, bFinish)) {
                sType = "Save";
                LOG.info("saveNewTaskInfo block started");
                LOG.info("oRequest body {}", sRequestBody);
                LOG.info("sResponseBody {}", sResponseBody);
                if (oResponse.getStatus() < 200 || oResponse.getStatus() >= 300
                        || (sResponseBody != null && sResponseBody.contains(SYSTEM_ERR))) { //SYSTEM_ERR
                    try {
                        new Log(this.getClass(), LOG)//this.getClass()
                                ._Case("Activiti_FailStartTask")
                                ._Status(Log.LogStatus.ERROR)
                                ._Head("Error hapened while start process!")
                                ._Body(oResponse.toString())
                                //._Param("sRequestBody", sRequestBody)
                                //._Param("sResponseBody", sResponseBody)
                                ._Param("mRequestParam", mRequestParam)
                                .save();
                    } catch (Exception ex) {
                        LOG.error("Can't save error to MSG", ex.getMessage());
                    }
                    return;
                } else {
                    //LOG.info("sRequestBody {}", sRequestBody);
                    //LOG.info("sResponseBody {}", sRequestBody);
                    //LOG.info("mRequestParam {}", sRequestBody);
                    saveNewTaskInfo(sRequestBody, sResponseBody, mRequestParam);
                }
                //{nID_Service=25, nID_Subject=255289, nID_ServiceData=542, sID_BP=dms_0025_ID2 545_iGov:1:1, sID_UA=1210100000}
                LOG.info("saveNewTaskInfo block finished");
            } else if (isCloseTask(oRequest, sResponseBody)) {
                LOG.info("saveClosedTaskInfo block started");
                List<String> aTaskId = new ArrayList<>();

                if (oRequest.getRequestURL().toString().indexOf(SERVICE_CANCELTASK) > 0) {
                    LOG.info("We catch cancel task...");
                    //LOG.info("mRequestParam {}", mRequestParam);
                    String nID_Order = mRequestParam.get("nID_Order");
                    LOG.info("nID_Order {}", nID_Order);

                    aTaskId = actionTaskService.getTaskIdsByProcessInstanceId(
                            actionTaskService.getOriginalProcessInstanceId(Long.parseLong(nID_Order)));

                    List<Task> aTask = taskService.createTaskQuery().processInstanceId(
                            actionTaskService.getOriginalProcessInstanceId(Long.parseLong(nID_Order))).active().list();
                    boolean bProcessClosed = aTask == null || aTask.size() == 0;
                    String sUserTaskName = bProcessClosed ? "закрита" : aTask.get(0).getName();
                    LOG.info("sUserTaskName in close event is {}", sUserTaskName);
                    //for(String taskId : aTaskId){
                    LOG.info("taskId {}", aTaskId.get(aTaskId.size() - 1));
                    Map<String, String> mParam = new HashMap<>();
                    String sID_Order = generalConfig.getOrderId_ByOrder(generalConfig.getSelfServerId(), Long.parseLong(nID_Order));
                    LOG.info("sID_Order for cancel flowslot {}", sID_Order);
                    mParam.put("nID_StatusType", HistoryEvent_Service_StatusType.CREATED.getnID().toString());
                    closeTaskEvent.doWorkOnCloseTaskEvent(bFinish, aTaskId.get(aTaskId.size() - 1), null, true);
                    //oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 19L);
                }
                sType = "Close";
                if (aTaskId.isEmpty()) {
                    saveClosedTaskInfo(sRequestBody, snTaskId, bFinish);
                }
                LOG.info("saveClosedTaskInfo block finished");
            } else if (isUpdateTask(oRequest)) {
                sType = "Update";
                LOG.info("saveUpdatedTaskInfo block started");
                //LOG.info("oRequest URL: {}", oRequest.getRequestURL().toString());
                //LOG.info("oRequest mRequestParam: {}", mRequestParam);
                saveUpdatedTaskInfo(sResponseBody, mRequestParam);
                LOG.info("saveUpdatedTaskInfo block finished");
            }
        } catch (Exception oException) {
            LOG_BIG.error("Can't save service-history record: {}", oException.getMessage());
        }
    }

    /**
     * сохранение информации таска
     *
     * @param sRequestBody
     * @param sResponseBody
     * @param mParamRequest
     * @throws Exception
     */
    private void saveNewTaskInfo(String sRequestBody, String sResponseBody, Map<String, String> mParamRequest)
            throws Exception {

        LOG.info("saveNewTaskInfo started in " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));

        LOG.info("sRequestBody {}", sRequestBody);// 
        LOG.info("sResponseBody {}", sResponseBody);
        LOG.info("mParamRequest {}", mParamRequest);
        if (sResponseBody == null) {
            //LOG.warn("sResponseBody=null!!! (sRequestBody={},mParamRequest={})", sRequestBody, mParamRequest);
        }
        Map<String, String> mParam = new HashMap<>();
        //LOG.info("sRequestBody {}", sRequestBody);
        JSONObject omRequestBody = (JSONObject) oJSONParser.parse(sRequestBody);

        // LOG.info("omRequestBody >>>>>>>>>>>>>> {}", omRequestBody );
        JSONArray properties = (JSONArray) omRequestBody.get("properties");
        //  LOG.info("properties >>>>>>>>>>>>>> {}", properties );

        if (properties == null) {
            properties = (JSONArray) omRequestBody.get("aFormProperty");
        }

        Iterator<JSONObject> iterator = properties.iterator();
        String sID_Public_SubjectOrganJoin = null;

        while (iterator.hasNext()) {
            JSONObject jsonObject = iterator.next();

            if (jsonObject.get("value") instanceof java.lang.String) {
                String sId = (String) jsonObject.get("id");
                String sValue = (String) jsonObject.get("value");

                if (sId.equals("sID_Public_SubjectOrganJoin")) {
                    sID_Public_SubjectOrganJoin = sValue;
                    break;
                }
            }
        }
        LOG.info("RequestProcessingInterceptor sID_Public_SubjectOrganJoin: " + sID_Public_SubjectOrganJoin);
        mParam.put("sID_Public_SubjectOrganJoin", sID_Public_SubjectOrganJoin);

        JSONObject omResponseBody = (JSONObject) oJSONParser.parse(sResponseBody);
        mParam.put("nID_StatusType", HistoryEvent_Service_StatusType.CREATED.getnID().toString());

        //String osnID_Process = omResponseBody.containsKey("id"); //разобраться чего получаем нал в некоторых случаях
        String snID_Process = String.valueOf(omResponseBody.containsKey("id") ? omResponseBody.get("id") : omResponseBody.get("snID_Process")); //разобраться чего получаем нал в некоторых случаях
        //if(snID_Process) //{"snID_Process":"23285433","nID_Task":"23285483"}
        if (sRequestBody != null && sRequestBody.contains("sCancelInfo")) {
            runtimeService.setVariable(snID_Process, "sCancelInfo", String.format("Заявка актуальна"));
        }

        if (snID_Process != null && !"null".equalsIgnoreCase(snID_Process)) {
            Long nID_Process = Long.valueOf(snID_Process);
            LOG.info("snID_Process please be here: " + snID_Process);
            String sID_Order = generalConfig.getOrderId_ByProcess(nID_Process);
            //String snID_Subject = String.valueOf(omRequestBody.get("nID_Subject"));
            String snID_Subject = String.valueOf(omRequestBody.containsKey("nID_Subject") ? omRequestBody.get("nID_Subject") : mParamRequest.get("nID_Subject"));
            mParam.put("nID_Subject", snID_Subject);

            LOG.info("(sID_Order={},nID_Subject={})", sID_Order, snID_Subject);

            String snID_Service = mParamRequest.get("nID_Service");
            LOG.info("nID_Service in RequestProcessingInterceptor: " + snID_Service);
            if (snID_Service != null) {
                mParam.put("nID_Service", snID_Service);
            }

            String sID_UA = mParamRequest.get("sID_UA");
            if (sID_UA != null) {
                mParam.put("sID_UA", sID_UA);
            }

            LOG.info("RequestProcessingInterceptor sID_UA: " + sID_UA);

            //TODO: need remove in future
            String snID_Region = mParamRequest.get("nID_Region");
            if (snID_Region != null) {
                LOG.info("nID_Region in saveNewTaskInfo is {}", snID_Region);
                mParam.put("nID_Region", snID_Region);
            }

            LOG.info("RequestProcessingInterceptor snID_Region: " + snID_Region);

            String snID_ServiceData = mParamRequest.get("nID_ServiceData");
            if (snID_ServiceData != null) {
                mParam.put("nID_ServiceData", snID_ServiceData);
            }

            LOG.info("RequestProcessingInterceptor snID_ServiceData: " + snID_ServiceData);

            HistoricProcessInstance oHistoricProcessInstance
                    = historyService.createHistoricProcessInstanceQuery().processInstanceId(snID_Process).singleResult();
            ProcessDefinition oProcessDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(oHistoricProcessInstance.getProcessDefinitionId()).singleResult();
            String sProcessName = oProcessDefinition.getName() != null ? oProcessDefinition.getName() : "";
            //mParam.put("sProcessInstanceName", sProcessInstanceName);
            mParam.put("sHead", sProcessName);

            List<Task> aTask = taskService.createTaskQuery().processInstanceId(snID_Process).active().list();
            boolean bProcessClosed = aTask == null || aTask.size() == 0;
            String sUserTaskName = bProcessClosed ? "закрита" : aTask.get(0).getName();//"(нет назви)"

            sendMailTo(omRequestBody, sID_Order, snID_Subject, snID_Service, oProcessDefinition);

            historyEventService.addHistoryEvent(sID_Order, sUserTaskName, mParam);
            //LOG.info("Before calling set action process count {}, {}", mParam, oProcessDefinition.getKey());
            if (oProcessDefinition.getKey().startsWith("_doc_") || DNEPR_MVK_291_COMMON_BP.contains(oProcessDefinition.getKey())) {
                //Integer count = ActionProcessCountUtils.callSetActionProcessCount(httpRequester, generalConfig, oProcessDefinition.getKey(), Long.valueOf(snID_Service));
                //LOG.info("RequestProcessInterceptor process count: " + count.intValue());
            }//2017-05-16_13:56:48.390 
        }
    }

    /**
     * сохранение информации при закрытии таски
     *
     * @param sRequestBody
     * @param snClosedTaskId
     * @param bSaveHistory
     * @throws Exception
     */
    //(#1234) added additional parameter snClosedTaskId
    private void saveClosedTaskInfo(String sRequestBody, String snClosedTaskId, boolean bSaveHistory) throws Exception {
        LOG.info("Method saveClosedTaskInfo started");

        //LOG.info("sRequestBody is {}", sRequestBody);
        LOG.info("snClosedTaskId is {}", snClosedTaskId);

        JSONObject omRequestBody = null;
        String snID_Task = null;
        try {
            omRequestBody = (JSONObject) oJSONParser.parse(sRequestBody);
            snID_Task = String.valueOf(omRequestBody.get("taskId"));
        } catch (Exception ex) {
            LOG.info("sRequestBody in saveClosedTaskInfo is unparsable {}", ex);
        }

        if ((snID_Task == null) && (snClosedTaskId != null)) {
            snID_Task = snClosedTaskId.trim();
            LOG.info("Task id from requestbody is null, so using task id from url - " + snID_Task);
        }

        LOG.info("Task id is - " + snID_Task);
        if (snID_Task != null) {
            closeTaskEvent.doWorkOnCloseTaskEvent(bSaveHistory, snID_Task, omRequestBody, false);
        }
        LOG.info("Method saveClosedTaskInfo END");
    }

    /**
     * сохранение информации при обновлении таски
     *
     * @param sResponseBody
     * @param mRequestParam
     * @throws Exception
     */
    private void saveUpdatedTaskInfo(String sResponseBody, Map<String, String> mRequestParam) throws Exception {
        Map<String, String> mParam = new HashMap<>();
        JSONObject omResponseBody = (JSONObject) oJSONParser.parse(sResponseBody);
        String snID_Task = (String) omResponseBody.get("taskId");
        if (snID_Task == null && mRequestParam.containsKey("taskId")) {
            LOG.info("snID_Task is NULL, looking for it in mRequestParam");
            snID_Task = (String) mRequestParam.get("taskId");
            LOG.info("Found taskId in mRequestParam {}", snID_Task);
        }

        LOG.info("Looking for a task with ID {}", snID_Task);

        HistoricTaskInstance oHistoricTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(snID_Task)
                .singleResult();

        mParam.put("sUserTaskName", oHistoricTaskInstance.getName());
        String snID_Process = oHistoricTaskInstance.getProcessInstanceId();
        closeEscalationProcessIfExists(snID_Process);
        Long nID_Process = Long.valueOf(snID_Process);
        String sSubjectInfo = mRequestParam.get("sSubjectInfo");
        if (sSubjectInfo != null) {
            mParam.put("sSubjectInfo", sSubjectInfo);
        }
        if (mRequestParam.get("nID_Subject") != null) {
            String nID_Subject = String.valueOf(mRequestParam.get("nID_Subject"));
            mParam.put("nID_Subject", nID_Subject);
        }
        LOG_BIG.info("mParams: {}", mParam.toString());
        String sID_Order = generalConfig.getOrderId_ByProcess(nID_Process);
        LOG.info("(sID_Order={})", sID_Order);
        historyEventService.updateHistoryEvent(sID_Order, HistoryEvent_Service_StatusType.OPENED_ASSIGNED, mParam);
        LOG.info("historyEventService.updateHistoryEvent finished");
        updateEscalationState(oHistoricTaskInstance, nID_Process);
    }

    /**
     *
     * @param omRequestBody
     * @param sID_Order
     * @param snID_Subject
     * @param snID_Service
     * @param oProcessDefinition
     * @throws ParseException
     * @throws EmailException
     */
    public void sendMailTo(JSONObject omRequestBody, String sID_Order, String snID_Subject, String snID_Service,
            ProcessDefinition oProcessDefinition) throws ParseException, EmailException {                
        
        String sMailTo = JsonRequestDataResolver.getEmail(omRequestBody);        
        
        String sPhone = String.valueOf(JsonRequestDataResolver.getPhone(omRequestBody));
        String bankIdFirstName = JsonRequestDataResolver.getBankIdFirstName(omRequestBody);
        String bankIdLastName = JsonRequestDataResolver.getBankIdLastName(omRequestBody);  

        int nID_Server = generalConfig.getSelfServerId();
        LOG.info("nID_Server in sendMailTo in interceptor is ", nID_Server);

        if (sMailTo != null) {            
            if (Arrays.asList(asID_BP_SendMail).contains(oProcessDefinition.getKey())) {
                ActionProcessCountUtils.callSetActionProcessCount(httpRequester, generalConfig, oProcessDefinition.getKey(), Long.valueOf(snID_Service));
                LOG.info("Before send notification mail... (sMailTo={}, oProcessDefinition.getKey()={})", sMailTo, oProcessDefinition.getKey());
                oNotificationPatterns.sendTaskCreatedInfoEmail(sMailTo, sID_Order, bankIdFirstName, bankIdLastName);
                LOG.info("Send notification mail... (sMailTo={}, oProcessDefinition.getKey()={})", sMailTo, oProcessDefinition.getKey());
            } else {
                LOG.info("SKIP send notification mail... (sMailTo={}, oProcessDefinition.getKey()={})", sMailTo, oProcessDefinition.getKey());
            }
        }

        if (sMailTo != null || sPhone != null) {
            try {
                Map<String, String> mParamSync = new HashMap<String, String>();
                mParamSync.put("snID_Subject", snID_Subject);
                mParamSync.put("sMailTo", sMailTo);
                mParamSync.put("sPhone", sPhone);
                LOG.info("Вносим параметры в коллекцию (sMailTo {}, snID_Subject {}, sPhone {})", sMailTo, snID_Subject, sPhone);
                String sURL = generalConfig.getSelfHostCentral() + URI_SYNC_CONTACTS;
                LOG.info("Подключаемся к центральному порталу by sURL: " + sURL);
                String sResponse = httpRequester.getInside(sURL, mParamSync);
                LOG.info("Подключение осуществлено.. sResponse is: " + sResponse);
            } catch (Exception ex) {
                LOG.warn("(isSaveTask exception {})", ex.getMessage());
            }

        }
    }

    /**
     * Обновление статуса ескалации
     *
     * @param oHistoricTaskInstance
     * @param nID_Process
     */
    public void updateEscalationState(HistoricTaskInstance oHistoricTaskInstance, Long nID_Process) {
        String sProcessName = oHistoricTaskInstance.getProcessDefinitionId();
        try {
            LOG.info("Update escalation history... (sProcessName={})", sProcessName);
            if (sProcessName.indexOf(BpServiceHandler.PROCESS_ESCALATION) == 0) {//issue 981
                escalationHistoryService
                        .updateStatus(nID_Process, EscalationHistoryService.STATUS_IN_WORK);//Long.valueOf(sID_Process)
            } else { //issue 1297
                LOG.trace("BpServiceHandler.PROCESS_ESCALATION = {}", BpServiceHandler.PROCESS_ESCALATION);
            }
        } catch (Exception oException) {
            new Log(oException, LOG)//this.getClass()
                    ._Case("IC_UpdateEscalation")
                    ._Status(Log.LogStatus.ERROR)
                    ._Head("Can't update escalation history")
                    ._Param("nID_Process", nID_Process)
                    ._LogTrace()
                    .save();
        }
    }

    private boolean isUpdateTask(HttpServletRequest oRequest) {
        return (oRequest.getRequestURL().toString().indexOf(RUNTIME_TASKS) > 0
                && PUT.equalsIgnoreCase(oRequest.getMethod().trim()))
                || oRequest.getRequestURL().toString().indexOf("action/task/updateProcess") > 0
                && POST.equalsIgnoreCase(oRequest.getMethod().trim());
    }

    private boolean isCloseTask(HttpServletRequest oRequest, String sResponseBody) {
        return POST.equalsIgnoreCase(oRequest.getMethod().trim())
                && (((sResponseBody == null || "".equals(sResponseBody))
                && (oRequest.getRequestURL().toString().indexOf(FORM_FORM_DATA) > 0
                || oRequest.getRequestURL().toString().indexOf(UPDATE_PROCESS) > 0))
                || TAG_PATTERN_PREFIX.matcher(oRequest.getRequestURL()).find()
                || (oRequest.getRequestURL().toString().indexOf(SERVICE_CANCELTASK) > 0));
    }

    private boolean isSaveTask(HttpServletRequest oRequest, String sResponseBody, boolean bFinish) {
        return (bFinish && sResponseBody != null && !"".equals(sResponseBody))
                //&& oRequest.getRequestURL().toString().indexOf(FORM_FORM_DATA) > 0
                && ((oRequest.getRequestURL().toString().indexOf(FORM_FORM_DATA) > 0
                || oRequest.getRequestURL().toString().indexOf(UPDATE_PROCESS) > 0)
                || oRequest.getRequestURL().toString().indexOf(START_PROCESS) > 0)
                && POST.equalsIgnoreCase(oRequest.getMethod().trim());
    }

    private boolean isDocumentSubmit(HttpServletRequest oRequest) {
        return (oRequest != null && (oRequest.getRequestURL().toString().indexOf(FORM_FORM_DATA) > 0
                || oRequest.getRequestURL().toString().indexOf(UPDATE_PROCESS) > 0)
                && POST.equalsIgnoreCase(oRequest.getMethod().trim()));
    }

    private boolean isUpdateProcess(HttpServletRequest oRequest) {
        return (oRequest != null && oRequest.getRequestURL().toString().indexOf("task/updateProcess") > 0
                && POST.equalsIgnoreCase(oRequest.getMethod().trim()));
    }

    private boolean isSetDocumentService(HttpServletRequest oRequest, String sResponseBody, boolean bFinish) {
        boolean isNewDocument = (bFinish && sResponseBody != null && !"".equals(sResponseBody))
                && oRequest.getRequestURL().toString().indexOf(DOCUMENT_SERVICE) > 0
                && GET.equalsIgnoreCase(oRequest.getMethod().trim());

        if (isNewDocument) {
            LOG.info("We catch document in requestProcessingInterceptor! Yippie-Kai-Yay!");
        }

        return isNewDocument;
    }

    protected void closeEscalationProcessIfExists(String sID_Process) {
        closeTaskEvent.closeEscalationProcessIfExists(sID_Process);
    }

    private boolean isSetProcessSubjectStatus(HttpServletRequest oRequest) {

        return (oRequest != null && oRequest.getRequestURL().toString().indexOf(SERVICE_SUBJECT_PROCESS_SET_PROCESS_SUBJECT_STATUS) > 0
                && POST.equalsIgnoreCase(oRequest.getMethod().trim()));
    }

    private void processSubjectStatusHistoryWritingPreHandle(HttpServletRequest oRequest) throws Exception {
        try {
            if (isSetProcessSubjectStatus(oRequest)) {
                LOG.info("processSubjectStatusHistoryWritingPreHandle started.");                
                checksLoginRefernt(oRequest);
                
                Map<String, String> mRequestParam = new HashMap<>();
                Enumeration<String> paramsName = oRequest.getParameterNames();

                while (paramsName.hasMoreElements()) {
                    String sKey = (String) paramsName.nextElement();
                    mRequestParam.put(sKey, oRequest.getParameter(sKey));
                }
                
                LOG.info("taskHistoryProcessing mRequestParam: {}", mRequestParam);

                JsonParser parser = new JsonParser();

                String sBody = oRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                LOG.info("sBody={}", sBody);
                JsonObject jsonBody = parser.parse(sBody).getAsJsonObject();   
               
                JsonObject jsonQueryParams = jsonBody.get("queryParams").getAsJsonObject();

                String sID_ProcessSubjectStatus = jsonQueryParams.get("sID_ProcessSubjectStatus").getAsString();
                String snID_Task_Activiti = jsonQueryParams.get("snID_Task_Activiti").getAsString();
                String sLoginController = jsonQueryParams.get("sLoginController") == null
                        ? null : jsonQueryParams.get("sLoginController").getAsString();
                String sLoginExecutor = jsonQueryParams.get("sLoginExecutor") == null
                        ? null : jsonQueryParams.get("sLoginExecutor").getAsString();
                LOG.debug("snID_Task_Activiti={}, sID_ProcessSubjectStatus={}, sLoginController={},"
                        + "sLoginExecutor={}", snID_Task_Activiti, sID_ProcessSubjectStatus, sLoginController,
                        sLoginExecutor);
                String sText = jsonQueryParams.get("sText") == null
                        ? null : jsonQueryParams.get("sText").getAsString();;
                String sDatePlaneNew = jsonQueryParams.get("sDatePlaneNew") == null
                        ? null : jsonQueryParams.get("sDatePlaneNew").getAsString();;

                boolean isReferent = false;
                String sReferentInitials = "";
                String sLoginReferent = (String) mRequestParam.get("sLoginReferent");
                if (sLoginReferent != null && !sLoginReferent.isEmpty()) {
                    isReferent = true;
                    SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLoginReferent);
                    String sFIO = oSubjectGroup.getoSubject().getsLabel();
                    sReferentInitials = usersService.getUserInitials(sFIO);
                }
                /**
                 * Определяем кто вызвал сервис (исполнитель или контролирующий). Пришел только логин sLoginExecutor -
                 * исполнитель, пришел только логин sLoginController - контролирующий, если пришло два логина -
                 * контролирующий.
                 */
                String sLoginMain = sLoginController;
                String sLoginRoleMain = "Controller";
                if (sLoginExecutor != null && sLoginController == null) {
                    sLoginMain = sLoginExecutor;
                    sLoginRoleMain = "Executor";
                }

                if (sLoginRoleMain.equals("Executor") || sLoginRoleMain.equals("Controller")) {
                    HistoricTaskInstance oHistoricTaskInstance = historyService.createHistoricTaskInstanceQuery()
                            .taskId(snID_Task_Activiti)
                            .singleResult();
                    String sProcessInstanceId = oHistoricTaskInstance.getProcessInstanceId();

                    String sID_Order = generalConfig.getOrderId_ByProcess(Long.parseLong(sProcessInstanceId));

                    SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLoginMain);
                    String sName = oSubjectGroup.getoSubject().getsLabel();

                    List<Task> aTask = taskService.createTaskQuery()
                            .processInstanceId(sProcessInstanceId)
                            .active()
                            .list();
                    boolean bProcessClosed = aTask == null || aTask.isEmpty();
                    //проверка, чтобы выбрать таску по ид, который пришел в запросе
                    String sUserTaskName = bProcessClosed
                            ? "закрита"
                            : aTask.stream().filter(oTask
                                    -> oTask.getId().equals(snID_Task_Activiti)).findFirst().toString();

                    JSONObject oTransportObject = new JSONObject();

                    oTransportObject.put("sLoginRole", sLoginRoleMain);
                    oTransportObject.put("sProcessInstanceId", sProcessInstanceId);
                    oTransportObject.put("sID_Order", sID_Order);
                    oTransportObject.put("sUserTaskName", sUserTaskName);
                    oTransportObject.put("sName", sName);
                    oTransportObject.put("sLogin", sLoginMain);
                    oTransportObject.put("sID_ProcessSubjectStatus", sID_ProcessSubjectStatus);
                    oTransportObject.put("sText", sText);
                    oTransportObject.put("sDatePlaneNew", sDatePlaneNew); 
                    oTransportObject.put("snID_Task_Activiti", snID_Task_Activiti); 
                    oTransportObject.put("sReferentInitials", sReferentInitials); 
                    LOG.info("oTransportObject={}", oTransportObject);

                    oRequest.setAttribute("oTransportObject", oTransportObject);
                }
            }
        } catch (Exception oException) {
            LOG.error("Error during writing processSubjectStatusHistory in interceptor: {}", oException);
        }
    }

    private void processSubjectStatusHistoryWritingPostHandle(HttpServletRequest oRequest) throws Exception {
        LOG.info("processSubjectStatusHistoryWritingPostHandle start.");
        try {
            checksLoginRefernt(oRequest);

            if (isSetProcessSubjectStatus(oRequest)) {
                Map<String, Object> mRequestAttribute = new HashMap<>();
                Enumeration<String> aAttributeName = oRequest.getAttributeNames();

                while (aAttributeName.hasMoreElements()) {
                    String sKey = (String) aAttributeName.nextElement();
                    mRequestAttribute.put(sKey, oRequest.getAttribute(sKey));
                }                              
                
                JSONObject oTransportObject = (JSONObject) mRequestAttribute.get("oTransportObject");

                String sLoginRole = (String) oTransportObject.get("sLoginRole");
                String sID_Order = (String) oTransportObject.get("sID_Order");
                String sUserTaskName = (String) oTransportObject.get("sUserTaskName");
                String sName = (String) oTransportObject.get("sName");
                String sLogin = (String) oTransportObject.get("sLogin");
                String sID_ProcessSubjectStatus = (String) oTransportObject.get("sID_ProcessSubjectStatus");
                String sText = (String) oTransportObject.get("sText");
                String sDatePlaneNew = (String) oTransportObject.get("sDatePlaneNew");                                  
                String snID_Task_Activiti = (String) oTransportObject.get("snID_Task_Activiti");
                String sProcessInstanceId = (String) oTransportObject.get("sProcessInstanceId"); 
                String sReferentInitials = (String) oTransportObject.get("sReferentInitials");
                
                 
                String sAuthorInitials = "";
                if (!sName.isEmpty()) {
                    sAuthorInitials = usersService.getUserInitials(sName);
                }
                
                Map<String, String> mParam = new HashMap<>();
                
                if (sReferentInitials.isEmpty()){
                    mParam.put("sLoginNew", sAuthorInitials);
                }
                else {
                    mParam.put("sLoginNew", sReferentInitials);
                }
                                                
                
                mParam.put("nID_StatusType", HistoryEvent_Service_StatusType.CREATED.getnID().toString());
                
                mParam.put("sName", sAuthorInitials);
                mParam.put("sID_Process", sProcessInstanceId);
                
                String sTemplateDoc = "/documents/sID_Order=";
                String sTemplateFile = "/api/tasks/download/";
                String sTemplateFileName = "/attachment/Mongo/";
                String sAnswer = " ";
                
                Long nID = generalConfig.getSelfServerId().longValue();
                
                Server oServer = oServerDao.findByIdExpected(nID);
                String sURL = oServer.getsURL();
                LOG.info("sURL: {}", sURL);                
                 
                ProcessSubject oProcessSubject = oProcessSubjectDao.findByExpected("snID_Task_Activiti", snID_Task_Activiti);
                String sTextType = oProcessSubject.getsTextType();                        
                LOG.info("sTextType: {}", sTextType);
                               
                if (sID_ProcessSubjectStatus.equals("executed") && sLoginRole.equals("Executor")) {
                    if (sText != null) {
                        if (sTextType.equals("string")) {
                            String sBody = sURL + sTemplateDoc + sText;
                            sAnswer = "<a href=\"" + sBody + "\">" + sBody + "</a>";
                        } else if (sTextType.equals("textArea")) {
                            sAnswer = sText;
                        } else if (sTextType.equals("file")) {
                            JsonParser parser = new JsonParser();
                            JsonObject jsonFile = parser.parse(sText).getAsJsonObject();
                            LOG.info("jsonFile: {}", jsonFile);
                            String sKey = jsonFile.get("sKey").getAsString();
                            LOG.info("sKey: {}", sKey);
                            String sFileNameAndExt = jsonFile.get("sFileNameAndExt").getAsString();
                            LOG.info("sFileNameAndExt: {}", sFileNameAndExt);
                            String sBody = sURL + sTemplateFile + sKey + sTemplateFileName + sFileNameAndExt;
                            sAnswer = "<a href=\"" + sBody + "\" target=\"_blank\" download>" + sBody + "</a>";
                        } else {
                            sAnswer = sText;
                        }
                    }
                    mParam.put("sBody", sAnswer);
                    oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 20L);
                } else if (sID_ProcessSubjectStatus.equals("notExecuted") && sLoginRole.equals("Executor")) {
                    mParam.put("sBody", sText);
                    oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 21L);
                } else if (sID_ProcessSubjectStatus.equals("unactual") && sLoginRole.equals("Executor")) {
                    mParam.put("sBody", sText);
                    oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 22L);
                } else if (sID_ProcessSubjectStatus.equals("requestTransfered") && sLoginRole.equals("Executor")) {                    
                    mParam.put("newData", sDatePlaneNew);
                    oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 23L);
                } else if (sID_ProcessSubjectStatus.equals("transfered") && sLoginRole.equals("Controller")) {
                    mParam.put("newData", sDatePlaneNew);
                    oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 24L);
                } else if (sID_ProcessSubjectStatus.equals("rejected") && sLoginRole.equals("Controller")) {
                    mParam.put("sBody", sText);
                    oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 25L);
                } else if (sID_ProcessSubjectStatus.equals("executed") && sLoginRole.equals("Controller")) {
                    oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 26L);
                } else if (sID_ProcessSubjectStatus.equals("notExecuted") && sLoginRole.equals("Controller")) {
                    mParam.put("sBody", sText);
                    oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 27L);
                } else if (sID_ProcessSubjectStatus.equals("unactual") && sLoginRole.equals("Controller")) {
                    oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 28L);
                }
            }
        } catch (Exception oException) {
            LOG.error("Error during writing processSubjectStatusHistory in interceptor: {}", oException);
        }
    }
    
    private void checksLoginRefernt(HttpServletRequest oRequest) throws Exception
    {
        
        Map<String, String> mRequestParam = new HashMap<>();
        Enumeration<String> paramsName = oRequest.getParameterNames();
        while (paramsName.hasMoreElements()) {
            String sKey = (String) paramsName.nextElement();
            mRequestParam.put(sKey, oRequest.getParameter(sKey));
        }
        
        if(mRequestParam.containsKey("sLogin") && mRequestParam.containsKey("sLoginReferent")){
           String sLogin = (String) mRequestParam.get("sLogin");
           String sLoginReferent = (String) mRequestParam.get("sLoginReferent");
           LOG.info("checksLoginRefernt sLogin: {} sLoginReferent: {}", sLogin, sLoginReferent);
           if(sLoginReferent != null && !sLoginReferent.equals(sLogin)){
                Set<String> asGroup = usersService.getUserGroupMember(sLoginReferent);
               
                if(!asGroup.contains(sLogin)){
                    throw new RuntimeException(ACCESS_DENIED);
                }
           }
        }
    }       

    private void taskHistoryProcessing(HttpServletRequest oRequest, HttpServletResponse oResponse) {
        LOG.info("Welcome to taskHistoryProcessing in interceptor");
        try {           
            long start = System.currentTimeMillis();
            checksLoginRefernt(oRequest);

            Map<String, String> mRequestParam = new HashMap<>();
            Enumeration<String> paramsName = oRequest.getParameterNames();

            while (paramsName.hasMoreElements()) {
                String sKey = (String) paramsName.nextElement();
                mRequestParam.put(sKey, oRequest.getParameter(sKey));
            }

            String sResponseBody = " ";
            if (oResponse != null) {
                sResponseBody = oResponse.toString();
            }

            StringBuilder osRequestBody_test = new StringBuilder();
            BufferedReader oReader_test = oRequest.getReader();
            String sline;

            if (oReader_test != null) {
                while ((sline = oReader_test.readLine()) != null) {
                    osRequestBody_test.append(sline);
                }
            }

            LOG.info("taskHistoryProcessing sResponseBody: {}", sResponseBody);
            LOG.info("taskHistoryProcessing mRequestParam: {}", mRequestParam);           

            if (isSetMessage(oRequest) || isEditMessage(oRequest) || isAddAcceptor(oRequest) || isAddViewer(oRequest)
                    || isAddVisor(oRequest) || isDelegate(oRequest) || isCancelSign(oRequest) || isUpdateProcess(oRequest)
                    || isRemoveDocumentStepSubject(oRequest)) {

                StringBuilder osRequestBody = new StringBuilder();
                BufferedReader oReader = oRequest.getReader();
                String line;

                if (oReader != null) {
                    while ((line = oReader.readLine()) != null) {
                        osRequestBody.append(line);
                    }
                }

                String sRequestBody = osRequestBody.toString();
                String sURL = oRequest.getRequestURL().toString();

                JSONObject omRequestBody = null;
                JSONObject omResponseBody = null;

                try {
                    if (!sRequestBody.trim().equals("")) {
                        omRequestBody = (JSONObject) oJSONParser.parse(sRequestBody);
                    }
                } catch (Exception ex) {
                    LOG.info("Error parsing sRequestBody: {}", ex);
                }

                try {
                    if (!sResponseBody.trim().equals("")) {
                        omResponseBody = (JSONObject) oJSONParser.parse(sResponseBody);
                    }
                } catch (Exception ex) {
                    LOG.debug("Error parsing sResponseBody: {}", ex);
                }

                LOG.info("protocolize sURL is: " + sURL);
                LOG.info("sRequestBody: {}", sRequestBody);
                LOG.info("sResponseBody: {}", sResponseBody);
                LOG.info("mRequestParam {}", mRequestParam);

                String sID_Process = null;

                Map<String, String> mParam = new HashMap<>();
                List<Task> aTask = new ArrayList<Task>();
                String sUserTaskName = " ";

                LOG.info("task nID_StatusType in interceptor {}", HistoryEvent_Service_StatusType.CREATED.getnID());
                mParam.put("nID_StatusType", HistoryEvent_Service_StatusType.CREATED.getnID().toString());               

                if (mRequestParam != null) {

                    if (mRequestParam.get("nID_Process_Activiti") != null) {
                        sID_Process = String.valueOf(mRequestParam.get("nID_Process_Activiti"));
                    } else if (mRequestParam.get("snID_Process_Activiti") != null) {
                        sID_Process = (String) mRequestParam.get("snID_Process_Activiti");
                    } else if (omRequestBody.get("nID_Process") != null) {
                        sID_Process = String.valueOf(omRequestBody.get("nID_Process"));
                    }

                    if (sID_Process != null) {
                        mParam.put("sID_Process", sID_Process);
                        String sID_Order = generalConfig.getOrderId_ByProcess(Long.parseLong(sID_Process));
                        mParam.put("sID_Order", sID_Order);

                        aTask = taskService.createTaskQuery().processInstanceId(sID_Process).active().list();
                        boolean bProcessClosed = aTask == null || aTask.size() == 0;
                        sUserTaskName = bProcessClosed ? "закрита" : aTask.get(0).getName();

                        LOG.info("task sID_Process in interceptor {}", sID_Process);
                        LOG.info("task sID_Order in interceptor {}", sID_Order);
                        LOG.info("task sUserTaskName in interceptor {}", sUserTaskName);
                        
                        boolean isReferent = false;
                        String sReferentInitials = "";
                        String sLoginReferent = (String) mRequestParam.get("sLoginReferent");
                        if (sLoginReferent != null && !sLoginReferent.isEmpty()){
                            isReferent = true;
                            SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLoginReferent);
                            String sFIO = oSubjectGroup.getoSubject().getsLabel();
                            sReferentInitials = usersService.getUserInitials(sFIO);
                        }

                        if (isSetMessage(oRequest) || isEditMessage(oRequest)) {
                            if (omRequestBody.get("sBody") != null) {
                                mParam.put("sBody", omRequestBody.get("sBody").toString());
                            }
                            if (mRequestParam.get("sLogin") != null) {
                                String sLogin = (String) mRequestParam.get("sLogin");
                                SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLogin);
                                String sFIO = oSubjectGroup.getoSubject().getsLabel();
                                String sInitials = usersService.getUserInitials(sFIO);                                
                                mParam.put("sName", sInitials);
                                
                                if(isReferent){
                                    mParam.put("sLoginNew", sReferentInitials);
                                }
                                else {
                                    mParam.put("sLoginNew", sInitials);
                                }
                            }
                        }

                        if (isAddAcceptor(oRequest) || isAddViewer(oRequest) || isAddVisor(oRequest) || isDelegate(oRequest)) {
                            if (mRequestParam.get("sKey_Group_Delegate") != null) {
                                String sLoginNew = (String) mRequestParam.get("sKey_Group_Delegate");
                                SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLoginNew);
                                String sFIO = oSubjectGroup.getoSubject().getsLabel();
                                String sInitials = usersService.getUserInitials(sFIO);                                
                                mParam.put("sLogin", sInitials);                               
                                mParam.put("sLoginNew", sInitials);                                
                            }
                            if (mRequestParam.get("sKey_Group") != null) {
                                String sLogin = (String) mRequestParam.get("sKey_Group");
                                SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLogin);
                                String sFIO = oSubjectGroup.getoSubject().getsLabel();
                                String sInitials = usersService.getUserInitials(sFIO);                                
                                mParam.put("sName", sInitials);
                                
                                if(isReferent){
                                    mParam.put("sBody", sReferentInitials);
                                }
                                else {
                                    mParam.put("sBody", sInitials);
                                }
                            }
                        }  
                        
                        if(isRemoveDocumentStepSubject(oRequest)){
                            if (mRequestParam.get("sLogin") != null) {
                                String sLoginNew = (String) mRequestParam.get("sLogin");
                                SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLoginNew);
                                String sFIO = oSubjectGroup.getoSubject().getsLabel();
                                String sInitials = usersService.getUserInitials(sFIO);                               
                                mParam.put("sName", sInitials);                                                                 
                                
                                if(isReferent){
                                    mParam.put("sBody", sReferentInitials);
                                }
                                else {
                                    mParam.put("sBody", sInitials);
                                }
                            }
                            if (mRequestParam.get("sKey_Group") != null) {
                                String sLogin = (String) mRequestParam.get("sKey_Group");
                                SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLogin);
                                String sFIO = oSubjectGroup.getoSubject().getsLabel();
                                String sInitials = usersService.getUserInitials(sFIO);                                
                                mParam.put("sLogin", sInitials);
                            }
                        }

                        if (isUpdateProcess(oRequest)) {                           
                            
                            if (mRequestParam.get("sName_DocumentStepSubjectSignType") != null && !mRequestParam.get("sName_DocumentStepSubjectSignType").equals("sign")) {
                                if (mRequestParam.get("sLogin") != null) {
                                    String sLogin = (String) mRequestParam.get("sLogin");
                                    SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLogin);
                                    String sFIO = oSubjectGroup.getoSubject().getsLabel();
                                    String sInitials = usersService.getUserInitials(sFIO);
                                    LOG.info("sLoginNew: {}", sLogin);
                                    mParam.put("sName", sInitials);

                                    if (isReferent) {
                                        mParam.put("sLoginNew", sReferentInitials);
                                    } else {
                                        mParam.put("sLoginNew", sInitials);
                                    }
                                }
                                String StepType = (String) mRequestParam.get("sName_DocumentStepSubjectSignType");
                                LOG.info("StepType: {}", StepType);
                                if (StepType.equals("refuse")) {
                                    oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 39L);
                                } else if (StepType.equals("needlessly")) {
                                    oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 38L);
                                } else if (StepType.equals("seen")) {
                                    mParam.put("sBody", "ознайомлено");
                                    oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 41L);
                                }
                            } else if (omRequestBody.get("sKey_Step") != null && omRequestBody.get("properties") != null) {

                                String sSignType = " ";
                                String sKeyStep = omRequestBody.get("sKey_Step").toString();
                                DocumentStepType oDocumentStepType = null;
                                String sLogin = (String) mRequestParam.get("sLogin");

                                SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLogin);
                                String sFIO = oSubjectGroup.getoSubject().getsLabel();
                                String sInitials = usersService.getUserInitials(sFIO);                                
                                mParam.put("sName", sInitials);
                                
                                if(isReferent){
                                    mParam.put("sLoginNew", sReferentInitials);
                                }
                                else {
                                    mParam.put("sLoginNew", sInitials);
                                }

                                List<DocumentStep> aDocumentSteps = oDocumentStepDao.getStepForProcess(sID_Process);

                                JSONArray aProperties = (JSONArray) omRequestBody.get("properties");
                                Iterator<JSONObject> iterator = aProperties.iterator();

                                boolean bAuthor = false;

                                while (iterator.hasNext()) {
                                    JSONObject jsonObject = iterator.next();
                                    String sId = (String) jsonObject.get("id");

                                    if (sId.equals("bAuthorEdit")) {
                                        Map<String, Object> mProcessVariable = new HashMap<>();
                                        HistoricProcessInstance oProcessInstance = oHistoryService.createHistoricProcessInstanceQuery()
                                                .processInstanceId(sID_Process.trim()).includeProcessVariables().singleResult();

                                        mProcessVariable = oProcessInstance.getProcessVariables();

                                        String sLoginAuthor = mProcessVariable.containsKey("sLoginAuthor")
                                                ? (String) mProcessVariable.get("sLoginAuthor") : null;
                                        LOG.info("sLoginAuthor: {}", sLoginAuthor);

                                        if (sLogin.equalsIgnoreCase(sLoginAuthor)) {
                                            bAuthor = true;
                                        }
                                    }
                                }
                                for (DocumentStep oDocumentStep : aDocumentSteps) {
                                    if (oDocumentStep.getsKey_Step().equals(sKeyStep)) {
                                        oDocumentStepType = oDocumentStep.getoDocumentStepType();

                                        sSignType = oDocumentStepType.getsSing();
                                        LOG.info("sSignType: {}", sSignType);

                                        mParam.put("sBody", sSignType.toLowerCase());

                                        if (!bAuthor || (bAuthor && sSignType.equalsIgnoreCase("Відредаговано"))) {
                                            oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 41L);
                                        }
                                    }
                                }
                            }
                            
                            if (omRequestBody.get("properties") != null) {
                                org.json.simple.JSONObject jsonObj = (org.json.simple.JSONObject) new JSONParser().parse(omRequestBody.toString());
                                String nID_Task = null;
                                if (jsonObj.containsKey("taskId")) {
                                    nID_Task = jsonObj.get("taskId").toString();
                                } else {
                                    LOG.info("Variable \"taskId\" not found");
                                }
                                LOG.info("taskId = " + nID_Task);

                                Pattern patternDate = Pattern.compile("(.+?) :: (.+)");
                                Matcher matcherDate = patternDate.matcher(sUserTaskName);

                                String sStatus = " ";

                                while (matcherDate.find()) {
                                    sStatus = matcherDate.group(1);
                                }
                                LOG.info("sStatus = {}", sStatus);
                                
                                if(sUserTaskName.equals("закрита")){
                                    sStatus = "документ закрито.";
                                }
                                
                                boolean isTask = false;
                                
                                if (omRequestBody.get("aProcessSubjectTask") != null) {
                                    JSONArray aProcessSubjectTask = (JSONArray) omRequestBody.get("aProcessSubjectTask");
                                    LOG.info("aProcessSubjectTask: {}", aProcessSubjectTask);
                                    Iterator<JSONObject> i = aProcessSubjectTask.iterator();

                                    while (i.hasNext()) {
                                        JSONObject objectJSON = i.next();
                                        String sID_BP = (String) objectJSON.get("sID_BP");
                                        LOG.info("sID_BP: {}", sID_BP);

                                        if (sID_BP.startsWith("_task")) {
                                            isTask = true;
                                        }
                                    }
                                }   

                                mParam.put("sName", sStatus.toLowerCase());
                                if (!isTask) {
                                    oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 16L);
                                }
                            }
                        }

                        if (isSetMessage(oRequest)) {
                            oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 31L);
                        } else if (isEditMessage(oRequest)) {
                            oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 32L);                        
                        } else if (isAddAcceptor(oRequest)) {
                            oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 33L);
                        } else if (isAddViewer(oRequest)) {
                            oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 34L);
                        } else if (isAddVisor(oRequest)) {
                            oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 35L);
                        } else if (isDelegate(oRequest)) {
                            oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 36L);
                        } else if (isRemoveDocumentStepSubject(oRequest)) {
                            oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 53L);
                         } else if (isCancelSign(oRequest)) {
                            LOG.info("isCancelSign started");
                            if (mRequestParam.get("sKey_Group") != null) {
                                String snID_Process_Activiti = (String)mRequestParam.get("snID_Process_Activiti");
                                String sKeyStep = (String)mRequestParam.get("sKeyStep");
                                DocumentStep oDocumentStep = oDocumentStepService.getDocumentStep(snID_Process_Activiti, sKeyStep);
                                
                                LOG.info("oDocumentStep is {}", oDocumentStep.getsKey_Step());
                                String sLogin = (String) mRequestParam.get("sKey_Group");
                                SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLogin);
                                String sFIO = oSubjectGroup.getoSubject().getsLabel();
                                String sInitials = usersService.getUserInitials(sFIO);                                
                                mParam.put("sName", sInitials);

                                if(isReferent){
                                    mParam.put("sLoginNew", sReferentInitials);
                                }
                                else {
                                    mParam.put("sLoginNew", sInitials);
                                }
                                
                                for(DocumentStepSubjectRight oDocumentStepSubjectRight : oDocumentStep.aDocumentStepSubjectRight()){
                                    if(oDocumentStepSubjectRight.getsKey_GroupPostfix().equals(sLogin) 
                                            && !oDocumentStepSubjectRight.getoDocumentStepSubjectSignType().getsID().equals("needlessly"))
                                    {
                                      LOG.info("addHistoryEvent was started");  
                                      oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 37L);  
                                    }
                                }
                            }
                        }
                    } else if (omRequestBody.get("properties") != null && isUpdateProcess(oRequest)) {
                        JSONArray aProperties = (JSONArray) omRequestBody.get("properties");
                        LOG.info("properties: {}", aProperties);
                        Iterator<JSONObject> iterator = aProperties.iterator();

                        while (iterator.hasNext()) {
                            JSONObject jsonObject = iterator.next();

                            String sId = (String) jsonObject.get("id");

                            LOG.info("sId field {}", sId);

                            if (sId.equals("processInstanceId")) {
                                sID_Process = String.valueOf(jsonObject.get("value"));
                                LOG.info("sID_Process {}", sID_Process);
                                mParam.put("sID_Process", sID_Process);

                                String sID_Order = generalConfig.getOrderId_ByProcess(Long.parseLong(sID_Process));
                                mParam.put("sID_Order", sID_Order);
                                LOG.info("task sID_Order in interceptor {}", sID_Order);

                                aTask = taskService.createTaskQuery().processInstanceId(sID_Process).active().list();
                                boolean bProcessClosed = aTask == null || aTask.size() == 0;
                                sUserTaskName = bProcessClosed ? "закрита" : aTask.get(0).getName();
                                LOG.info("task sUserTaskName in interceptor {}", sUserTaskName);

                                JSONArray aProcessSubjectTask = (JSONArray) omRequestBody.get("aProcessSubjectTask");
                                LOG.info("aProcessSubjectTask: {}", aProcessSubjectTask);
                                Iterator<JSONObject> i = aProcessSubjectTask.iterator();

                                while (i.hasNext()) {
                                    JSONObject objectJSON = i.next();
                                    String sActionType = (String) objectJSON.get("sActionType");
                                    LOG.info("sActionType: {}", sActionType);

                                    if (sActionType.equals("set")) {
                                        JSONArray aProcessSubject = (JSONArray) objectJSON.get("aProcessSubject");
                                        LOG.info("aProcessSubject: {}", aProcessSubject);
                                        Iterator<JSONObject> j = aProcessSubject.iterator();
                                        if (j.hasNext()) {
                                            JSONObject ObjectJSON = j.next();
                                            String sDatePlan = (String) ObjectJSON.get("sDatePlan");
                                            LOG.info("sDatePlan: {}", sDatePlan);
                                            mParam.put("newData", sDatePlan);
                                            oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 40L);
                                        }
                                    }
                                }
                            }
                        }                        
                    }
                }
            }
            long start5 = System.currentTimeMillis();
            LOG.info("all time task processing: {}", start5 - start);
        } catch (Exception ex) {
            LOG.info("Error during task processing in interceptor: {}", ex.getMessage());
        }
    }
    
    private void processIpayHistory(HttpServletRequest oRequest){
        try{
            Map<String, String> mRequestParam = new HashMap<>();
            Enumeration<String> paramsName = oRequest.getParameterNames();

            while (paramsName.hasMoreElements()) {
                String sKey = (String) paramsName.nextElement();
                mRequestParam.put(sKey, oRequest.getParameter(sKey));
            }

            LOG.info("mRequestParam in processIpayHistory {}", mRequestParam);
            String sProcessInstanceId = mRequestParam.get("processInstanceId");
            if(sProcessInstanceId != null){
                List<HistoricVariableInstance> aHistoricVariableInstance = historyService.createHistoricVariableInstanceQuery()
                                            .processInstanceId(sProcessInstanceId).list();
                
                HistoricVariableInstance oHistoricVariableInstance_asPayResult = null;
                HistoricVariableInstance oHistoricVariableInstance_bHistoryIpayMessage = null;
                HistoricVariableInstance oHistoricVariableInstance_bPayResult = null;
                HistoricVariableInstance oHistoricVariableInstance_sID_Pay_MasterPass = null;

                for(HistoricVariableInstance oHistoricVariableInstance : aHistoricVariableInstance){
                    
                    if(oHistoricVariableInstance.getVariableName().equals("asPayResult")){
                        oHistoricVariableInstance_asPayResult = oHistoricVariableInstance;
                    }
                    
                    if(oHistoricVariableInstance.getVariableName().equals("bHistoryIpayMessage")){
                        oHistoricVariableInstance_bHistoryIpayMessage = oHistoricVariableInstance;
                    }
                    
                    if(oHistoricVariableInstance.getVariableName().equals("bPayResult")){
                        oHistoricVariableInstance_bPayResult = oHistoricVariableInstance;
                    }
                    
                    if(oHistoricVariableInstance.getVariableName().equals("sID_Pay_MasterPass")){
                        oHistoricVariableInstance_sID_Pay_MasterPass = oHistoricVariableInstance;
                    }
                }
                
                if(oHistoricVariableInstance_asPayResult != null && oHistoricVariableInstance_bPayResult != null && oHistoricVariableInstance_sID_Pay_MasterPass != null 
                        && oHistoricVariableInstance_bHistoryIpayMessage == null)
                {
                      
                    if(oHistoricVariableInstance_asPayResult.getValue() != null){
                        
                        org.activiti.engine.impl.util.json.JSONObject oResponseParams = 
                                new org.activiti.engine.impl.util.json.JSONObject((String)oHistoricVariableInstance_asPayResult.getValue());
                        
                        org.activiti.engine.impl.util.json.JSONObject oRequestBody =
                                new org.activiti.engine.impl.util.json.JSONObject((String)oHistoricVariableInstance_sID_Pay_MasterPass.getValue());
                        
                        String pmt_status = (String)((org.activiti.engine.impl.util.json.JSONObject)oResponseParams.get("response")).get("pmt_status");
                        String amount = ((String)((org.activiti.engine.impl.util.json.JSONObject)oResponseParams.get("response")).get("amount"));
                        
                        if(amount != null){
                            Map<String, String> mParam = new HashMap<>();
                            
                            Double amount_fix = (double)Integer.parseInt(amount)/100;
                            DecimalFormat decim = new DecimalFormat("0.00");
                            mParam.put("sIpay_Amount", decim.format(amount_fix));
                            
                            mParam.put("sID_Order", generalConfig.getOrderId_ByProcess(generalConfig.getSelfServerId(), Long.parseLong(sProcessInstanceId)));
                            mParam.put("nID_StatusType", HistoryEvent_Service_StatusType.CREATED.getnID().toString());
                            
                            String user_id = null;
                            
                            if(oRequestBody.get("user_id") instanceof Integer){
                                user_id = ((Integer)oRequestBody.get("user_id")).toString();
                            }else if(oRequestBody.get("user_id") instanceof String){
                                user_id = ((String)oRequestBody.get("user_id"));
                            }
                            
                            mParam.put("nID_Subject", user_id);
                            
                            mParam.put("sUserTaskName", "");

                            if(pmt_status.equals("5")){
                                mParam.put("nID_HistoryEventType", "46");
                                oActionEventHistoryService.doRemoteRequest("/wf/service/history/document/event/addHistoryEvent", mParam);
                                runtimeService.setVariable(sProcessInstanceId, "bHistoryIpayMessage", "true");
                            }
                            if(pmt_status.equals("9") || pmt_status.equals("4")){
                                mParam.put("nID_HistoryEventType", "47");
                                oActionEventHistoryService.doRemoteRequest("/wf/service/history/document/event/addHistoryEvent", mParam);
                                runtimeService.setVariable(sProcessInstanceId, "bHistoryIpayMessage", "true");
                            }
                        }
                    }
                }
            }
        }catch (Exception ex){
            LOG.info("Error during processIpayHistory {}", ex);
        }
    }

    private boolean isSetMessage(HttpServletRequest oRequest) {
        return (oRequest != null && oRequest.getRequestURL().toString().indexOf(SET_CHAT_MESSAGE) > 0
                && POST.equalsIgnoreCase(oRequest.getMethod().trim()));
    }
    
    private boolean isEditMessage(HttpServletRequest oRequest) {
        return (oRequest != null && oRequest.getRequestURL().toString().indexOf(EDIT_CHAT_MESSAGE) > 0
                && PUT.equalsIgnoreCase(oRequest.getMethod().trim()));
    }
    
    private boolean isAddAcceptor(HttpServletRequest oRequest) {
        return (oRequest != null && oRequest.getRequestURL().toString().indexOf(ADD_ACCEPTOR) > 0
                && GET.equalsIgnoreCase(oRequest.getMethod().trim()));
    }
    
    private boolean isAddViewer(HttpServletRequest oRequest) {
        return (oRequest != null && oRequest.getRequestURL().toString().indexOf(ADD_VIEWER) > 0
                && GET.equalsIgnoreCase(oRequest.getMethod().trim()));
    }
    
    private boolean isAddVisor(HttpServletRequest oRequest) {
        return (oRequest != null && oRequest.getRequestURL().toString().indexOf(ADD_VISOR) > 0
                && GET.equalsIgnoreCase(oRequest.getMethod().trim()));
    }
    
    private boolean isDelegate(HttpServletRequest oRequest) {
        return (oRequest != null && oRequest.getRequestURL().toString().indexOf(DELEGATE) > 0
                && GET.equalsIgnoreCase(oRequest.getMethod().trim()));
    }
    private boolean isCancelSign(HttpServletRequest oRequest) {
        return (oRequest != null && oRequest.getRequestURL().toString().indexOf(CANCEL_SIGN) > 0
                && GET.equalsIgnoreCase(oRequest.getMethod().trim()));
    }             
    
    private boolean isAutentification(HttpServletRequest oRequest) {
        return (oRequest != null && oRequest.getRequestURL().toString().indexOf(URI_DASHBOARD_ENTER) > 0
                && POST.equalsIgnoreCase(oRequest.getMethod().trim()));
    }
    
    private boolean isSetUrgent(HttpServletRequest oRequest) {
        return (oRequest != null && oRequest.getRequestURL().toString().indexOf(URI_URGENT_DOCUMENT) > 0
                && GET.equalsIgnoreCase(oRequest.getMethod().trim()));
    }

    private boolean isRemoveDocumentStepSubject(HttpServletRequest oRequest) {
        return (oRequest != null && oRequest.getRequestURL().toString().indexOf(SERVICE_DOCUMENT_REMOVE_DOCUMENT_STEP_SUBJECT) > 0
                && GET.equalsIgnoreCase(oRequest.getMethod().trim()));
    }
}
