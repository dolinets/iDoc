package org.igov.model.process.processChat;

import org.igov.model.core.EntityDao;

public interface ProcessChatMessageDao extends EntityDao<Long, ProcessChatMessage> {

    ProcessChatMessage setProcessChatMessage(ProcessChatMessage processChatMessage);
    
    void removeProcessChatMessage(Long nID_ProcessChatMessage);
   
    
}
