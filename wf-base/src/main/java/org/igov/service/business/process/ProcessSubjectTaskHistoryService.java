
package org.igov.service.business.process;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.task.Task;
import org.igov.io.GeneralConfig;
import static org.igov.model.action.event.HistoryEvent_ServiceDaoImpl.DASH;
import org.igov.model.action.event.HistoryEvent_Service_StatusType;
import org.igov.model.process.ProcessSubject;
import org.igov.model.process.ProcessSubjectTask;
import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectGroupDao;
import org.igov.service.business.action.event.ActionEventHistoryService;
import org.igov.service.business.action.task.core.UsersService;
import org.igov.util.ToolLuna;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Oleksandr Belichenko
 */
@Service
public class ProcessSubjectTaskHistoryService {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessSubjectTaskHistoryService.class);
    
    @Autowired
    private TaskService oTaskService;
    
    @Autowired
    private GeneralConfig oGeneralConfig;
    
    @Autowired
    private HistoryService oHistoryService;
    
    @Autowired
    private SubjectGroupDao oSubjectGroupDao;
    
    @Autowired
    private RuntimeService oRuntimeService;
    
    @Autowired
    private UsersService oUsersService;
    
    @Autowired
    private ActionEventHistoryService oActionEventHistoryService;
    
    public void setInHistory(List<ProcessSubject> aProcessSubject, ProcessSubjectTask oProcessSubjectTask, Map<String, Object> mParamTask, String sAction) throws Exception {
        try {
            LOG.info("setInHistory started...");
            LOG.info("aProcessSubject: {}", aProcessSubject);
            LOG.info("oProcessSubjectTask: {}", oProcessSubjectTask);
            LOG.info("mParamTask: {}", mParamTask);
            
            String snID_ProcessSubjectTask = "";
            if(mParamTask.get("snID_ProcessSubjectTask") != null){
                snID_ProcessSubjectTask = mParamTask.get("snID_ProcessSubjectTask").toString();
            }
            String snID_Process_Activiti_Root = "";
            if(mParamTask.get("snID_Process_Activiti_Root") != null){
                snID_Process_Activiti_Root = mParamTask.get("snID_Process_Activiti_Root").toString();
            }
           
            
            ProcessSubject oProcessSubject = aProcessSubject.get(0);
            String snID_Process_Activiti = oProcessSubject.getSnID_Process_Activiti();
            if (snID_Process_Activiti == null) {
                if (mParamTask.get("snID_Process_Activiti_Parent") != null) {
                    snID_Process_Activiti = mParamTask.get("snID_Process_Activiti_Parent").toString();
                }
            }
            

            Map<String, String> mParam = new HashMap<>();
            mParam.put("nID_StatusType", HistoryEvent_Service_StatusType.CREATED.getnID().toString());
            mParam.put("sID_Process", snID_Process_Activiti);
            
            String sID_Order = "";
            if (snID_Process_Activiti != null) {
                sID_Order = oGeneralConfig.getOrderId_ByProcess(Long.parseLong(snID_Process_Activiti));
            } 
            
            
            mParam.put("sID_Order", sID_Order);

            List<Task> aTask = oTaskService.createTaskQuery().processInstanceId(snID_Process_Activiti).active().list();
            boolean bProcessClosed = aTask == null || aTask.size() == 0;
            String sUserTaskName = bProcessClosed ? "закрита" : aTask.get(0).getName();

           // HistoricVariableInstance historicVariableInstance = null;
            
            /*if(mParamTask.get("sID_Order_Document") != null){
                historicVariableInstance = oHistoryService.createHistoricVariableInstanceQuery().
                    processInstanceId((String)mParamTask.get("snID_Process_Activiti_Root")).variableName("sLoginAuthor").singleResult();
            }
            */
            String sDatePlan = oProcessSubject.getsDateEdit().toString().substring(0, 10);          
            
            /*String sLogin = null;

            if (historicVariableInstance != null) {
                sLogin = historicVariableInstance.getValue().toString();
                LOG.info("sLogin: {}", sLogin);
            }else{
                return;
            }

            SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLogin);
            if (oSubjectGroup != null) {
                String sName = oSubjectGroup.getoSubject().getsLabel();
                mParam.put("sLoginNew", sLogin);
                mParam.put("sName", sName);
            }*/
            
            mParam.put("sUserTaskName", sUserTaskName);            
            mParam.put("sID_Order", sID_Order);
            
            if (sAction.equals("start")) {    
                mParam.put("nID_HistoryEventType", "43");
                mParam.put("newData", sDatePlan);
                if (oRuntimeService.hasVariable(snID_Process_Activiti_Root, "TaskCreatorLogin")) {
                    JSONObject mTaskCreatorLogin = new JSONObject();
                    mTaskCreatorLogin = (JSONObject) oRuntimeService.getVariable(snID_Process_Activiti_Root, "TaskCreatorLogin");
                    LOG.info("mTaskCreatorLogin present: {}", mTaskCreatorLogin);
                    String sLogin = (String) mTaskCreatorLogin.get(oProcessSubjectTask.getId());
                    LOG.info("sLogin: {}", sLogin);
                    SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLogin);
                    if (oSubjectGroup != null) {
                        String sName = oSubjectGroup.getoSubject().getsLabel();
                        String sInitials = oUsersService.getUserInitials(sName);
                        mParam.put("sLoginNew", sInitials);
                        mParam.put("sName", sInitials);
                    }
                }
                //oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 43L);
                oActionEventHistoryService.doRemoteRequestRegion("/wf/service/history/document/event/addHistoryEvent", mParam);
            } else if (sAction.equals("create")) {
                mParam.put("newData", sDatePlan);
                mParam.put("nID_HistoryEventType", "43");
                String sLoginAuthor = "";
                if (mParamTask.get("sLoginAuthor") != null) {
                    sLoginAuthor = mParamTask.get("sLoginAuthor").toString();
                    LOG.info("sLoginAuthor: {}", sLoginAuthor);
                    SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLoginAuthor);
                    if (oSubjectGroup != null) {
                        String sName = oSubjectGroup.getoSubject().getsLabel();
                        String sInitials = oUsersService.getUserInitials(sName);
                        mParam.put("sLoginNew", sInitials);
                        mParam.put("sName", sInitials);
                    }
                }
                oActionEventHistoryService.doRemoteRequestRegion("/wf/service/history/document/event/addHistoryEvent", mParam);
            } else if (sAction.equals("edit")) {
                mParam.put("newData", sDatePlan);
                mParam.put("nID_HistoryEventType", "44");
                String sLoginAuthor = "";
                if (mParamTask.get("sLoginPrincipal") != null) {
                    sLoginAuthor = mParamTask.get("sLoginPrincipal").toString();
                    LOG.info("sLoginAuthor: {}", sLoginAuthor);
                    SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLoginAuthor);
                    if (oSubjectGroup != null) {
                        String sName = oSubjectGroup.getoSubject().getsLabel();
                        String sInitials = oUsersService.getUserInitials(sName);
                        mParam.put("sLoginNew", sInitials);
                        mParam.put("sName", sInitials);
                    }
                }
                oActionEventHistoryService.doRemoteRequestRegion("/wf/service/history/document/event/addHistoryEvent", mParam);                
            } else if (sAction.equals("delegate")) {
                mParam.put("nID_HistoryEventType", "45");
                String sLoginAuthor = "";
                if (mParamTask.get("sLoginPrincipal") != null) {
                    sLoginAuthor = mParamTask.get("sLoginPrincipal").toString();
                    LOG.info("sLoginAuthor: {}", sLoginAuthor);
                    SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLoginAuthor);
                    if (oSubjectGroup != null) {
                        String sName = oSubjectGroup.getoSubject().getsLabel();
                        String sInitials = oUsersService.getUserInitials(sName);
                        mParam.put("sLoginNew", sInitials);
                        mParam.put("sName", sInitials);
                    }
                } 
                String sLoginDelegate = "";
                for (ProcessSubject oProcessSubjectDelegate : aProcessSubject) {
                    String sRole = oProcessSubjectDelegate.getsLoginRole();
                    if (sRole.equals("Executor")) {
                        sLoginDelegate = oProcessSubjectDelegate.getsLogin();
                    }

                }
                SubjectGroup oSubjectGroup = oSubjectGroupDao.findByExpected("sID_Group_Activiti", sLoginDelegate);
                if (oSubjectGroup != null) {
                    String sName = oSubjectGroup.getoSubject().getsLabel();
                    String sInitials = oUsersService.getUserInitials(sName);
                    mParam.put("sLogin", sInitials);
                }                
                oActionEventHistoryService.doRemoteRequestRegion("/wf/service/history/document/event/addHistoryEvent", mParam);
            } else {
                LOG.info("sAction is wrong");
            }

            //oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 43L);
        } catch (Exception e) {
            LOG.info("Error during writing setInHistory: {}", e);
        }
    }
    
}
