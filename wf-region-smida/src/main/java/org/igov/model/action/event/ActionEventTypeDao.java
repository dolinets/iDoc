package org.igov.model.action.event;

import java.util.List;
import org.igov.model.core.EntityDao;

/**
 * @author alex
 */
public interface ActionEventTypeDao  extends EntityDao<Long, ActionEventType>{
    
    public List<ActionEventType> findByActionEventTypeGroup(Long nID_ActionEventTypeGroup);
    
}
