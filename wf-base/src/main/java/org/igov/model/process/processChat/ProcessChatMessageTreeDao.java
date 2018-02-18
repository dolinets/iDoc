package org.igov.model.process.processChat;

import java.util.List;
import org.igov.model.core.EntityDao;

public interface ProcessChatMessageTreeDao extends EntityDao<Long, ProcessChatMessageTree> {
    
    List<ProcessChatMessageTree> findChildrenProcess_Activiti(Long nID_Process_Activiti);

}
