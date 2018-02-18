package org.igov.service.business.process.processChat;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.igov.model.process.processChat.ProcessChatMessageTree;
import org.igov.model.process.processChat.ProcessChatMessageTreeDao;
import org.igov.service.business.process.ProcessSubjectTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component("processChatMessageTreeService")
@Service
public class ProcessChatMessageTreeService {

    @Autowired
    ProcessChatMessageTreeDao processChatMessageTreeDao;

    private static final Log LOG = LogFactory.getLog(ProcessSubjectTreeService.class);
    
    /**
     * Get ProcessChatMessageTree by nID_Process_Activiti
     *
     * @param nID_Process_Activiti
     * @return
     */
    public List<ProcessChatMessageTree> getProcessChatMessageRelations(Long nID_Process_Activiti){
        return processChatMessageTreeDao.findChildrenProcess_Activiti(nID_Process_Activiti);
    }
    
    /**
     * Get ProcessChatMessageTree by nID_Process_Activiti filtered nID_ProcessChat
     *
     * @param nID_Process_Activiti
     * @param nID_ProcessChat
     * @return
     */
    public List<ProcessChatMessageTree> getProcessChatMessageRelationsFilter(Long nID_Process_Activiti, Long nID_ProcessChat){
        List<ProcessChatMessageTree> aProcessChatMessageTreeBynIDProcessChat;
        aProcessChatMessageTreeBynIDProcessChat = getProcessChatMessageRelations(nID_Process_Activiti).stream()
                .filter(t -> t.getProcessChatMessageParent().getoProcessChat().getId().equals(nID_ProcessChat)).collect(Collectors.toList());
        LOG.debug("aProcessChatMessageTreeBynIDProcessChat size= " + aProcessChatMessageTreeBynIDProcessChat.size());
        return aProcessChatMessageTreeBynIDProcessChat;   
    }

}
