package org.igov.service.business.action.task.listener.doc;

import static org.igov.service.business.action.task.core.AbstractModelTask.getStringFromFieldExpression;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.igov.service.business.nais.NaisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("MinJust_GetServiceUrl")
public class MinJust_GetServiceUrl implements TaskListener {

    private static final long serialVersionUID = 1L;

    private final static Logger LOG = LoggerFactory.getLogger(MinJust_GetServiceUrl.class);

    private Expression sID_NAIS_Service_code;

    private Expression sID_NAIS_Application_id;

    @Autowired
    private NaisService naisService;

    @Override
    public void notify(DelegateTask delegateTask) {
        DelegateExecution execution = delegateTask.getExecution();
        String sID_NAIS_Service_code_value = getStringFromFieldExpression(this.sID_NAIS_Service_code, execution);
        String sID_NAIS_Application_id_value = getStringFromFieldExpression(this.sID_NAIS_Application_id, execution);
        String result = "[none]";
        try {
            result = naisService.getServiceURL(sID_NAIS_Service_code_value, sID_NAIS_Application_id_value);
            execution.setVariable("result", result);
        } catch (Exception e) {
            LOG.error("!!! Error in MinJust_GetServiceUrl", e);
            execution.setVariable("result", result);
        }
    }

}
