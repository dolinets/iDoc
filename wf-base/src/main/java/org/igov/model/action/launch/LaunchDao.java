package org.igov.model.action.launch;

import org.igov.model.core.EntityDao;

import java.util.List;

/**
 *
 * @author idenysenko
 */
public interface LaunchDao extends EntityDao<Long, Launch> {

    List<Launch> findByFilter(Integer nTry, Long nID_server, String sDateFrom, String sDateTo);
}
