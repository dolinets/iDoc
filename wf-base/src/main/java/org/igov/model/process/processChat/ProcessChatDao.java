package org.igov.model.process.processChat;

import java.util.List;
import org.igov.model.core.EntityDao;

public interface ProcessChatDao extends EntityDao<Long, ProcessChat>{
    
    ProcessChat setProcessChat(ProcessChat oProcessChat);
    
    List<ProcessChat> findByProcessActivitiId(Long nID_Process_Activiti);
    
    ProcessChat findByProcessActivitiIdAndsKeyGroup(Long nID_Process_Activiti, String sKeyGroup);
    
}
