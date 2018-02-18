package org.igov.service.business.action.task.systemtask;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.igov.service.business.subject.SubjectRightBPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("doRightBP")
public class DoRightBP implements JavaDelegate {

    private static final String ACTION_SET = "set";
    private static final String ACTION_REMOVE = "remove";

    @Autowired
    private SubjectRightBPService subjectRightBPService;

    Expression sID_Action;
    Expression sID_BP;
    Expression sID_Group_Referent;

    @Override
    public void execute(DelegateExecution oExecution) throws Exception {
        String sID_Action_value = (String) sID_Action.getValue(oExecution);
        String sID_BP_value = (String) sID_BP.getValue(oExecution);
        String sID_Group_Referent_value = (String) sID_Group_Referent.getValue(oExecution);
        if (ACTION_SET.equalsIgnoreCase(sID_Action_value)) {
            subjectRightBPService.setBP(sID_BP_value, sID_Group_Referent_value, null);
        } else if (ACTION_REMOVE.equalsIgnoreCase(sID_Action_value)) {
            subjectRightBPService.removeBP(sID_BP_value, sID_Group_Referent_value, null);
        } else {
            throw new RuntimeException("action is not specified");
        }
    }

}
