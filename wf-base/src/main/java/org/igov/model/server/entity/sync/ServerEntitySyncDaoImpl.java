package org.igov.model.server.entity.sync;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.igov.model.core.GenericEntityDao;

/**
 *
 * @author Kovylin
 */
@Repository
public class ServerEntitySyncDaoImpl extends GenericEntityDao<Long, ServerEntitySync> implements ServerEntitySyncDao{
    
    public ServerEntitySyncDaoImpl() {
        super(ServerEntitySync.class);
    }
    
    @Override
    public List<ServerEntitySync> getaServerEntitySync_ByEntityAndRow(String sID_EntityRow, String sID_Entity){
        Criteria criteria = createCriteria();
        
        if(sID_EntityRow != null){
             criteria.add(Restrictions.eq("sID_EntityRow", sID_EntityRow));
        }
        if(sID_Entity != null){
             criteria.add(Restrictions.eq("sID_Entity", sID_Entity));
        }
        
        criteria.add(Restrictions.lt("nTry", 3L));
        
        criteria.add(Restrictions.lt("oServerEntitySyncStatus.id", 3L));
        
        return criteria.list();
    }
    
    @Override
    public List<ServerEntitySync> getaServerEntitySync_ByAction(String sID_EntityRow, String sID_Entity, ServerEntitySyncStatus oServerEntitySyncStatus, Long nTry){
        Criteria criteria = createCriteria();
        
        /* if(sID_EntityRow != null){
             criteria.add(Restrictions.eq("sID_EntityRow", sID_EntityRow));
        }
        if(sID_Entity != null){
             criteria.add(Restrictions.eq("sID_Entity", sID_Entity));
        }*/
        
        if(oServerEntitySyncStatus != null){
             criteria.add(Restrictions.eq("oServerEntitySyncStatus.id", oServerEntitySyncStatus.getId()));
        }
        
        if(nTry != null){
             criteria.add(Restrictions.lt("nTry", nTry));
        }
        
        return criteria.list();
    }
}
