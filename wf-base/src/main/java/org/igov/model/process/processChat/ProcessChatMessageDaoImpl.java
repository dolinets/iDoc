package org.igov.model.process.processChat;

import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

@Repository
public class ProcessChatMessageDaoImpl extends GenericEntityDao<Long, ProcessChatMessage> implements ProcessChatMessageDao {

    public ProcessChatMessageDaoImpl() {
        super(ProcessChatMessage.class);
    }

    @Override
    public ProcessChatMessage setProcessChatMessage(ProcessChatMessage processChatMessage) {
        return saveOrUpdate(processChatMessage);
    }

    @Override
    public void removeProcessChatMessage(Long nID_ProcessChatMessage) {
        delete(nID_ProcessChatMessage);
    }
}
