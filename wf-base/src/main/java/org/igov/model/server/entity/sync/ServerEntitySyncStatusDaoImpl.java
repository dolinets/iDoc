package org.igov.model.server.entity.sync;

import org.springframework.stereotype.Repository;
import org.igov.model.core.GenericEntityDao;

/**
 *
 * @author Kovylin
 */
@Repository
public class ServerEntitySyncStatusDaoImpl extends GenericEntityDao<Long, ServerEntitySyncStatus> implements ServerEntitySyncStatusDao{

    public ServerEntitySyncStatusDaoImpl() {
        super(ServerEntitySyncStatus.class);
    }
    
    
}
