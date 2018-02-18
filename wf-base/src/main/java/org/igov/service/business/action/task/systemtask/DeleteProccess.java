/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.business.action.task.systemtask;

/**
 *
 * @author olya
 */

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.igov.io.GeneralConfig;
import org.igov.service.business.document.DocumentStepService;
import org.igov.service.business.process.processLink.ProcessLinkService;
import org.igov.service.exception.CRCInvalidException;
import org.igov.util.ToolLuna;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.AccessControlException;
import java.util.*;
import java.util.stream.Collectors;

import static org.igov.service.business.action.task.core.AbstractModelTask.getStringFromFieldExpression;

@Component("deleteProccess")
public class DeleteProccess implements JavaDelegate {

    private final static Logger LOG = LoggerFactory.getLogger(DeleteProccess.class);

    @Autowired private RuntimeService runtimeService;
    @Autowired private HistoryService historyService;
    @Autowired private GeneralConfig generalConfig;
    @Autowired private DocumentStepService oDocumentStepService;
    @Autowired private ProcessLinkService oProcessLinkService;
    @Autowired private TaskService oTaskService;

    public Expression processDefinitionKey;

    private int limitCountRowDeleted = 200000;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String processDefinitionKeyValue = getStringFromFieldExpression(this.processDefinitionKey, execution);
        closeProcess(processDefinitionKeyValue);
    }

    public void closeProcess(String processDefinitionKeyValue) throws Exception {
        //if (generalConfig.isSelfTest()) {
        //List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
        ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();
        if (processDefinitionKeyValue == null || "".equals(processDefinitionKeyValue.trim())
                || (!generalConfig.isSelfTest() && "all".equals(processDefinitionKeyValue.trim()))) {
            LOG.info("You don't have access for this operation! processDefinitionKeyValue: " + processDefinitionKeyValue);
            throw new AccessControlException("У Вас нет прав на данную операцию!");
        } else if (!"all".equals(processDefinitionKeyValue.trim())) {
            LOG.info("Delete all active proccess with processDefinitionKeyValue: " + processDefinitionKeyValue);
            processInstanceQuery.processDefinitionKey(processDefinitionKeyValue);
        }
        List<ProcessInstance> processInstances;
        int countRowDeleted = 0;
        int index = 0;
        int size = 1000;
        do {
            size = (limitCountRowDeleted < size ? limitCountRowDeleted : size);
            LOG.info("processInstances processInstanceQuery...");
            LOG.info("processInstances processInstanceQuery: index={},size={}", index, size);
            processInstances = processInstanceQuery.listPage(index, size);
            LOG.info("processInstances processInstanceQuery: processInstances.size()={}", processInstances.size());
            for (ProcessInstance processInstance : processInstances) {
                closeProcessInstance(processInstance.getProcessInstanceId(), null);
                countRowDeleted++;
            }
            LOG.info("processInstances processInstanceQuery size: " + processInstances.size() + " countRowDeleted: " + countRowDeleted + " success!");
            //index = ++index + size;
        } while (!processInstances.isEmpty() && countRowDeleted <= limitCountRowDeleted);
        LOG.info("FINISHED!!! processInstances processInstanceQuery size: countRowDeleted: " + countRowDeleted);

        //}
    }

    public void setLimitCountRowDeleted(int limitCountRowDeleted) {
        this.limitCountRowDeleted = limitCountRowDeleted;
    }

    public void closeProcessInstance(String snID_Process_Activiti, String sLogin) throws Exception {
        LOG.info("closeProcessInstance...");
        List<Task> aoTask = oTaskService.createTaskQuery().processInstanceId(snID_Process_Activiti).active().list();
        //даем права администратору на отображение таски в истории в удаленных и на просмотр документа
        for (Task oTask : aoTask) {
            oTaskService.addCandidateGroup(oTask.getId(), "admin");
            if (sLogin != null && !"".equals(sLogin.trim())) {
                oDocumentStepService.delegateDocumentStepSubject(snID_Process_Activiti, "_", sLogin, "admin", "AddViewer");
            }
        }
        //удаляем внешние таски (устанавливаем статус, что удален)
        if (sLogin != null && !"".equals(sLogin.trim())) {
            oProcessLinkService.setExternalProcessLinkStatus(snID_Process_Activiti, "deleted", sLogin);
        }
        //удаляем таски активити
        runtimeService.deleteProcessInstance(snID_Process_Activiti, "deleted");
    }

    public void deleteHistoricProcessInstance(String snID_Order, String sID_Process_Def) throws CRCInvalidException {
        LOG.info("deleteHistoricProcessInstance started...");
        try {
            List<String> asID_Process_Activiti = new ArrayList<>();

            if (snID_Order != null && !"".equals(snID_Order.trim())) {
                String sID_Process_Activiti = String.valueOf(ToolLuna.getValidatedOriginalNumber(Long.parseLong(snID_Order)));
                asID_Process_Activiti.add(sID_Process_Activiti);
                LOG.info("sID_Process_Activiti in deleteHistoricProcessInstance {}", sID_Process_Activiti);
            } else if (sID_Process_Def != null && !"".equals(sID_Process_Def.trim())) {
                List<HistoricProcessInstance> aHistoricProcessInstance
                        = historyService.createHistoricProcessInstanceQuery().processDefinitionKey(sID_Process_Def).list();
                for (HistoricProcessInstance oHistoricProcessInstance : aHistoricProcessInstance) {
                    asID_Process_Activiti.add(oHistoricProcessInstance.getId());
                }
            }
            for (String sID_Process_Activiti : asID_Process_Activiti) {
                LOG.info("Process to deleting {}", sID_Process_Activiti);
                //удаляем внешнюю таску, если находим внешнего SubjectHuman
                oProcessLinkService.removeExternalProcessLinks(sID_Process_Activiti, null, null);
                //удаляем степы и права
                oDocumentStepService.removeDocumentSteps(sID_Process_Activiti);
                //удаление процесса из активи (история и рантайм)
                if (runtimeService.createProcessInstanceQuery().processInstanceId(sID_Process_Activiti)
                        .active().singleResult() != null) {
                    runtimeService.deleteProcessInstance(sID_Process_Activiti, "deleted");
                }
                if (historyService.createHistoricProcessInstanceQuery().processInstanceId(sID_Process_Activiti)
                        .singleResult() != null) {
                    historyService.deleteHistoricProcessInstance(sID_Process_Activiti);
                }
            }

        } catch (Exception ex) {
            LOG.error("Error during order deliting: {}", ex);
            throw new RuntimeException("Error during order deliting: " + ex.getMessage());
        }
    }
    
    public Map<String, Object> removeAllProcess() throws Exception {
        LOG.info("Remove all process start...");
        long start = System.currentTimeMillis();
        Map<String, Object> mResponse = new HashMap<>();
        /*
        List<ProcessLink> aoProcessLink = oProcessLinkDao.findAll();
        Integer nExternalTaskCount = aoProcessLink.size();
        LOG.info("nExternalTaskCount={}", nExternalTaskCount);
        oProcessLinkDao.delete(aoProcessLink);*/
        
        List<HistoricProcessInstance> aoHistoricProcess = historyService.createHistoricProcessInstanceQuery()
                .finished()
                .list();
        LOG.info("aoProcess history count {}", aoHistoricProcess.size());
        List<ProcessInstance> aoProcess = runtimeService.createProcessInstanceQuery().active().list();
        LOG.info("aoProcess count {}", aoProcess.size());
        Set<String> asProcessDefinitionKey = aoHistoricProcess.stream()
                .map(oProcess -> oProcess.getProcessDefinitionId().split(":")[0])
                .collect(Collectors.toSet());
        asProcessDefinitionKey.addAll(aoProcess.stream()
                .map(oProcess -> oProcess.getProcessDefinitionId().split(":")[0])
                .collect(Collectors.toSet())
        );
        LOG.info("Key count {} asHistoryProcessDefinitionKey={}", asProcessDefinitionKey.size(), asProcessDefinitionKey);
        Integer nProcessCount = aoHistoricProcess.size() + aoProcess.size();
        long nTaskCount = 0;
        
        for (HistoricProcessInstance oHistoricProcess : aoHistoricProcess) {
            //удаляем внешнюю таску, если находим внешнего SubjectHuman
            oProcessLinkService.removeExternalProcessLinks(oHistoricProcess.getId(), null, null);
            nTaskCount = nTaskCount + historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(oHistoricProcess.getId())
                    .count();
            if (historyService.createHistoricProcessInstanceQuery().processInstanceId(oHistoricProcess.getId())
                    .singleResult() != null) {
                historyService.deleteHistoricProcessInstance(oHistoricProcess.getId());
            }
            if (oHistoricProcess.getProcessDefinitionId().startsWith("_doc_")) {
                oDocumentStepService.removeDocumentSteps(oHistoricProcess.getId());
            }        
        }
        for (ProcessInstance oProcess : aoProcess) {
            //удаляем внешнюю таску, если находим внешнего SubjectHuman
            oProcessLinkService.removeExternalProcessLinks(oProcess.getProcessInstanceId(), null, null);
            nTaskCount = nTaskCount + historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(oProcess.getId())
                    .count();
            runtimeService.deleteProcessInstance(oProcess.getId(), "deleted");
            if (oProcess.getProcessDefinitionId().startsWith("_doc_")) {
                oDocumentStepService.removeDocumentSteps(oProcess.getId());
            }
        }
       
        //mResponse.put("nExternalTaskCount", nExternalTaskCount);
        mResponse.put("nTaskCount", nTaskCount);
        mResponse.put("nProcessCount", nProcessCount);
        mResponse.put("nProcessDefinitionCount", asProcessDefinitionKey.size());
        mResponse.put("asProcessDefinitionKeys", asProcessDefinitionKey);
       
        long stop = System.currentTimeMillis();
        long nElapsedTime = stop - start;
        mResponse.put("nElapsedTime", nElapsedTime);
        
        return mResponse;
    }
}
