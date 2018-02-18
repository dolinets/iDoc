package org.igov.model.process.processChat;

import java.util.List;
import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

@Repository
public class ProcessChatMessageTreeDaoImpl extends GenericEntityDao<Long, ProcessChatMessageTree> implements ProcessChatMessageTreeDao {

    public ProcessChatMessageTreeDaoImpl() {
        super(ProcessChatMessageTree.class);
    }

    @Override
    public List<ProcessChatMessageTree> findChildrenProcess_Activiti(Long nID_Process_Activiti) {
        return findAllBy("processChatMessageParent.oProcessChat.nID_Process_Activiti", nID_Process_Activiti);
    }

}
