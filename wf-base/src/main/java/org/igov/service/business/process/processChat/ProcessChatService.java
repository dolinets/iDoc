package org.igov.service.business.process.processChat;

import java.util.List;
import org.igov.model.process.processChat.ProcessChat;
import org.igov.model.process.processChat.ProcessChatDao;
import org.igov.model.process.processChat.ProcessChatMessage;
import org.igov.model.process.processChat.ProcessChatResult;
import org.igov.service.business.email.EmailProcessSubjectService;
import org.igov.service.business.process.ProcessSubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("processChatService")
public class ProcessChatService {

    @Autowired
    private ProcessChatDao processChatDao;

    @Autowired
    private ProcessChatMessageService processChatMessageService;

    @Autowired
    private EmailProcessSubjectService oEmailProcessSubjectService;

    private static final Logger LOG = LoggerFactory.getLogger(ProcessSubjectService.class);

    /**
     * Save ProcessChat
     *
     * @param nID_Process_Activiti
     * @param sKeyGroup
     * @param sKeyGroup_Author
     * @param sBody
     * @param nID_ProcessChatMessage_Parent
     * @return
     */
    public ProcessChatMessage setProcessChat(Long nID_Process_Activiti, String sKeyGroup, String sKeyGroup_Author, String sLoginReferent,
            String sBody, Long nID_ProcessChatMessage_Parent) throws Exception{
        ProcessChatMessage oProcessChatMessage = null;
        try {
            ProcessChat oProcessChat = setProcessChat(nID_Process_Activiti, sKeyGroup);
            oProcessChatMessage = processChatMessageService.setProcessChatMessage(oProcessChat, sKeyGroup_Author, sLoginReferent, sBody, nID_ProcessChatMessage_Parent);
            oEmailProcessSubjectService.sendEmail_comment(nID_Process_Activiti, sKeyGroup, sKeyGroup_Author, sBody); 
        } catch (Exception e) {
            LOG.error("FAIL: ", e);
            throw e;
        }
        return oProcessChatMessage;
    }

    /**
     * Save ProcessChat
     *
     * @param nID_Process_Activiti
     * @param sKeyGroup
     * @return
     */
    private ProcessChat setProcessChat(Long nID_Process_Activiti, String sKeyGroup) {
        LOG.info(String.format("saving ProcessChat with nID_Process_Activiti=%s, and  sKeyGroup=%s", nID_Process_Activiti, sKeyGroup));
        ProcessChat oProcessChat = null;
        try {
            oProcessChat = processChatDao.findByProcessActivitiIdAndsKeyGroup(nID_Process_Activiti, sKeyGroup);
            if (oProcessChat == null) {
                oProcessChat = new ProcessChat();
                oProcessChat.setnID_Process_Activiti(nID_Process_Activiti);
                oProcessChat.setsKeyGroup(sKeyGroup);
                LOG.info(String.format("The new instance of ProcessChat with "
                        + "snID_Process_Activiti=%s, sKeyGroup=%s was created",
                        nID_Process_Activiti, sKeyGroup));
                oProcessChat = processChatDao.setProcessChat(oProcessChat);
            }
            LOG.info(String.format("Entity was added with id=%s", oProcessChat.getId()));
        } catch (Exception e) {
            LOG.error("(Fail set process {})", e.getMessage());
        }
        return oProcessChat;
    }

    /**
     * Get all ProcessChats maped by nID_Process_Activiti
     *
     * @param nID_Process_Activiti
     * @return
     */
    public ProcessChatResult findByProcess_Activiti(Long nID_Process_Activiti) {
        LOG.info(String.format("find ProcessChat with nID_Process_Activiti=%s", nID_Process_Activiti));
        List<ProcessChat> aProcessChat = processChatDao.findAllBy("nID_Process_Activiti", nID_Process_Activiti);

        aProcessChat = processChatMessageService.getCatalogProcessChatMessage(nID_Process_Activiti, aProcessChat);        

        ProcessChatResult processChatResult = new ProcessChatResult();
        processChatResult.setaProcessChat(aProcessChat);        
        return processChatResult;
    }
}
