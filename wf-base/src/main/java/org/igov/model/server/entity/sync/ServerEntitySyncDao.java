package org.igov.model.server.entity.sync;

import java.util.List;
import org.igov.model.core.EntityDao;

/**
 *
 * @author Kovylin
 */
public interface ServerEntitySyncDao extends EntityDao<Long, ServerEntitySync>{
    
    public List<ServerEntitySync> getaServerEntitySync_ByEntityAndRow(String sID_EntityRow, String sID_Entity);
    
    public List<ServerEntitySync> getaServerEntitySync_ByAction(String sID_EntityRow, String sID_Entity, ServerEntitySyncStatus oServerEntitySyncStatus, Long nTry);
}
