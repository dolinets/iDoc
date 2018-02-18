package org.igov.model.action.event;

import java.util.List;
import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

/**
 *
 * @author alex
 */
@Repository
public class ActionEventTypeDaoImpl extends GenericEntityDao<Long, ActionEventType> implements ActionEventTypeDao {

    protected ActionEventTypeDaoImpl() {
        super(ActionEventType.class);
    }

    @Override
    public List<ActionEventType> findByActionEventTypeGroup(Long nID_ActionEventTypeGroup) {
        return findAllBy("oActionEventTypeGroup.id", nID_ActionEventTypeGroup);
    }

}
