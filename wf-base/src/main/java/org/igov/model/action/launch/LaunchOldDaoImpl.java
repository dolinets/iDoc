package org.igov.model.action.launch;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StandardBasicTypes;
import org.igov.model.core.GenericEntityDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Repository;

/**
 *
 * @author idenysenko
 */
@Repository
public class LaunchOldDaoImpl extends GenericEntityDao<Long, LaunchOld> implements LaunchOldDao{
    
    private static final Logger LOG = LoggerFactory.getLogger(LaunchOldDaoImpl.class);
    
    protected LaunchOldDaoImpl() {
        super(LaunchOld.class);
    }

    @Override
    public List<LaunchOld> findWithDateDiff(Integer nDayCount) {
        LOG.info("findWithDateDiff nDayCount={}", nDayCount);
        Criteria criteria = getSession().createCriteria(Launch.class);
        criteria.add(Restrictions.sqlRestriction("datediff(dd,sDateLock,getDate())> ?", nDayCount, StandardBasicTypes.INTEGER));
        List<LaunchOld> aoLaunch = criteria.list();
        LOG.info("finded LaunchOld {}", aoLaunch.size());
        
        return aoLaunch;
    }
}
