/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.business.action.task.listener.doc;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import static org.igov.service.business.action.task.core.AbstractModelTask.getStringFromFieldExpression;
import org.igov.service.business.dfs.DfsService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author olga
 */
@Component("TransferDocumentAnswer_SWinEd")
public class TransferDocumentAnswer_SWinEd implements TaskListener {
       
    private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(TransferDocumentAnswer_SWinEd.class);

    private Expression sINN;
    
    private Expression snCountYear;
    
    private Expression sFile_XML_SWinEd_Filter;

    @Autowired
    private DfsService dfsService;
    

    @Override
    public void notify(DelegateTask delegateTask) {
        LOG.info("TransferDocumentAnswer_SWinEd {}", delegateTask.getName());
        LOG.info("!!! delegateTask.getId(): " + delegateTask.getId());
        String sINN_Value = getStringFromFieldExpression(this.sINN, delegateTask.getExecution());
        String snCountYear_Value = getStringFromFieldExpression(this.snCountYear, delegateTask.getExecution());
        String sFile_XML_SWinEd_Filter_Value = getStringFromFieldExpression(this.sFile_XML_SWinEd_Filter, delegateTask.getExecution());
        String asID_Attach_Dfs = dfsService.getAnswer(delegateTask.getId(), delegateTask.getProcessInstanceId(), sINN_Value, snCountYear_Value, sFile_XML_SWinEd_Filter_Value);
        LOG.info("!!! delegateTask.getId(): " + delegateTask.getId() + " as: " + asID_Attach_Dfs);
    }

}
