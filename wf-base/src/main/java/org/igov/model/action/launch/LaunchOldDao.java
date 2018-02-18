package org.igov.model.action.launch;

import java.util.List;
import org.igov.model.core.EntityDao;

/**
 *
 * @author idenysenko
 */
public interface LaunchOldDao extends EntityDao<Long, LaunchOld> {
    
    public List<LaunchOld> findWithDateDiff(Integer nDayCount);
}
