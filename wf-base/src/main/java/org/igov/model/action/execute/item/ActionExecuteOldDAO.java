package org.igov.model.action.execute.item;

import org.igov.model.core.EntityDao;

import java.util.List;

public interface ActionExecuteOldDAO extends EntityDao<Long, ActionExecuteOld>, Sessionable {
	
	ActionExecuteOld getActionExecute(Long id);
	List<ActionExecuteOld> getAllActionExecutes();

}
