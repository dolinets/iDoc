package org.igov.service.business.action.task.systemtask.mail;

import java.util.List;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.igov.model.document.image.DocumentImageFileVO;
import org.springframework.stereotype.Component;
import static org.igov.service.business.action.task.core.AbstractModelTask.getStringFromFieldExpression;

/**
 * @author BW
 */
@Component("SendLinkToDocument")
public class SendLinkToDocument extends Abstract_MailTaskCustom {

    private Expression saAttachmentsForSend;

    @Override
    public void execute(DelegateExecution oExecution) throws Exception {
        sendMailWithAttachments(oExecution);
        LOG.info("MailTaskWithAttachments ok!");
    }

    protected void sendMailWithAttachments(DelegateExecution oExecution) throws Exception {
        String sAttachmentsForSend = getStringFromFieldExpression(this.saAttachmentsForSend, oExecution);
        
        if (oExecution.getProcessDefinitionId().split(":")[0].equals("subsidies_Ukr_result")
                && (sAttachmentsForSend == null || "".equals(sAttachmentsForSend.trim()))) {
            throw new RuntimeException("Не найден файл для отправки! Он обязателен!");
        }
        
        LOG.info("Process id is {} sAttachmentsForSend after arriving in MailTaskWithAttachments {}",
                oExecution.getProcessInstanceId(), sAttachmentsForSend);
        
        sendMail_LinkToDocument(oExecution, sAttachmentsForSend);
    }
}
