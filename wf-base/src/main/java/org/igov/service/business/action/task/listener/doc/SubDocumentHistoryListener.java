package org.igov.service.business.action.task.listener.doc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.igov.model.document.DocumentStepSubjectRight;
import org.igov.service.business.action.event.ActionEventHistoryService;
import org.igov.io.GeneralConfig;
import org.igov.model.action.event.HistoryEvent_Service_StatusType;
import static org.igov.service.business.action.task.core.AbstractModelTask.getStringFromFieldExpression;
import org.igov.service.business.document.DocumentStepService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Kovilin
 */

/*
*
@Component("SubDocumentHistoryListener")
public class SubDocumentHistoryListener implements TaskListener{
    
    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(SetTasks.class);
    private Expression sID_Created_BP;
    
    @Autowired
    private ActionEventHistoryService oActionEventHistoryService;
    
    @Autowired
    private GeneralConfig generalConfig;
    
    @Autowired
    private TaskService taskService;
    
    @Override
    public void notify(DelegateTask delegateTask) {
        
        LOG.info("We are in SubDocumentHistoryListener");
        
        String sID_Created_BP_value = getStringFromFieldExpression(this.sID_Created_BP, delegateTask.getExecution());        
        LOG.info("sID_Created_BP_value: {}", sID_Created_BP_value);
        
        String processInstanceId = delegateTask.getProcessInstanceId();        
        LOG.info("processInstanceId: {}", processInstanceId);
        
        List<Task> aTask = taskService.createTaskQuery().processInstanceId(processInstanceId).active().list();
        LOG.info("aTask: {}", aTask);
        boolean bProcessClosed = aTask == null || aTask.size() == 0;
        LOG.info("bProcessClosed: {}", bProcessClosed);
        String sUserTaskName = bProcessClosed ? "закрита" : aTask.get(0).getName();
        LOG.info("sUserTaskName: {}", sUserTaskName);
        
        Map<String, String> mParam = new HashMap<>();
        String sID_Order = generalConfig.getOrderId_ByProcess(Long.parseLong(processInstanceId));
        LOG.info("sID_Order: {}", sID_Order);
        mParam.put("nID_StatusType", HistoryEvent_Service_StatusType.CREATED.getnID().toString());
        mParam.put("new_BP_ID", sID_Created_BP_value);
        LOG.info("mParam: {}", mParam);
        
        try {
            oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 15L);
        } catch (Exception ex) {
            LOG.info("Exception in SubDocumentHistoryListener: {}", ex);
            Logger.getLogger(SubDocumentHistoryListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void setHistory(DelegateExecution oExecution){
    }
}
*/

@Component("SubDocumentHistoryListener")
public class SubDocumentHistoryListener implements JavaDelegate, TaskListener {

    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(SetTasks.class);
    private Expression sID_Created_BP;

    @Autowired
    private ActionEventHistoryService oActionEventHistoryService;

    @Autowired
    private GeneralConfig generalConfig;

    @Autowired
    private TaskService taskService;

    @Override
    public void execute(DelegateExecution oDelegateExecution) throws Exception {
        LOG.info("SubDocumentHistoryListener start in execute....");
        setHistory(oDelegateExecution);
    }

    private void setHistory(DelegateExecution oDelegateExecution) {       
        String sID_Created_BP_value = getStringFromFieldExpression(this.sID_Created_BP, oDelegateExecution);       
        String processInstanceId = oDelegateExecution.getProcessInstanceId();
        
        List<Task> aTask = taskService.createTaskQuery().processInstanceId(processInstanceId).active().list();
        boolean bProcessClosed = aTask == null || aTask.size() == 0;
        String sUserTaskName = bProcessClosed ? "закрита" : aTask.get(0).getName();
        
        Map<String, String> mParam = new HashMap<>();
        String sID_Order = generalConfig.getOrderId_ByProcess(Long.parseLong(processInstanceId));
        
        mParam.put("nID_StatusType", HistoryEvent_Service_StatusType.CREATED.getnID().toString());
        mParam.put("new_BP_ID", sID_Created_BP_value);
        

        try {
            oActionEventHistoryService.addHistoryEvent(sID_Order, sUserTaskName, mParam, 15L);
        } catch (Exception ex) {
            LOG.info("Exception in SubDocumentHistoryListener: {}", ex);
            Logger.getLogger(SubDocumentHistoryListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void notify(DelegateTask oDelegateTask) {
        LOG.info("SubDocumentHistoryListener start in notify....");
        setHistory(oDelegateTask.getExecution());
    }
}
