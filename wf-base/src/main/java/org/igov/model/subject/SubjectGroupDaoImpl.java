package org.igov.model.subject;

import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author idenysenko
 */
@Repository
public class SubjectGroupDaoImpl extends GenericEntityDao<Long, SubjectGroup> implements SubjectGroupDao {

    public SubjectGroupDaoImpl() {
        super(SubjectGroup.class);
    }

    
    @Override
    @SuppressWarnings("unchecked")
    public SubjectGroup findSubjectById(Long nID_Subject) {
        List<SubjectGroup> aSubjectGroup = getSession().createQuery("SELECT sg "
                + "FROM SubjectGroup sg "
                + "WHERE sg.oSubject.id = :nID_Subject")
                .setParameter("nID_Subject", nID_Subject)
                .list();
        
        return aSubjectGroup!=null&&!aSubjectGroup.isEmpty()?aSubjectGroup.get(0):null;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<SubjectGroup> findHumansInCompany(String sChain) {
        return getSession().createQuery("SELECT sg "
                + "FROM SubjectGroup sg, SubjectHuman sh "
                + "WHERE sg.sChain = :sChain AND sg.oSubject.id = sh.oSubject.id")
                .setParameter("sChain", sChain)
                .list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SubjectGroup> findDepartsInCompany(String sChain) {
        return getSession().createQuery("SELECT sg "
                + "FROM SubjectGroup sg, SubjectOrgan so "
                + "WHERE sg.sChain = :sChain AND sg.oSubject.id = so.oSubject.id")
                .setParameter("sChain", sChain)
                .list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SubjectGroup> findHumansInCompanyByLikeNameOrLogin(String sChain, String sName) {
        return getSession().createQuery("SELECT sg "
                + "FROM SubjectGroup sg, SubjectHuman sh "
                + "WHERE sg.sChain = :sChain AND ((lower(sg.name) LIKE :sName OR lower(sg.sID_Group_Activiti) LIKE :sName)) AND sg.oSubject.id = sh.oSubject.id")
                .setParameter("sChain", sChain)
                .setParameter("sName", "%" + sName.toLowerCase() + "%")
                .list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SubjectGroup> findDepartsInCompanyByLikeName(String sChain, String sName) {
        return getSession().createQuery("SELECT sg "
                + "FROM SubjectGroup sg, SubjectOrgan so "
                + "WHERE sg.sChain = :sChain AND lower(sg.name) LIKE :sName AND sg.oSubject.id = so.oSubject.id")
                .setParameter("sChain", sChain)
                .setParameter("sName", "%" + sName.toLowerCase() + "%")
                .list();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<SubjectGroup> findAllByLikeNameOrLogin(String sNameOrLogin) {
        Criteria criteria = createCriteria();
        criteria.add(Restrictions.or(
            Restrictions.ilike("sID_Group_Activiti", sNameOrLogin, MatchMode.ANYWHERE),
            Restrictions.ilike("name", sNameOrLogin, MatchMode.ANYWHERE)
        ));
        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SubjectGroup> findAllByLikeLogin(String sLogin) {
        Criteria criteria = createCriteria();
        criteria.add(
            Restrictions.ilike("sID_Group_Activiti", sLogin, MatchMode.EXACT)
        );
        return criteria.list();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> findLoginDubles() {
        return getSession().createQuery(
                "SELECT sID_Group_Activiti "//,count(sID_Group_Activiti)
                + "FROM SubjectGroup "
                + "group by sID_Group_Activiti "
                + "having count(sID_Group_Activiti) > 1 "
                )
                .list();
    }
    





    
}
