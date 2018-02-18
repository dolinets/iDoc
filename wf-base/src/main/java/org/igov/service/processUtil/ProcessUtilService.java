package org.igov.service.processUtil;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Task;

import org.igov.io.GeneralConfig;

import org.igov.service.business.action.task.core.ActionTaskService;
import org.igov.service.exception.CRCInvalidException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component("processUtilService")
@Service
public class ProcessUtilService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessUtilService.class);

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ActionTaskService oActionTaskService;

    @Autowired
    private GeneralConfig generalConfig;

    public Map<String, Object> getmID_TaskAndProcess(String sID_Order, String nID_Process) throws CRCInvalidException {
        LOG.info("getmID_TaskAndProcess started with sID_Order={}, nID_Process={}", sID_Order, nID_Process);
        if (sID_Order != null) {
            Long nID_Order = Long.valueOf(sID_Order.substring(2));
            nID_Process = oActionTaskService.getOriginalProcessInstanceId(nID_Order);
        }

        Map<String, Object> resulMap = new HashMap<>();

        resulMap.put("nID_Process", nID_Process);
        String nID_Task_Active = null;
        List<Task> aTask = taskService.createTaskQuery().processInstanceId(nID_Process).active().list();
        if (aTask != null && aTask.size() > 0) {
            nID_Task_Active = aTask.get(0).getId();
        }
        LOG.info("nID_Task_Active={}", nID_Task_Active);
        resulMap.put("nID_Task_Active", nID_Task_Active);

        List<HistoricTaskInstance> aHistoricTaskInstance = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(nID_Process)
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();

        String snID_Task_HistoryLast = null;

        if (aHistoricTaskInstance != null && aHistoricTaskInstance.size() > 0) {
            if (resulMap.get("nID_Task_Active") == null) {
                snID_Task_HistoryLast = aHistoricTaskInstance.get(0).getId();

            } else {
                if (aHistoricTaskInstance.size() > 1) {
                    snID_Task_HistoryLast = aHistoricTaskInstance.get(1).getId();
                }
            }
        }
        resulMap.put("nID_Task_HistoryLast", snID_Task_HistoryLast);
        LOG.info("snID_Task_HistoryLast={}", snID_Task_HistoryLast);

        resulMap.put("sID_Order", generalConfig.getOrderId_ByProcess(Long.parseLong(nID_Process)));
        LOG.info("sID_Order={}", sID_Order);

        return resulMap;
    }

}
