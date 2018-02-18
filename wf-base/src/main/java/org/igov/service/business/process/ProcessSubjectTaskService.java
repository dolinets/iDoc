package org.igov.service.business.process;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.igov.io.GeneralConfig;
import org.igov.io.db.kv.statical.IBytesDataStorage;
import org.igov.io.db.kv.temp.exception.RecordInmemoryException;
import org.igov.model.document.DocumentStep;
import org.igov.model.document.DocumentStepDao;
import org.igov.model.document.DocumentStepSubjectRight;
import org.igov.model.process.*;
import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectGroupDao;
import org.igov.service.business.action.event.ActionEventHistoryService;
import org.igov.service.business.action.task.core.ActionTaskService;
import org.igov.service.business.document.DocumentStepService;
import org.igov.service.business.email.EmailProcessSubjectService;
import org.igov.service.business.process.processLink.ProcessLinkService;
import org.igov.service.business.util.BusinessDaysCalculator;
import org.igov.service.conf.AttachmetService;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author idenysenko
 */
@Service
@Component("processSubjectTaskService")
public class ProcessSubjectTaskService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessSubjectTaskService.class);

    @Autowired
    private ProcessSubjectTaskDao oProcessSubjectTaskDao;

    @Autowired
    private ProcessSubjectDao oProcessSubjectDao;

    @Autowired
    private ProcessSubjectService oProcessSubjectService;

    @Autowired
    private ProcessSubjectTreeDao oProcessSubjectTreeDao;

    @Autowired
    private TaskService oTaskService;

    @Autowired
    private RuntimeService oRuntimeService;

    @Autowired
    private GeneralConfig oGeneralConfig;

    @Autowired
    private DocumentStepService oDocumentStepService;

    @Autowired
    private DocumentStepDao oDocumentStepDao;

    @Autowired
    private HistoryService oHistoryService;

    @Autowired
    private ProcessSubjectStatusDao oProcessSubjectStatusDao;

    @Autowired
    private EmailProcessSubjectService oEmailProcessSubjectService;

    @Autowired
    private SubjectGroupDao oSubjectGroupDao;

    @Autowired
    private AttachmetService oAttachmetService;
    
    @Autowired
    @Qualifier("durableBytesDataStorage")
    private IBytesDataStorage oBytesDataStaticStorage;
    
    @Autowired
    private ProcessLinkService oProcessLinkService; 
    
    @Autowired
    private IdentityService identityService;
    
    @Autowired
    private ActionEventHistoryService oActionEventHistoryService;
    
    @Autowired
    private ProcessSubjectTaskHistoryService oProcessSubjectTaskHistoryService;

    @Autowired
    private ActionTaskService oActionTaskService;

    /**
     * author Kovylin Yegor
     * temporary method (before fix hibernate error in ProcessSubjectTask)
     * @param oProcessSubjectTask
     * @return ProcessSubjects by ProcessSubjectTask
     */
    private List<ProcessSubject> findProcessSubjectsByrocessSubjectTask(ProcessSubjectTask oProcessSubjectTask){ 
       return oProcessSubjectDao.findAllBy("nID_ProcessSubjectTask", oProcessSubjectTask.getId());
    }
    
    /**
     * author Kovylin Yegor
     * @param snID_Process_Activiti
     * @return if all tasks in process are finished
     */
    public Map<String, Object> isDocumentTaskFinishedAll(String snID_Process_Activiti) {
        Map<String, Object> mResult = new HashMap<>();
        mResult.put("bTaskFinishedAll", false);
        
        List<ProcessSubject> aProcessSubject
                = oProcessSubjectDao.findAllBy("snID_Process_Activiti", snID_Process_Activiti);
        
        //if coincide any of controller task status - than all task in process are finished
        for (ProcessSubject oProcessSubject : aProcessSubject) {
            if (oProcessSubject.getsLoginRole().equals("Controller")
                    && (oProcessSubject.getoProcessSubjectStatus().getsID().equals("executed")
                    || oProcessSubject.getoProcessSubjectStatus().getsID().equals("notExecuted")
                    || oProcessSubject.getoProcessSubjectStatus().getsID().equals("unactual"))) {
                mResult.replace("bTaskFinishedAll", true);
            }
        }

        return mResult;
    }

    /**
     * author Kovylin Yegor
     * @param snID_Process_Activiti_Root - Document processId 
     * @param sFilterLoginRole - executors or controllers (if null - both)
     * @return list of logins from ProcessSubjects-task by root document
     */
    public List<String> getProcessSubjectByDocument(String snID_Process_Activiti_Root, String sFilterLoginRole) {

        LOG.info("getProcessSubjectByDocument started...");
        List<String> asLogin = new ArrayList<>();

        List<ProcessSubjectTask> aProcessSubjectTask = oProcessSubjectTaskDao.findAllBy("snID_Process_Activiti_Root", snID_Process_Activiti_Root);
        LOG.info("aProcessSubjectTask={}", aProcessSubjectTask);
        for (ProcessSubjectTask oProcessSubjectTask : aProcessSubjectTask) {
            List<ProcessSubject> aProcessSubject = findProcessSubjectsByrocessSubjectTask(oProcessSubjectTask);
            for (ProcessSubject oProcessSubject : aProcessSubject) {
                if (oProcessSubject.getsLoginRole().equals(sFilterLoginRole) || sFilterLoginRole == null) {
                    asLogin.add(oProcessSubject.getsLogin());
                }
            }
        }
        LOG.info("asLogin is {}", asLogin);
        LOG.info("getProcessSubjectByDocument finished");
        return asLogin;
    }

    /**
     * author Kovylin Yegor
     * Method calls in in bpmn script task and return list of logins from mongo-saved json aProcessSubject-object
     * needed for reseting all current multitasks after submit and return
     * @param snID_Process_Activiti
     * @param sFilterLoginRole - if null, we return both type (ussualy null)
     * @return
     * @throws RecordInmemoryException
     * @throws org.json.simple.parser.ParseException 
     */
    public List<String> getProcessSubjectLoginsWithoutTask(String snID_Process_Activiti, String sFilterLoginRole) throws RecordInmemoryException, org.json.simple.parser.ParseException {
        //Attention!!! Core of the task logic. DO NOT CHANGE IT BEFORE TEST CREATING
        LOG.info("getProcessSubjectLoginsWithoutTask started...");
        LOG.info("snID_Process_Activiti {}", snID_Process_Activiti);
        LOG.info("sFilterLoginRole {}", sFilterLoginRole);

        String sKeyMongo = (String) oRuntimeService.getVariable(snID_Process_Activiti, "sID_File_StorateTemp");
        byte[] aByteTaskBody = oBytesDataStaticStorage.getData(sKeyMongo);

        JSONParser parser = new JSONParser();

        List<String> aResultLogins = new ArrayList<>();

        if (aByteTaskBody != null) {
            JSONArray aJsonProcessSubject
                    = (JSONArray) ((JSONObject) parser.parse(new String(aByteTaskBody))).get("aProcessSubject");

            LOG.info("aJsonProcessSubject in getProcessSubjectLoginsWithoutTask: {}",
                    aJsonProcessSubject.toJSONString());

            for (Object oJsonProcessSubject : aJsonProcessSubject) {
                String sLogin = (String) ((JSONObject) oJsonProcessSubject).get("sLogin");
                aResultLogins.add(sLogin);
            }
        }
        LOG.info("aResultLogins in setProcessSubjectList {}", aResultLogins);
        return aResultLogins;
    }
    
    /**
     * author Kovylin Yegor
     * edit list of task in the document - if author of the document edit tasks before process-start
     * @param oaProcessSubjectTask_New
     * @param snId_Task - if null, we return both type (ussualy null)
     * @throws RecordInmemoryException
     * @throws Exception 
     */
    public void editProcessSubjectTask(JSONArray oaProcessSubjectTask_New, String snId_Task) throws Exception {
        if (snId_Task != null) {
            Task oTask = oTaskService.createTaskQuery().taskId(snId_Task).active().singleResult();
            List<Long> aProcessSubjectTaskID_ToDelete = new ArrayList<>();

            if (oTask != null) {
                String snID_Process = oTask.getProcessInstanceId();
                LOG.info("editProcessSubjectTask: snID_Process {}", snID_Process);

                List<ProcessSubjectTask> aProcessSubjectTask_Saved
                        = oProcessSubjectTaskDao.findAllBy("snID_Process_Activiti_Root", snID_Process);
                List<Long> aProcessSubjectTaskID_Saved = new ArrayList<>();

                for (ProcessSubjectTask oProcessSubjectTask_Saved : aProcessSubjectTask_Saved) {
                    aProcessSubjectTaskID_Saved.add(oProcessSubjectTask_Saved.getId());
                }

                LOG.info("aProcessSubjectTaskID_Saved {}", aProcessSubjectTaskID_Saved);
                
                boolean bDeleteAll = true;
                
                for(Object oProcessSubjectTask_New : oaProcessSubjectTask_New){
                    String sProcessSubjectTaskID_New = (String) ((JSONObject) oProcessSubjectTask_New).get("snID_ProcessSubjectTask");
                    if(sProcessSubjectTaskID_New != null){
                        bDeleteAll = false;
                    }
                }
                
                if (oaProcessSubjectTask_New.isEmpty() || bDeleteAll) {
                    // in case if we delete all tasks in the document
                    aProcessSubjectTaskID_ToDelete.addAll(aProcessSubjectTaskID_Saved);
                } else {
                    List<Long> aProcessSubjectTaskID_New = new ArrayList<>();

                    for (Object oProcessSubjectTask_New : oaProcessSubjectTask_New) {
                        String sProcessSubjectTaskID_New = (String) ((JSONObject) oProcessSubjectTask_New).get("snID_ProcessSubjectTask");

                        if (sProcessSubjectTaskID_New != null) {
                            Long oProcessSubjectTaskID_New = Long.parseLong(sProcessSubjectTaskID_New);
                            aProcessSubjectTaskID_New.add(oProcessSubjectTaskID_New);
                        }
                    }

                    LOG.info("aProcessSubjectTaskID_New {}", aProcessSubjectTaskID_New);

                    for (Long oProcessSubjectTaskID_Saved : aProcessSubjectTaskID_Saved) {
                        if (!aProcessSubjectTaskID_New.isEmpty() && !aProcessSubjectTaskID_New.contains(oProcessSubjectTaskID_Saved)) {
                            aProcessSubjectTaskID_ToDelete.add(oProcessSubjectTaskID_Saved);
                        }
                    }
                }

                LOG.info("aProcessSubjectTaskID_ToDelete {}", aProcessSubjectTaskID_ToDelete);

                for (Long oProcessSubjectTaskID_ToDelete : aProcessSubjectTaskID_ToDelete) {
                    ProcessSubjectTask oProcessSubjectTask = oProcessSubjectTaskDao.findByIdExpected(oProcessSubjectTaskID_ToDelete);
                    List<ProcessSubject> aProcessSubject = oProcessSubjectDao.findAllBy("nID_ProcessSubjectTask", oProcessSubjectTaskID_ToDelete);

                    for (ProcessSubject oProcessSubject : aProcessSubject) {
                        //because we add task - it member adds to Accept-step
                        removeProcessSubjectFromAccept(oProcessSubject);
                        LOG.info("oProcessSubject id {}", oProcessSubject.getId());
                        oProcessSubjectDao.delete(oProcessSubject);
                    }

                    oProcessSubjectTaskDao.delete(oProcessSubjectTask);
                }
            } else {
                LOG.info("editProcessSubjectTask: oTask is null");
            }
        }
    }
    
    /**
     * author Kovylin Yegor
     * main task processing - calls from task submit service
     * @param oaProcessSubjectTask
     * @param snId_Task
     * @param mParam
     * @return if we need to submit task
     * @throws Exception 
     */    
    public boolean syncProcessSubjectTask(JSONArray oaProcessSubjectTask, String snId_Task, Map<String, Object> mParam) throws Exception {
        boolean isSubmitFlag = true;
        //Attention!!! Core of the task logic. DO NOT CHANGE IT BEFORE TEST CREATING
        try {
            String sLoginPrincipal = (String) mParam.get("sLogin");
            //validation by status
            boolean isEnableToUpdate = oProcessSubjectService.isValid(snId_Task);
            LOG.info("isEnableToUpdate: {}", isEnableToUpdate);
            
            if (!isEnableToUpdate) {
                throw new RuntimeException("Завдання закрите, Ви не можете його змінити!");
            }
            
            int processSubjectTask_Counter = 0; //counter for task order detecting
            editProcessSubjectTask(oaProcessSubjectTask, snId_Task); //always check if we need to edit the list of task
            
            for (Object oJsonProcessSubjectTask : oaProcessSubjectTask) {

                String sActionType = (String) ((JSONObject) oJsonProcessSubjectTask).get("sActionType");
                JSONArray aJsonProcessSubject = (JSONArray) ((JSONObject) oJsonProcessSubjectTask).get("aProcessSubject");
                 
                String sKey = oBytesDataStaticStorage.saveData(((JSONObject) oJsonProcessSubjectTask).toJSONString().getBytes());
                LOG.info("Redis key in synctProcessSubjectTask: {}", sKey);

                if (sActionType.equals("set")) {
                    //create new task
                    setProcessSubjectTask(oJsonProcessSubjectTask, aJsonProcessSubject, sKey, processSubjectTask_Counter, mParam);                    
                    LOG.info("setProcessSubjectTask ended..");
                } else if (sActionType.equals("edit")) {
                    //edit existing task
                    editProcessSubject(oJsonProcessSubjectTask, aJsonProcessSubject, sKey, snId_Task, sLoginPrincipal);
                } else if (sActionType.equals("delegate")) {
                    //delegste task
                    LOG.info("delegating started...");
                    ProcessSubject oProcessSubjectController = getProcessSubjectByTask(snId_Task);
                    LOG.info("oProcessSubjectController is {}", oProcessSubjectController.getsLogin());
                    LOG.info("oProcessSubjectController id is {}", oProcessSubjectController.getId());
                    firstDelegateProcessSubject(oProcessSubjectController, oJsonProcessSubjectTask,
                            aJsonProcessSubject, sKey, sLoginPrincipal);
                    isSubmitFlag = false;
                } else {
                    //throw new RuntimeException("There is wrong sActionType");
                }

                processSubjectTask_Counter++;
            }
        } catch (Exception ex) {
            LOG.error("Error task setting: ", ex);
            throw ex;
        }

        return isSubmitFlag;
    }

    /**
     * author Kovylin Yegor
     * Edit one current task
     * @param oJsonProcessSubjectTask
     * @param aJsonProcessSubject
     * @param sKey
     * @param snId_Task
     * @throws Exception
     */
    private void editProcessSubject(Object oJsonProcessSubjectTask, JSONArray aJsonProcessSubject, String sKey, String snId_Task, String sLoginPrincipal) throws Exception {
        //Attention!!! Core of the task logic. DO NOT CHANGE IT BEFORE TEST CREATING
        LOG.info("editing started....");

        ProcessSubjectTask oProcessSubjectTask = oProcessSubjectTaskDao.findByIdExpected(
                Long.parseLong((String) ((JSONObject) oJsonProcessSubjectTask).get("snID_ProcessSubjectTask")));

        //ProcessSubject oProcessSubjectController = null;
        String snID_Process_Activiti_Controller = null;
        ProcessSubject oProcessSubject_Controller = null;
        Optional<ProcessSubject> oProcessSubjectController_Optional = oProcessSubjectDao.findBy("snID_Task_Activiti", snId_Task);

        if (oProcessSubjectController_Optional.isPresent() && oProcessSubjectController_Optional.get() != null) {
            // we edit task after starting
            snID_Process_Activiti_Controller = oProcessSubjectController_Optional.get().getSnID_Process_Activiti();
            oProcessSubject_Controller = oProcessSubjectController_Optional.get();
        } else {
            //we edit before start process (from document)
            snID_Process_Activiti_Controller = oTaskService.createTaskQuery().taskId(snId_Task)
                    .active().singleResult().getProcessInstanceId();
            
            if (snID_Process_Activiti_Controller == null) {
                throw new RuntimeException("Error! snID_Process_Activiti_Controller is empty!");
            }
        }

        String sKey_New = oBytesDataStaticStorage.saveData(((JSONObject) oJsonProcessSubjectTask).toJSONString().getBytes());
        oRuntimeService.setVariable(snID_Process_Activiti_Controller, "sID_File_StorateTemp", sKey_New);
        oProcessSubjectTask.setsKey(sKey_New);
        List<ProcessSubject> aProcessSubject_saved
                = oProcessSubjectDao.findAllBy("nID_ProcessSubjectTask", oProcessSubjectTask.getId());
        
        boolean isDelegate = false;
        ProcessSubject oProcessSubject_Controller_Parent = null;
        if (oProcessSubject_Controller != null) { //check if we editing of delegated task - for tree editing
            LOG.info("oProcessSubject_Controller is {}", oProcessSubject_Controller.getId());
            Optional<ProcessSubjectTree> processSubjectTree = oProcessSubjectTreeDao.findBy("processSubjectChild", oProcessSubject_Controller);
            if (processSubjectTree.isPresent() && processSubjectTree.get() != null) {
                isDelegate = true;
                oProcessSubject_Controller_Parent = processSubjectTree.get().getProcessSubjectParent();
            }else{
                //with common execution
                for(ProcessSubject oProcessSubject_saved : aProcessSubject_saved){
                    if(!oProcessSubject_Controller.getId().equals(oProcessSubject_saved.getId())){
                        List<ProcessSubjectTree> aProcessSubjectTree_Common = 
                               oProcessSubjectTreeDao.findAllBy("processSubjectChild", oProcessSubject_saved); 
                        for(ProcessSubjectTree processSubjectTree_Common : aProcessSubjectTree_Common) 
                        {
                            if(!(processSubjectTree_Common.getProcessSubjectParent().getId().equals(oProcessSubject_Controller.getId()))){
                                isDelegate = true;
                                oProcessSubject_Controller_Parent = processSubjectTree_Common.getProcessSubjectParent();
                                break;
                            }
                        }
                    }
                    
                    if(isDelegate){
                        break;
                    }
                }
            }
        }
        
        List<String> aNewLogin = new ArrayList<>();
        List<String> aNewLogin_LoginRole = new ArrayList<>();

        for (Object oJsonProcessSubject : aJsonProcessSubject) {
            String sLogin = (String) ((JSONObject) oJsonProcessSubject).get("sLogin");
            String sLoginRole = (String) ((JSONObject) oJsonProcessSubject).get("sLoginRole");
            aNewLogin.add(sLogin);
            aNewLogin_LoginRole.add(sLogin + sLoginRole); //we need it in case of controller equals executor (now depricated, but client can ask thisopportunity)
        }
        
        ArrayList<String> asProcessSubject_ToUpdate = new ArrayList<>();
        
        Boolean bDelete = false;
        Boolean bUpdate = false;
        
        LOG.info("aProcessSubject_saved is {}", aProcessSubject_saved);
        List<ProcessSubject> aProcessSubject_ToUpdate = new ArrayList<>();
        for (ProcessSubject oProcessSubject : aProcessSubject_saved) {
            if(oProcessSubject.getSnID_Process_Activiti().equals(snID_Process_Activiti_Controller)){
                if (!aNewLogin_LoginRole.contains(oProcessSubject.getsLogin() + oProcessSubject.getsLoginRole())) 
                {
                    //delete task that we didn't find in new json data
                    LOG.info("Login to delete in new task setting schema is {}", oProcessSubject.getsLogin());
                    //try to delete user from accept-step (automaticly added when task was create from document)
                    removeProcessSubjectFromAccept(oProcessSubject);
                    //delete current task and all tasks by delegating tree
                    removeProcessSubjectDeep(oProcessSubject);
                    if (((JSONObject) oJsonProcessSubjectTask).get("sKey_GroupPostfix") != null) {
                        String nId_Task_Root = oTaskService.createTaskQuery().processInstanceId((String) ((JSONObject) oJsonProcessSubjectTask)
                                .get("snID_Process_Activiti_Root")).active().singleResult().getId();
                        //delete task from activiti
                        oTaskService.deleteCandidateGroup(nId_Task_Root, oProcessSubject.getsLogin());
                        LOG.info("user to delete is {}", oProcessSubject.getsLogin());
                        LOG.info("nId_Task_Root to delete is {}", nId_Task_Root);
                    }
                    
                    bDelete = true;
                } else {
                    bUpdate = true;
                    LOG.info("Login to update in new task setting schema is {}", oProcessSubject.getsLogin());
                    aProcessSubject_ToUpdate.add(oProcessSubject);
                    asProcessSubject_ToUpdate.add(oProcessSubject.getsLogin() + oProcessSubject.getsLoginRole());
                }
            }
        }
        
        if(!bDelete && !bUpdate){
            //Because when we add second task - first task send to us from front with "edit" status - but there is no changes
            //don't ask. Just ignore it
            LOG.info("Nothing to edit...");
            return;
        }

        //for getting of max nOrder - because when we create task - we use max nOrder of all tasks + 1
        LongSummaryStatistics summaryStatistics = aProcessSubject_saved.stream()
                .mapToLong(ProcessSubject::getnOrder)
                .summaryStatistics();
        //modify ProcessSubject-data in database
        List<ProcessSubject> aProcessSubjectList = setProcessSubjectList(aJsonProcessSubject,
                (JSONObject) oJsonProcessSubjectTask, oProcessSubjectTask,
                snID_Process_Activiti_Controller, aProcessSubject_ToUpdate, summaryStatistics.getMax() + 1);
        LOG.info("isDelegate is {}", isDelegate);
        if (isDelegate && oProcessSubject_Controller_Parent != null) {
            //edit procesSubjectTree for correct delegate-hierarchy display
            LOG.info("oProcessSubject_Controller_Parent is {}", oProcessSubject_Controller_Parent.getId());
            ProcessSubject oProcessSubject_Controller_New = null;

            for (ProcessSubject oProcessSubject : aProcessSubjectList) {
                if (oProcessSubject.getsLoginRole().equals("Controller")) {
                    oProcessSubject_Controller_New = oProcessSubject;
                }
            }

            LOG.info("oProcessSubject_Controller_New is {}", oProcessSubject_Controller_New.getId());
            LOG.info("aProcessSubjectList is {}", aProcessSubjectList);
            LOG.info("asProcessSubject_ToUpdate is {}", asProcessSubject_ToUpdate);
            
            if (oProcessSubject_Controller_New != null) {
                for (ProcessSubject oProcessSubject : aProcessSubjectList) {
                    if (asProcessSubject_ToUpdate.isEmpty()
                            || !asProcessSubject_ToUpdate.contains(oProcessSubject.getsLogin() + oProcessSubject.getsLoginRole())) {
                        if (!oProcessSubject_Controller_New.getId().equals(oProcessSubject.getId())) {
                            saveProcessSubjectTree(oProcessSubject_Controller_Parent, oProcessSubject);
                        }
                    }
                }
            }
        }

        for (ProcessSubject oProcessSubject : aProcessSubjectList) {
            ProcessSubjectTask oProcessSubjectTask_Root = oProcessSubjectTaskDao.findByIdExpected(oProcessSubject.getnID_ProcessSubjectTask());
            if(oProcessSubjectTask_Root!= null && oProcessSubjectTask_Root.getSnID_Process_Activiti_Root() != null){
                //share activiti-permition for task
                oActionTaskService.addIdentityLinkToDocument(oProcessSubjectTask_Root.getSnID_Process_Activiti_Root(), oProcessSubject.getsLogin());
            }
            
            if (oProcessSubject.getsLoginRole().equals("Controller")) {
                LOG.info("oProcessSubject controller id is {}", oProcessSubject.getSnID_Task_Activiti());
                if (oProcessSubject.getSnID_Task_Activiti() != null) {
                    //something wrong was in bp - add sStatusDecision seting for quick fix
                    oTaskService.setVariable(oProcessSubject.getSnID_Task_Activiti(), "sStatusDecision", "test");
                    //recomliting all tasks - lisner reset all tasks id in database
                    oTaskService.complete(oProcessSubject.getSnID_Task_Activiti());
                }
            }
        }

        oProcessSubjectTask.setsBody((String) ((JSONObject) oJsonProcessSubjectTask).get("sBody"));
        oProcessSubjectTask.setsHead((String) ((JSONObject) oJsonProcessSubjectTask).get("sHead"));
        oProcessSubjectTaskDao.saveOrUpdate(oProcessSubjectTask);
        
        //save in task history
        Map<String, Object> mParamTask = new HashMap<>();
        mParamTask.put("sLoginPrincipal", sLoginPrincipal);        
        oProcessSubjectTaskHistoryService.setInHistory(aProcessSubject_saved, oProcessSubjectTask, mParamTask, "edit");
                
        //send mails and syncProcessLinks
        oProcessLinkService.syncProcessLinks(snID_Process_Activiti_Controller, sLoginPrincipal);
        
        List<String> aSavedLogins = aNewLogin;
        List<ProcessSubject> aProcessSubject_ToEmail = new ArrayList<>();
        
        for (ProcessSubject oProcessSubject : aProcessSubject_saved) {
            if (aSavedLogins.contains(oProcessSubject.getsLogin())) {
                aSavedLogins.remove(oProcessSubject.getsLogin());
            }
        }
        LOG.info("aSavedLogins: {}", aSavedLogins);

        List<ProcessSubject> aProcessSubject
                = oProcessSubjectDao.findAllBy("nID_ProcessSubjectTask", oProcessSubjectTask.getId());

        for (ProcessSubject oProcessSubject : aProcessSubject) {
            if (aSavedLogins.contains(oProcessSubject.getsLogin())) {
                LOG.info("Login: {}", oProcessSubject.getsLogin());
                aProcessSubject_ToEmail.add(oProcessSubject);
            }
        }

        sendEmail(oProcessSubjectTask, aProcessSubject_ToEmail);
    }

    private ProcessInstance startProcessInstanceByKey(String sID_BP, Map<String, Object> mParamTask) {
        LOG.info("startProcessInstanceByKey {}", sID_BP);
        return oRuntimeService.startProcessInstanceByKey(sID_BP, mParamTask);
    }

    private void updateProcessSubjects(ProcessSubjectTask oProcessSubjectTask, ProcessInstance oProcessInstance) {
        List<ProcessSubject> aProcessSubject
                = oProcessSubjectDao.findAllBy("nID_ProcessSubjectTask", oProcessSubjectTask.getId());

        LOG.info("aProcessSubject size is {}", aProcessSubject.size());

        for (ProcessSubject oProcessSubject : aProcessSubject) {
            oProcessSubject.setSnID_Process_Activiti(oProcessInstance.getId());
        }

        oProcessSubjectDao.saveOrUpdate(aProcessSubject);
    }

    public Long startProcess(String snID_Process_Activiti_Root) throws ParseException, Exception {

        LOG.info("startProcess started...");
        Map<String, Object> mParamTask = new HashMap<>();
        List<ProcessSubjectTask> aProcessSubjectTask
                = oProcessSubjectTaskDao.findAllBy("snID_Process_Activiti_Root", snID_Process_Activiti_Root);

        for (ProcessSubjectTask oProcessSubjectTask : aProcessSubjectTask) {
            LOG.info("ProcessSubjectTask with id {} was founded {}", oProcessSubjectTask.getId());
            byte[] aByteTaskBody = oBytesDataStaticStorage.getData(oProcessSubjectTask.getsKey());
            
            //in case when we start task after each submit in loop
            List<ProcessSubject> aProcessSubject_saved
                    = oProcessSubjectDao.findAllBy("nID_ProcessSubjectTask", oProcessSubjectTask.getId());
            LOG.info("aProcessSubject_saved size is {}", aProcessSubject_saved.size());
            for(ProcessSubject oProcessSubject_saved : aProcessSubject_saved){
                LOG.info("oProcessSubject_saved id is {}", oProcessSubject_saved.getId());
                LOG.info("oProcessSubject_saved processid is {}", oProcessSubject_saved.getSnID_Process_Activiti());
            }
            
            if(!aProcessSubject_saved.isEmpty() && 
                    !aProcessSubject_saved.get(0).getSnID_Process_Activiti().equals(snID_Process_Activiti_Root)){
                continue;
            }
            
            LOG.info("aByteTaskBody size {}", aByteTaskBody.length);

            JSONParser parser = new JSONParser();
            mParamTask.put("snID_ProcessSubjectTask", oProcessSubjectTask.getId());
            mParamTask.put("sID_File_StorateTemp", oProcessSubjectTask.getsKey());
            mParamTask.put("sHead", oProcessSubjectTask.getsHead());            
            
            List<ProcessSubject> aProcessSubject
                = oProcessSubjectDao.findAllBy("nID_ProcessSubjectTask", oProcessSubjectTask.getId());
            
            if (snID_Process_Activiti_Root != null) {
                mParamTask.put("sID_Order_Document", oGeneralConfig.getOrderId_ByProcess(Long.parseLong(snID_Process_Activiti_Root)));
                mParamTask.put("snID_Process_Activiti_Root", snID_Process_Activiti_Root);
            }
            LOG.info("mParamTask {}", mParamTask);
            ProcessInstance oProcessInstance = startProcessInstanceByKey((String) (((JSONObject) parser.parse(new String(aByteTaskBody)))).get("sID_BP"), mParamTask);
            LOG.info("Process {} was started", oProcessInstance.getProcessInstanceId());

            updateProcessSubjects(oProcessSubjectTask, oProcessInstance);           
            
            //oProcessLinkService.syncProcessLinks(oProcessInstance.getProcessInstanceId());
            
            //для задач у которых время исполнения было выбрано кол-во дней, планируемую дату выполнения проставляем
            //в момент старта задачи
            for (ProcessSubject oProcessSubject : aProcessSubject) {
                Integer nDayPlan = oProcessSubject.getnDayPlan();
                if (nDayPlan != null) {
                    LocalDate date_WorkingDays = LocalDate.now().with(BusinessDaysCalculator.addWorkingDays(nDayPlan));
                    DateTime sDatePlan = new DateTime(date_WorkingDays.toString());
                    oProcessSubject.setsDatePlan(sDatePlan);
                }
            }
            oProcessSubjectDao.saveOrUpdate(aProcessSubject);
            
            sendEmail(oProcessSubjectTask, aProcessSubject);   
            oProcessSubjectTaskHistoryService.setInHistory(aProcessSubject, oProcessSubjectTask, mParamTask, "start");
        }
        
        return Long.valueOf(aProcessSubjectTask.size());
    }    
    
    private void setProcessSubjectTask(Object oJsonProcessSubjectTask, JSONArray aJsonProcessSubject,
            String sKey, Integer processSubjectTask_Counter, Map<String, Object> mParamTask) throws Exception {
        LOG.info("setProcessSubjectTask started..");
        String sLoginPrincipal = (String) mParamTask.get("sLogin");
        String snID_Process_Activiti_Root = (String) ((JSONObject) oJsonProcessSubjectTask).get("snID_Process_Activiti_Root");

        ProcessSubjectTask oProcessSubjectTask = new ProcessSubjectTask();
        oProcessSubjectTask.setSnID_Process_Activiti_Root(snID_Process_Activiti_Root);
        oProcessSubjectTask.setsBody((String) ((JSONObject) oJsonProcessSubjectTask).get("sBody"));
        oProcessSubjectTask.setsHead((String) ((JSONObject) oJsonProcessSubjectTask).get("sHead"));
        oProcessSubjectTask.setsKey(sKey);
        oProcessSubjectTaskDao.saveOrUpdate(oProcessSubjectTask);        
        LOG.info("oProcessSubjectTask in synctProcessSubjectTask: {}", oProcessSubjectTask);

        mParamTask.put("snID_ProcessSubjectTask", oProcessSubjectTask.getId());
        mParamTask.put("sHead", oProcessSubjectTask.getsHead());

        List<ProcessSubject> aProcessSubject = null;

        if (snID_Process_Activiti_Root != null) {
            //mParamTask.put("sID_File_StorateTemp", sKey);
            aProcessSubject = setProcessSubjectList(aJsonProcessSubject, (JSONObject) oJsonProcessSubjectTask,
                    oProcessSubjectTask, snID_Process_Activiti_Root,
                    null, 0L);
            ///НЕ РАСКОМЕНЧИВАТЬ!!!!!!!!!!!!!!!!!!!!!
            //ProcessInstance oProcessInstance = startProcessInstanceByKey((String) ((JSONObject) oJsonProcessSubjectTask).get("sID_BP"), mParamTask);
            //LOG.info("oProcessInstance id is {}", oProcessInstance.getId());
            //sendEmail(oProcessSubjectTask, aProcessSubject);
            if (oRuntimeService.hasVariable(snID_Process_Activiti_Root, "TaskCreatorLogin")) {
                JSONObject mTaskCreatorLogin = new JSONObject();
                mTaskCreatorLogin = (JSONObject)oRuntimeService.getVariable(snID_Process_Activiti_Root, "TaskCreatorLogin");                
                mTaskCreatorLogin.put(oProcessSubjectTask.getId(), sLoginPrincipal);
                LOG.info("mTaskCreatorLogin update: {}", mTaskCreatorLogin);
                oRuntimeService.setVariable(snID_Process_Activiti_Root, "TaskCreatorLogin", mTaskCreatorLogin);
            } else {
                JSONObject mTaskCreatorLogin = new JSONObject();
                mTaskCreatorLogin.put(oProcessSubjectTask.getId(), sLoginPrincipal);
                LOG.info("mTaskCreatorLogin create: {}", mTaskCreatorLogin);
                oRuntimeService.setVariable(snID_Process_Activiti_Root, "TaskCreatorLogin", mTaskCreatorLogin);
            }
        } else {
            //Map<String, Object> mParamTask = new HashMap<>();

            mParamTask.put("sID_File_StorateTemp", sKey);
            aProcessSubject = setProcessSubjectList(aJsonProcessSubject, (JSONObject) oJsonProcessSubjectTask,
                    oProcessSubjectTask, "startedWithoutRootDocument",
                    null, 0L);

            ProcessInstance oProcessInstance = startProcessInstanceByKey((String) ((JSONObject) oJsonProcessSubjectTask).get("sID_BP"), mParamTask);
            LOG.info("oProcessInstance id is {}", oProcessInstance.getId());
            updateProcessSubjects(oProcessSubjectTask, oProcessInstance);
            oProcessSubjectTask.setSnID_Process_Activiti_Root(oProcessInstance.getProcessInstanceId());
            oProcessSubjectTaskDao.saveOrUpdate(oProcessSubjectTask);
            //Записываем в иcторию при старте задания            
            oProcessSubjectTaskHistoryService.setInHistory(aProcessSubject, oProcessSubjectTask, mParamTask, "create");

            sendEmail(oProcessSubjectTask, aProcessSubject);

            oProcessLinkService.syncProcessLinks(oProcessInstance.getProcessInstanceId(), sLoginPrincipal);
        }
        
        LOG.info("aProcessSubject in synctProcessSubjectTask: {}", aProcessSubject);
    }
    
    private void firstDelegateProcessSubject(ProcessSubject oProcessSubjectController, Object oJsonProcessSubjectTask,
                                             JSONArray aJsonProcessSubject, String sKey, String sLoginPrincipal) throws Exception {
        LOG.info("this is first delegating");
        ProcessSubjectTask oProcessSubjectTask = oProcessSubjectTaskDao.findByIdExpected(
                Long.parseLong((String) ((JSONObject) oJsonProcessSubjectTask).get("snID_ProcessSubjectTask")));

        LOG.info("oProcessSubjectTask is {}", oProcessSubjectTask);
        

        Map<String, Object> mParamTask = new HashMap<>();        

        mParamTask.put("sID_File_StorateTemp", sKey);
        /*mParamTask.put("sID_Order_Document", oGeneralConfig.
                getOrderId_ByProcess((String) ((JSONObject) oJsonProcessSubjectTask).get("snID_Process_Activiti_Root")));*/
        mParamTask.put("snID_ProcessSubjectTask", oProcessSubjectTask.getId());

        if ((String) ((JSONObject) oJsonProcessSubjectTask).get("snID_Process_Activiti_Root") != null) {
            mParamTask.put("sID_Order_Document", oGeneralConfig
                    .getOrderId_ByProcess(Long.parseLong((String) ((JSONObject) oJsonProcessSubjectTask)
                            .get("snID_Process_Activiti_Root"))));
        }
        mParamTask.put("sHead", oProcessSubjectTask.getsHead());
        LOG.info("oProcessSubjectTask is {}", oProcessSubjectTask);

        List<ProcessSubject> aProcessSubject
                = setProcessSubjectList(aJsonProcessSubject, (JSONObject) oJsonProcessSubjectTask,
                        oProcessSubjectTask, null, null, 0L);
        LOG.info("aProcessSubject is {}", aProcessSubject);
        oProcessSubjectTask.setaProcessSubject(aProcessSubject);
        oProcessSubjectTaskDao.saveOrUpdate(oProcessSubjectTask);

        ProcessInstance oProcessInstance = startProcessInstanceByKey((String) ((JSONObject) oJsonProcessSubjectTask).get("sID_BP"), mParamTask);
        LOG.info("oProcessInstance id is {}", oProcessInstance.getId());

        Integer nDeep_Child = oProcessSubjectController.getnDeep() + 1;
        LOG.info("nDeep_Child={}", nDeep_Child);
        //save in history
        mParamTask.put("sLoginPrincipal", sLoginPrincipal);
        mParamTask.put("oProcessInstanceID", oProcessInstance.getId());
        mParamTask.put("snID_Process_Activiti_Parent", oProcessSubjectController.getSnID_Process_Activiti());
        oProcessSubjectTaskHistoryService.setInHistory(aProcessSubject, oProcessSubjectTask, mParamTask, "delegate");

        List<ProcessSubject> aProcessSubject_ToEmail = new ArrayList<>();

        for (ProcessSubject oProcessSubject : aProcessSubject) {
            LOG.info("oProcessSubject is {}", oProcessSubject);
            ProcessSubjectTask oProcessSubjectTask_Root = oProcessSubjectTaskDao.findByIdExpected(oProcessSubject.getnID_ProcessSubjectTask());
            if(oProcessSubjectTask_Root!= null && oProcessSubjectTask_Root.getSnID_Process_Activiti_Root() != null){
                oActionTaskService.addIdentityLinkToDocument(oProcessSubjectTask_Root.getSnID_Process_Activiti_Root(), oProcessSubject.getsLogin());
            }
            
            if(!oProcessSubject.getsLogin().equals(oProcessSubjectController.getsLogin())){
                saveProcessSubjectTree(oProcessSubjectController, oProcessSubject);
            }
            
            oProcessSubject.setSnID_Process_Activiti(oProcessInstance.getId());
            oProcessSubject.setnDeep(nDeep_Child);
            oProcessSubjectDao.saveOrUpdate(oProcessSubject);
            if (oProcessSubject.getsLoginRole().equals("Executor")) {
                aProcessSubject_ToEmail.add(oProcessSubject);
            }
        }       
        oProcessLinkService.syncProcessLinks(oProcessInstance.getProcessInstanceId(), sLoginPrincipal);
        sendEmail(oProcessSubjectTask, aProcessSubject_ToEmail);        
    }
       
    private List<ProcessSubject> setProcessSubjectList(JSONArray aJsonProcessSubject, JSONObject oJsonProcessSubjectTask,
            ProcessSubjectTask oProcessSubjectTask, String snID_Process_Activiti,
            List<ProcessSubject> aProcessSubject_ToUpdate, Long nStartOrder) throws ParseException, Exception {

        DateFormat df_sDatePlan = new SimpleDateFormat("yyyy-MM-dd");

        LOG.info("setProcessSubjectList started..");
        LOG.info("nStartOrder is {}", nStartOrder);
        LOG.info("aJsonProcessSubject is {}", aJsonProcessSubject);

        ProcessSubjectStatus oProcessSubjectStatus = oProcessSubjectStatusDao.findByIdExpected(1L);

        List<ProcessSubject> aProcessSubject = new ArrayList<>();

        Long nOrder = nStartOrder;

        /*List<String> asKey_Step = null;

        if (((JSONObject) oJsonProcessSubjectTask).get("sKey_GroupPostfix") != null) {
            HistoricProcessInstance oHistoricProcessInstance = oHistoryService.createHistoricProcessInstanceQuery().
                    processInstanceId((String) ((JSONObject) oJsonProcessSubjectTask)
                            .get("snID_Process_Activiti_Root")).singleResult();

            nId_Task_Root = oTaskService.createTaskQuery().processInstanceId((String) ((JSONObject) oJsonProcessSubjectTask)
                    .get("snID_Process_Activiti_Root")).active().singleResult().getId();

            LOG.info("oProcessDefinition is {}", oHistoricProcessInstance.getProcessDefinitionId());

            String sPath = "document/"
                    + oHistoricProcessInstance.getProcessDefinitionId().split(":")[0] + ".json";
            LOG.info("sPath={}", sPath);

            byte[] aByteDocument = getFileData_Pattern(sPath);
            if (aByteDocument != null && aByteDocument.length > 0) {
                String soJSON = Tool.sData(aByteDocument);
                LOG.info("soJSON in ProcessSubjectTask is: {}", soJSON);
                org.activiti.engine.impl.util.json.JSONObject oJSON = new org.activiti.engine.impl.util.json.JSONObject(soJSON);
                asKey_Step = Arrays.asList(org.activiti.engine.impl.util.json.JSONObject.getNames(oJSON));

                LOG.info("List of steps in ProcessSubjectTask is: {}", asKey_Step);
            }

        }*/
        for (Object oJsonProcessSubject : aJsonProcessSubject) {

            ProcessSubject oProcessSubject = null;

            if (aProcessSubject_ToUpdate != null) {
                //update existing entity;
                for (ProcessSubject oProcessSubject_ToUpdate : aProcessSubject_ToUpdate) {
                    if (oProcessSubject_ToUpdate.getsLogin().equals((String) ((JSONObject) oJsonProcessSubject).get("sLogin"))
                            && oProcessSubject_ToUpdate.getsLoginRole().equals((String) ((JSONObject) oJsonProcessSubject).get("sLoginRole"))) {
                        oProcessSubject = oProcessSubject_ToUpdate;
                        LOG.info("oProcessSubject to update is {}", oProcessSubject);
                        if (((JSONObject) oJsonProcessSubject).get("sDatePlan") != null) {
                            DateTime datePlan = new DateTime(df_sDatePlan.parse(
                                    (String) ((JSONObject) oJsonProcessSubject).get("sDatePlan")));
                            oProcessSubject_ToUpdate.setsDatePlan(datePlan);
                        }
                        break;
                    }
                }
            }

            if (oProcessSubject == null) {
                //this is new entity;
                oProcessSubject = new ProcessSubject();
            }

            LOG.info("oJsonProcessSubject in setProcessSubjectList: {}", ((JSONObject) oJsonProcessSubject).toJSONString());

            oProcessSubject.setsTextType((String) ((JSONObject) oJsonProcessSubjectTask).get("sReportType"));
            oProcessSubject.setsLogin((String) ((JSONObject) oJsonProcessSubject).get("sLogin"));
            oProcessSubject.setsLoginRole((String) ((JSONObject) oJsonProcessSubject).get("sLoginRole"));
//            oProcessSubject.setoProcessSubjectTask(oProcessSubjectTask);
            oProcessSubject.setnID_ProcessSubjectTask(oProcessSubjectTask.getId());
            oProcessSubject.setoProcessSubjectStatus(oProcessSubjectStatus);
            oProcessSubject.setsDateEdit(new DateTime(new Date()));
            oProcessSubject.setnOrder(nOrder);

            nOrder = nOrder + 1L;
            oProcessSubject.setSnID_Process_Activiti(snID_Process_Activiti);
            
            if (((JSONObject) oJsonProcessSubject).get("sDatePlan") != null 
                    && ((JSONObject) oJsonProcessSubject).get("nDayPlan") != null) {        
                
                throw new IllegalArgumentException("Two parameter send [sDatePlan, nDayPlan], but must be only one.");
            }
            DateTime sDatePlan = null;
            if (((JSONObject) oJsonProcessSubject).get("sDatePlan") != null) {
                sDatePlan = new DateTime(df_sDatePlan.parse(
                        (String) ((JSONObject) oJsonProcessSubject).get("sDatePlan")));
            }
            oProcessSubject.setsDatePlan(sDatePlan);
            
            Integer nDayPlan = null;
            if (((JSONObject) oJsonProcessSubject).get("nDayPlan") != null){
                nDayPlan = Integer.parseInt(String.valueOf(((JSONObject) oJsonProcessSubject).get("nDayPlan")));          
            }
            oProcessSubject.setnDayPlan(nDayPlan);

            aProcessSubject.add(oProcessSubject);
            LOG.info("oProcessSubject in setProcessSubjectList: {}", oProcessSubject);
            String snID_Process_Activiti_Root = (String) ((JSONObject) oJsonProcessSubjectTask).get("snID_Process_Activiti_Root");

            if (snID_Process_Activiti_Root != null) {
                List<HistoricVariableInstance> aHistoricVariableInstance = oHistoryService.createHistoricVariableInstanceQuery()
                        .processInstanceId(snID_Process_Activiti_Root).list();

                String sKey_Step_Active = null;

                for (HistoricVariableInstance oHistoricVariableInstance : aHistoricVariableInstance) {
                    if (oHistoricVariableInstance.getVariableName().startsWith("sKey_Step")) {
                        sKey_Step_Active = (String) oHistoricVariableInstance.getValue();
                        LOG.info("oHistoricVariableInstance.getValue {}", oHistoricVariableInstance.getValue());
                    }
                }

                if (sKey_Step_Active != null) {
                    //CANDIDATE
                    /*oTaskService.addGroupIdentityLink(nId_Task_Root, 
                            (String)((JSONObject)oJsonProcessSubject).get("sLogin"), "CANDIDATE");*/
                    DocumentStep oDocumentStep_Common
                            = oDocumentStepService.getDocumentStep((String) ((JSONObject) oJsonProcessSubjectTask).get("snID_Process_Activiti_Root"), "_");

                    List<DocumentStepSubjectRight> aDocumentStepSubjectRight_Common = oDocumentStep_Common.aDocumentStepSubjectRight();

                    boolean isNewLogin = true;

                    for (DocumentStepSubjectRight oDocumentStepSubjectRight_Common : aDocumentStepSubjectRight_Common) {
                        if (oDocumentStepSubjectRight_Common.getsKey_GroupPostfix().equals((String) ((JSONObject) oJsonProcessSubject).get("sLogin"))) {
                            isNewLogin = false;
                        }
                    }

                    if (isNewLogin) {

                        Task oTask_Root_Active = oTaskService.createTaskQuery().processInstanceId((String) ((JSONObject) oJsonProcessSubjectTask)
                                .get("snID_Process_Activiti_Root")).active().singleResult();

                        if (oTask_Root_Active != null) {
                            String nId_Task_Root = oTask_Root_Active.getId();

                            oTaskService.addCandidateGroup(nId_Task_Root, (String) ((JSONObject) oJsonProcessSubject).get("sLogin"));
                            oDocumentStepService.addRightsToCommonStep((String) ((JSONObject) oJsonProcessSubjectTask).get("snID_Process_Activiti_Root"),
                                    (String) ((JSONObject) oJsonProcessSubject).get("sLogin"),
                                    sKey_Step_Active);
                            LOG.info("nId_Task_Root is {}", nId_Task_Root);
                            LOG.info("sLogin is {}", (String) ((JSONObject) oJsonProcessSubject).get("sLogin"));
                        }
                    }

                    /*for (String step : asKey_Step) {
                        oDocumentStepService.cloneDocumentStepSubject((String) ((JSONObject) oJsonProcessSubjectTask).get("snID_Process_Activiti_Root"),
                                (String) ((JSONObject) oJsonProcessSubjectTask).get("sKey_GroupPostfix"), (String) ((JSONObject) oJsonProcessSubject).get("sLogin"), step, true);

                        /*List<DocumentStep> aDocumentStep = oDocumentStepDao.getRightsByStep((String)((JSONObject)oJsonProcessSubjectTask).get("snID_Process_Activiti_Root"), step);
                        for(DocumentStep oDocumentStep : aDocumentStep){
                            List<DocumentStepSubjectRight> aDocumentStepSubjectRight = oDocumentStep.getRights();
                            for(DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight){
                                if(oDocumentStepSubjectRight.getsKey_GroupPostfix().equals((String)((JSONObject)oJsonProcessSubject).get("sLogin"))){
                                   LOG.info("sKey_GroupPostfix in processSubjectTaskService is {}", oDocumentStepSubjectRight.getsKey_GroupPostfix()); 
                                   oDocumentStepSubjectRight.setbWrite(false);
                                   oDocumentStepSubjectRightDao.saveOrUpdate(oDocumentStepSubjectRight);
                                   break;
                                }
                            }
                        }
                    }*/
                }
            }
        }

        LOG.info("aProcessSubject size is {}", aProcessSubject.size());

        for (ProcessSubject oProcessSubject : aProcessSubject) {
            LOG.info("oProcessSubject is setProcessSubject sLogin {}", oProcessSubject.getsLogin());
            LOG.info("oProcessSubject is setProcessSubject sLoginRole {}", oProcessSubject.getsLoginRole());
        }        
        return oProcessSubjectDao.saveOrUpdate(aProcessSubject);
    }

    public void removeProcessSubject(ProcessSubject processSubject, boolean closeTaskFlag) {
        LOG.info("removeProcessSubject started...");

        if (closeTaskFlag) {
            if (processSubject.getSnID_Task_Activiti() != null) {
                LOG.info("TaskInstance to remove is {}", processSubject.getSnID_Task_Activiti());
                //oTaskService.setVariable(processSubject.getSnID_Task_Activiti(), "sStatusDecision", "test");
                //oTaskService.complete(processSubject.getSnID_Task_Activiti());
            }
        } else {
            LOG.info("ProcessInstance is to delete {}", processSubject.getSnID_Process_Activiti());
            ProcessInstance processInstance = oRuntimeService.createProcessInstanceQuery().processInstanceId(processSubject.getSnID_Process_Activiti()).singleResult();
            if (processInstance != null) {
                oRuntimeService.deleteProcessInstance(processSubject.getSnID_Process_Activiti(), "deleted");
            }
        }

        LOG.info("TaskInstance deleted..");
        Optional<ProcessSubjectTree> processSubjectTreeToDelete = oProcessSubjectTreeDao.findBy("processSubjectChild", processSubject);

        if (processSubjectTreeToDelete.isPresent()) {
            LOG.info("processSubjectTreeToDelete {}", processSubjectTreeToDelete.get());
            oProcessSubjectTreeDao.delete(processSubjectTreeToDelete.get());
        } else {
            LOG.info("processSubjectTree is null");
        }

        LOG.info("deleted processSubject Id is {}", processSubject.getId());
        oProcessSubjectDao.delete(processSubject);
        LOG.info("removeProcessSubject ended...");
    }

    public void removeProcessSubjectDeep(ProcessSubject processSubject) {
        LOG.info("removeProcessSubjectDeep started...");
        ProcessSubjectResult processSubjectResult = oProcessSubjectService.getCatalogProcessSubject(processSubject.getSnID_Process_Activiti(), 0L, null);

        LOG.info("processSubjectResult {}", processSubjectResult.getaProcessSubject());
        List<ProcessSubject> aProcessSubject = processSubjectResult.getaProcessSubject();
        List<ProcessSubject> aReverseProcessSubject = Lists.reverse(aProcessSubject);

        for (ProcessSubject oProcessSubject : aReverseProcessSubject) {
            LOG.info("oProcessSubject catalog user is {}", oProcessSubject.getaUser());
            LOG.info("processSubject id to delete {}", oProcessSubject.getId());
            removeProcessSubject(oProcessSubject, false);
        }

        removeProcessSubject(processSubject, true);
        LOG.info("removeProcessSubjectDeep ended...");
    }

    /**
     * Получение ProcessSubject по ид таски.
     */
    private ProcessSubject getProcessSubjectByTask(String snID_Task_Activiti) {
        return oProcessSubjectDao.findByExpected("snID_Task_Activiti", snID_Task_Activiti);
    }

    /**
     * Получение списка ProcessSubjectTask
     *
     * @param snID_Process_Activiti ид процесса
     * @return список задач, которые относятся к заданому процессу(-ам)
     */
    public List<ProcessSubjectTask> getProcessSubjectTask(String snID_Process_Activiti) {
        LOG.info("getProcessSubjectTask started with snID_Process_Activiti={}", snID_Process_Activiti);
        List<ProcessSubjectTask> aoProcessSubjectTask = new ArrayList<>();
        try {
            //находим все ProcessSubjectTask для процесса
            List<ProcessSubject> aProcessSubjectAll = oProcessSubjectDao
                    .findAllBy("snID_Process_Activiti", snID_Process_Activiti);
            Set<Long> anID_ProcessSubjectTask = new HashSet<>();
            aProcessSubjectAll.forEach(oProcessSubject
                    -> anID_ProcessSubjectTask.add(oProcessSubject.getnID_ProcessSubjectTask()));
            LOG.debug("anID_ProcessSubjectTask={}", anID_ProcessSubjectTask);
            LOG.info("anID_ProcessSubjectTask={}", anID_ProcessSubjectTask);
            
            for (Long nID_ProcessSubjectTask : anID_ProcessSubjectTask) {
                ProcessSubjectTask oProcessSubjectTask = oProcessSubjectTaskDao.findById(nID_ProcessSubjectTask).orNull();
                if (oProcessSubjectTask != null) {
                    LOG.info("aoProcessSubjectTask={}", aoProcessSubjectTask);
                    aoProcessSubjectTask.add(oProcessSubjectTask);
                }
            }
            
            boolean isHistory = false;
            
            if(aoProcessSubjectTask.isEmpty()){
               aoProcessSubjectTask.addAll(oProcessSubjectTaskDao.findAllBy("snID_Process_Activiti_Root", snID_Process_Activiti));
               isHistory = true;
            }
            
            LOG.debug("aoProcessSubjectTask={}", aoProcessSubjectTask);

            LOG.info("aoProcessSubjectTask={}", aoProcessSubjectTask);
            Collections.sort(aoProcessSubjectTask, (a, b) -> a.getId() < b.getId() ? -1 : a.getId() == b.getId() ? 0 : 1);
            LOG.info("AFTER SORT aoProcessSubjectTask={}", aoProcessSubjectTask);

            for (ProcessSubjectTask oProcessSubjectTask : aoProcessSubjectTask) {
                List<ProcessSubject> aProcessSubject = oProcessSubjectDao.
                        findAllBy("nID_ProcessSubjectTask", oProcessSubjectTask.getId());
                
                if(isHistory){
                    //if we search in history (or in viewed) - we shoud find task with a min process id (before delegating)
                    Comparator<ProcessSubject> cmp = new Comparator<ProcessSubject>() {
                        @Override
                        public int compare(ProcessSubject ProcessSubject1, ProcessSubject ProcessSubject2) {
                            return Long.valueOf(ProcessSubject1.getSnID_Process_Activiti()).compareTo(Long.valueOf(ProcessSubject2.getSnID_Process_Activiti()));
                        }
                    };
                    
                    snID_Process_Activiti = Collections.min(aProcessSubject, cmp).getSnID_Process_Activiti();
                    
                }
                
                for (ProcessSubject oProcessSubject : aProcessSubject) {
                    LOG.info("oProcessSubject after getting is {} sloginRole is {}", oProcessSubject.getsLogin(), oProcessSubject.getsLoginRole());
                }

                LOG.debug("aProcessSubject.size={}", aProcessSubject.size());
                //заполнение иерархии ProcessSubject
                aProcessSubject.forEach(oProcessSubject -> {
                    LOG.info("oProcessSubject id to setting is {}", oProcessSubject.getId());
                    setProcessSubjectUserAndChild(oProcessSubject);
                });
                //сетим ProcessSubject- только рутовый (верх иерархии) в ProcessSubjectTask
                
                final String snID_Process_Activiti_Root = snID_Process_Activiti;
                List<ProcessSubject> aoProcessSubjectRoot = aProcessSubject.stream()
                        .filter(oProcessSubject -> oProcessSubject.getSnID_Process_Activiti().equals(snID_Process_Activiti_Root))
                        .collect(Collectors.toList());

                for (ProcessSubject oProcessSubject : aoProcessSubjectRoot) {
                    LOG.info("oProcessSubject in aoProcessSubjectRoot is {} sloginRole is {}", oProcessSubject.getsLogin(), oProcessSubject.getsLoginRole());
                }
                oProcessSubjectTask.setaProcessSubject(aoProcessSubjectRoot);
            }
        } catch (Exception ex) {
            LOG.error("Error in getProcessSubjectTask {}", ex.getMessage());
        }
        return aoProcessSubjectTask;
    }

    private void saveProcessSubjectTree(ProcessSubject oProcessSubjectParent, ProcessSubject oProcessSubjectChild) {
        LOG.info("oProcessSubjectParent is {}", oProcessSubjectParent.getId());
        LOG.info("oProcessSubjectChild is {}", oProcessSubjectChild.getId());
        ProcessSubjectTree oProcessSubjectTreeParent = new ProcessSubjectTree();
        oProcessSubjectTreeParent.setProcessSubjectParent(oProcessSubjectParent);
        oProcessSubjectTreeParent.setProcessSubjectChild(oProcessSubjectChild);
        LOG.info("oProcessSubjectTreeParent is {}", oProcessSubjectTreeParent);
        oProcessSubjectTreeDao.saveOrUpdate(oProcessSubjectTreeParent);
    }

    /**
     * Заполнить aUser и aProcessSubjectChild.
     *
     * @param oProcessSubject заполняемый обьект
     */
    private void setProcessSubjectUserAndChild(ProcessSubject oProcessSubject) {
        LOG.info("setProcessSubjectUserAndChild start");
        List<ProcessSubject> aoProcessSubjectChild = new ArrayList<>();
        oProcessSubject.setaUser(oProcessSubjectService.getUsersByGroupSubject(oProcessSubject.getsLogin()));
        List<ProcessSubjectTree> aoProcessSubjectTree = oProcessSubjectTreeDao
                .findAllBy("processSubjectParent", oProcessSubject);
        
        for(ProcessSubjectTree oProcessSubjectTree : aoProcessSubjectTree){
            LOG.info("parent {}, child {}", oProcessSubjectTree.getProcessSubjectParent().getId(), oProcessSubjectTree.getProcessSubjectChild().getId());
        }
        
        aoProcessSubjectTree.forEach(oProcessSubjectTree -> {
            aoProcessSubjectChild.add(oProcessSubjectTree.getProcessSubjectChild());
        });
        oProcessSubject.setaProcessSubjectChild(aoProcessSubjectChild);
        aoProcessSubjectChild.forEach(oProcessSubjectChild -> setProcessSubjectUserAndChild(oProcessSubjectChild));
    }

    private void sendEmail(ProcessSubjectTask oProcessSubjectTask, List<ProcessSubject> aProcessSubject) {
        LOG.info("sendEmail start");
        String sBody = oProcessSubjectTask.getsBody();

        JSONParser parser = new JSONParser();
        String sBodyTask = " ";
        List<String> aExecutors = new ArrayList<String>();
        String sExecutors = "";

        for (ProcessSubject oSubjectProcess : aProcessSubject) {
            String sLoginRole = oSubjectProcess.getsLoginRole();
            String sLogin = oSubjectProcess.getsLogin();
            SubjectGroup oSubjectGroup = oSubjectGroupDao.findBy("sID_Group_Activiti", sLogin).orNull();
            String sName = oSubjectGroup.getName();

            if (sLoginRole.equals("Executor")) {
                aExecutors.add(sName);
            }
        }

        for (ProcessSubject oProcessSubject : aProcessSubject) {

            try {
                org.json.simple.JSONObject oTableJSONObject = (org.json.simple.JSONObject) parser.parse(sBody);
                MultipartFile oMultipartFile = oAttachmetService.getAttachment(null, null,
                        (String) oTableJSONObject.get("sKey"), (String) oTableJSONObject.get("sID_StorageType"));

                sBodyTask = new String(oMultipartFile.getBytes(), "UTF-8");
            } catch (Exception e) {
                LOG.info("sendEmail getAttachment: {}" + e.getMessage());
            }

            String sLogin = oProcessSubject.getsLogin();

            String sLoginRole = oProcessSubject.getsLoginRole();
            if (sLoginRole.equals("Controller")) {
                sExecutors = String.join("; ", aExecutors);
            } else if (sLoginRole.equals("Executor")) {
                sExecutors = "";
            }
            
            LOG.info("dDate: " + oProcessSubject.getsDatePlan());
            Date dDate = null;
            
            if(oProcessSubject.getsDatePlan() != null){
                dDate = oProcessSubject.getsDatePlan().toDate();
            }
            //Date dDate = new Date();
            
            Long nID_Process_Activiti = Long.valueOf(oProcessSubject.getSnID_Process_Activiti());
            oEmailProcessSubjectService.sendEmail_createTask(sLogin, sBodyTask, sExecutors, dDate, nID_Process_Activiti);
        }
    }

    private void removeProcessSubjectFromAccept(ProcessSubject oProcessSubject) throws Exception {
        if (oProcessSubject.getSnID_Task_Activiti() == null) {
            //removing logins in task from assigne
            LOG.info("removeProcessSubjectFromAccept start");
            List<DocumentStep> aDocumentStep
                    = oDocumentStepDao.findAllBy("snID_Process_Activiti", oProcessSubject.getSnID_Process_Activiti());

            for (DocumentStep oDocumentStep : aDocumentStep) {
                if(!oDocumentStep.getsKey_Step().equals("_")){
                    LOG.info("oDocumentStep is {}", oDocumentStep.getsKey_Step());
                    List<DocumentStepSubjectRight> aDocumentStepSubjectRight_ToDelete = new ArrayList<>();
                    List<DocumentStepSubjectRight> aDocumentStepSubjectRight = oDocumentStep.aDocumentStepSubjectRight();
                    for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight) {
                        if (oDocumentStepSubjectRight.getsID_Field() == null
                                && oDocumentStepSubjectRight.getsKey_GroupPostfix().equals(oProcessSubject.getsLogin())
                                && oDocumentStepSubjectRight.getsDate() == null) {
                            oDocumentStepService.removeDocumentStepSubject(oProcessSubject.getSnID_Process_Activiti(),
                                    oDocumentStep.getsKey_Step(), oProcessSubject.getsLogin(), null);
                            break;
                        }
                    }
                }
            }
        }
    }

}
