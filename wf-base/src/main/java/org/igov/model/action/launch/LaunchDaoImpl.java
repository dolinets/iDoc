package org.igov.model.action.launch;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.igov.model.core.GenericEntityDao;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author idenysenko
 */
@Repository
public class LaunchDaoImpl extends GenericEntityDao<Long, Launch> implements LaunchDao {

    private static final Logger LOG = LoggerFactory.getLogger(LaunchDaoImpl.class);

    protected LaunchDaoImpl() {
        super(Launch.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Launch> findByFilter(Integer nTry, Long nID_server, String sDateFrom, String sDateTo) {
        LOG.info("findByFilter started with params: nTry={}, nID_server={}, sDateFrom={}, sDateFrom={}",
                nTry, nID_server, sDateFrom, sDateTo);
        Criteria criteria = getSession().createCriteria(Launch.class);
        if (nTry != null) {
            criteria.add(Restrictions.le("nTry", nTry));
        }
        if (nID_server != null) {
            criteria.add(Restrictions.eq("oServer.id", nID_server));
        }
        if (sDateFrom != null) {
            criteria.add(Restrictions.ge("sDateEdit", new DateTime(sDateFrom)));
        }
        if (sDateTo != null) {
            criteria.add(Restrictions.le("sDateEdit", new DateTime(sDateTo)));
        }
        //в первую очередь выполняем более старые записи, дабы избежать затирания новых старыми
        criteria.addOrder(Order.asc("sDateEdit"));

        return criteria.list();
    }

}
