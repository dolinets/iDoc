
package org.igov.service.business.action.task.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.igov.io.GeneralConfig;
import org.igov.model.action.event.HistoryEvent_Service_StatusType;
import org.igov.service.business.action.event.HistoryEventService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author olga
 */
@Component("updateStatus_HistoryServiceEvent")
public class UpdateStatus_HistoryServiceEvent implements JavaDelegate {

    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(UpdateStatus_HistoryServiceEvent.class);

    @Autowired
    HistoryEventService historyEventService;

    @Autowired
    GeneralConfig generalConfig;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try {
            String sID_order = generalConfig.getOrderId_ByProcess(Long.valueOf(execution.getProcessInstanceId()));
            LOG.info("sID_Order: " + sID_order);
            String result = historyEventService.updateHistoryEvent(sID_order, HistoryEvent_Service_StatusType.CLOSED, null);
            LOG.info("result: " + result);
        } catch (Exception ex) {
            LOG.error("updateStatus_HistoryServiceEvent fals!!!", ex);
        }
    }

}
