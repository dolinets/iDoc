package org.igov.model.subject.organ;

import java.util.ArrayList;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;
import org.igov.model.subject.SubjectHuman;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.igov.model.core.GenericEntityDao;

import java.util.List;
import org.igov.model.subject.Subject;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Subqueries;
import org.igov.model.subject.SubjectGroup;

@Repository
public class SubjectOrganDaoIml extends GenericEntityDao<Long, SubjectOrgan> implements SubjectOrganDao {

    @Autowired
    @Qualifier("subjectOrganJoinDao")
    private GenericEntityDao<Long, SubjectOrganJoin> subjectOrganJoinDao;

    protected SubjectOrganDaoIml() {
        super(SubjectOrgan.class);
    }

    @Override
    public SubjectOrgan getSubjectOrgan(String sOKPO) {
        return findBy("sOKPO", sOKPO).orNull();
    }

    @Override
    public SubjectOrgan setSubjectOrgan(String sOKPO) {
        SubjectOrgan oSubjectOrgan = new SubjectOrgan();
        oSubjectOrgan.setsOKPO(sOKPO);
        Subject oSubject = new Subject();
        oSubjectOrgan.setoSubject(oSubject);
        return saveOrUpdateSubjectOrgan(oSubjectOrgan);
    }

    @Override
    public SubjectOrgan saveOrUpdateSubjectOrgan(SubjectOrgan oSubjectOrgan) {
        oSubjectOrgan.getoSubject().setsID(oSubjectOrgan.getsOKPO());
        getSession().saveOrUpdate(oSubjectOrgan);
        return oSubjectOrgan;
    }

    @Override
    public SubjectOrgan getSubjectOrgan(Long nID) {
        return findById(nID).orNull();
    }

    @Override
    public SubjectOrgan getSubjectOrgan(Subject subject) {
        Long subjectId = subject.getId();
        Criteria criteria = createCriteria();
        criteria.createCriteria("oSubject").add(Restrictions.eq("id", subjectId));
        return (SubjectOrgan) criteria.uniqueResult();
    }

    @SuppressWarnings("unchecked" /* православно тут все... */)
    @Override
    public List<SubjectOrganJoin> findSubjectOrganJoinsBy(Long organID, Long regionID, Long cityID, String sID_Place_UA) {
        Criteria crt = getSession()
                .createCriteria(SubjectOrganJoin.class)
                .add(Restrictions.eq("subjectOrganId", organID));

        if (regionID != null && regionID > 0)
            crt.add(Restrictions.eq("regionId", regionID));

        if (cityID != null && cityID > 0)
            crt.add(Restrictions.eq("cityId", cityID));

        if (isNotBlank(sID_Place_UA)){
            crt.add(Restrictions.or(Restrictions.isNull("uaId"), Restrictions.eqOrIsNull("uaId", sID_Place_UA)));
        }

        return crt.list();
    }
    
    @Override
    public List<SubjectOrgan> getSubjectOrgansBysChain(String sChain) {

        Criteria criteria = getSession().createCriteria(SubjectOrgan.class, "subjectOrgan");

        DetachedCriteria subquery = DetachedCriteria.forClass(SubjectGroup.class, "subjectGroup")
                .add(Restrictions.eq("subjectGroup.sChain", sChain))
                .add(Restrictions.eqProperty("subjectGroup.oSubject.id", "subjectOrgan.oSubject.id"));

        criteria.add(Subqueries.exists(subquery.setProjection(Projections.property("subjectGroup.oSubject.id"))));

        //SubjectHuman oSubjectHuman = (SubjectHuman) criteria.uniqueResult();

        return criteria.list();
    }
    
    //@SuppressWarnings("unchecked" /* православно тут все... */)
    /*@Override
    public SubjectOrganJoin findSubjectOrganJoin(Long nID) {
        Criteria oCriteria = getSession()
                .createCriteria(SubjectOrganJoin.class)
                .add(Restrictions.eq("nID", nID));

        List<SubjectOrganJoin> a = oCriteria.list();
        return a.isEmpty() ? null : a.get(0);
    }*/

    
    @Transactional // TODO Might be bottleneck, should be replaced via stored procedure.
    public void add(SubjectOrganJoin soj) {

        SubjectOrganJoin persisted = get(soj.getPublicId(), soj.getSubjectOrganId());

        if (persisted == null) {
            // Object doesn't exists, we have to create it
            soj.setId(null);
            subjectOrganJoinDao.saveOrUpdate(soj);
        } else {
            // Object available, hence, we have to update its main parameters
            persisted.setUaId(soj.getUaId());
            persisted.setNameUa(soj.getNameUa());
            persisted.setNameRu(soj.getNameRu());
            persisted.setGeoLongitude(soj.getGeoLongitude());
            persisted.setGeoLatitude(soj.getGeoLatitude());

            if (soj.getRegionId() != null)
                persisted.setRegionId(soj.getRegionId());

            if (soj.getCityId() != null)
                persisted.setCityId(soj.getCityId());

            subjectOrganJoinDao.saveOrUpdate(persisted);
        }
    }

    private SubjectOrganJoin get(String publicId, Long subjectOrganId) {
        return (SubjectOrganJoin) getSession()
                .createCriteria(SubjectOrganJoin.class)
                .add(Restrictions.eq("publicId", publicId))
                .add(Restrictions.eq("subjectOrganId", subjectOrganId))
                .uniqueResult();
    }

    @SuppressWarnings("unchecked") // i все буде добре...
    @Transactional // TODO Might be bottleneck, should be replaced via bulk delete (stored procedure)
    public void removeSubjectOrganJoin(Long organID, String[] publicIDs) {
        List<SubjectOrganJoin> sojs = (List<SubjectOrganJoin>) getSession()
                .createCriteria(SubjectOrganJoin.class)
                .add(Restrictions.eq("subjectOrganId", organID))
                .add(Restrictions.in("publicId", publicIDs))
                .list();

        for (SubjectOrganJoin soj : sojs)
            subjectOrganJoinDao.delete(soj);
    }
    
    @Override
    public List<SubjectOrgan> getSubjectOrgansByIdRange(int nId, int count) {
        
        if(nId < 0 || count < 0){
            return new ArrayList<>();
        }
        
        Criteria criteria = createCriteria();
        criteria.addOrder(Order.asc("id"))
                 .setFirstResult(nId)
                 .setMaxResults(count);
        return criteria.list();
    }
}