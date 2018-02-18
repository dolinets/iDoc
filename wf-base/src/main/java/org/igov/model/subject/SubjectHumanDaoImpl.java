package org.igov.model.subject;

import org.hibernate.Criteria;
import org.hibernate.criterion.*;
import org.igov.model.core.GenericEntityDao;
import org.igov.model.server.Server;
import org.igov.model.server.ServerDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Repository
public class SubjectHumanDaoImpl extends GenericEntityDao<Long, SubjectHuman> implements SubjectHumanDao {
    
    private static final Logger LOG = LoggerFactory.getLogger(SubjectHumanDaoImpl.class);

    @Autowired private SubjectDao subjectDao;
    @Autowired private SubjectContactTypeDao contactTypeDao;
    @Autowired private ServerDao oServerDao;

    public SubjectHumanDaoImpl() {
        super(SubjectHuman.class);
    }

    @Override
    public SubjectHuman getSubjectHuman(String sINN) {
        return findBy("sINN", sINN).orNull();
    }


    public SubjectHuman getSubjectHuman(Subject subject) {
        Long subjectId = subject.getId();
        Criteria criteria = createCriteria();
        criteria.createCriteria("oSubject").add(Restrictions.eq("id", subjectId));
        return (SubjectHuman) criteria.uniqueResult();
    }

    public SubjectHuman getSubjectHuman(SubjectHumanIdType subjectHumanIdType, String sCode_Subject) {
        String subjectId = SubjectHuman.getSubjectId(subjectHumanIdType, sCode_Subject);
        Criteria criteria = createCriteria();
        criteria.createCriteria("oSubject").add(Restrictions.eq("sID", subjectId));
        return (SubjectHuman) criteria.uniqueResult();
    }

    @Override
    public SubjectHuman saveSubjectHuman(SubjectHumanIdType subjectHumanIdType, String sCode_Subject) {
        SubjectHuman oSubjectHuman = new SubjectHuman();
        oSubjectHuman.setSubjectHumanIdType(subjectHumanIdType);

        Subject subject = new Subject();
        String subjectId = SubjectHuman.getSubjectId(subjectHumanIdType, sCode_Subject);
        subject.setsID(subjectId);

        if (SubjectHumanIdType.INN == subjectHumanIdType) {
            oSubjectHuman.setsINN(sCode_Subject);
        } else if (Arrays.asList(SubjectHumanIdType.Phone, SubjectHumanIdType.Email).contains(subjectHumanIdType)) {
            SubjectContact subjectContact = new SubjectContact();
            subjectContact.setSubject(subject);
            subjectContact.setsValue(sCode_Subject);

            final boolean isPhone = SubjectHumanIdType.Phone == subjectHumanIdType;
            if (isPhone) {
                subjectContact.setSubjectContactType(contactTypeDao.getPhoneType());
                oSubjectHuman.setDefaultPhone(subjectContact);
            } else {
                subjectContact.setSubjectContactType(contactTypeDao.getEmailType());
                oSubjectHuman.setDefaultEmail(subjectContact);
            }

            subjectDao.saveOrUpdate(subject);
        }
        //else if (subjectHumanIdType.equals(SubjectHumanIdType.Passport)) {
        // TODO logic of setting fields  sPassportSeria, sPassportNumber
        //}

        oSubjectHuman.setoSubject(subject);

        saveOrUpdate(oSubjectHuman);
        return oSubjectHuman;
    }

    @Override
    public SubjectHuman saveSubjectHuman(String sINN) {
        return saveSubjectHuman(SubjectHumanIdType.INN, sINN);
    }

    @Override
    public SubjectHuman saveOrUpdateHuman(SubjectHuman oSubjectHuman) {
        String subjectId = SubjectHuman.getSubjectId(oSubjectHuman.getSubjectHumanIdType(),
                oSubjectHuman.getsINN());
        oSubjectHuman.getoSubject().setsID(subjectId);
        saveOrUpdate(oSubjectHuman);
        return oSubjectHuman;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SubjectHuman> getExternalSubjectHumanByIdGroupActiviti(Set<String> asID_Group_Activiti, String sURL) {
        LOG.info("getExternalSubjectHumanByIdGroupActiviti sURL={}", sURL);
        List<SubjectHuman> aoSubjectHuman = new ArrayList<>();
        if (!asID_Group_Activiti.isEmpty()) {
            //проверка для серверов у которых нет коллективной работы
            Server oServer = oServerDao.findBy("sURL", sURL).orNull();
            if (oServer != null) {
                aoSubjectHuman.addAll(
                        getSession().createQuery(
                                "select subjectHuman from SubjectGroup subjectGroup, SubjectHuman subjectHuman\n" +
                                        " where subjectGroup.oSubject.id = subjectHuman.oSubject.id\n" +
                                        " and subjectHuman.oServer is not null \n" +
                                        " and subjectHuman.oServer.id != :ServerId \n" +
                                        " and subjectGroup.sID_Group_Activiti in (:asLogin) \n")
                                .setParameterList("asLogin", asID_Group_Activiti)
                                .setParameter("ServerId", oServer.getId())
                                .list()
                );
            } else {
                LOG.info("Can't find server for sURL={}", sURL);
            }
        }

        return aoSubjectHuman;
    }

    @Override
    public List<SubjectHuman> getSubjectHumansByIdServer(Long nID_Server) {
        LOG.info("getSubjectHumanByIdServer nID_Server={}", nID_Server);
        List<SubjectHuman> aoSubjectHuman = new ArrayList<>();
        if (nID_Server!=null) {
            List<SubjectHuman> aoSubjectHuman_New = getSession().createQuery(
                    "select subjectHuman from SubjectHuman subjectHuman"
                            + " where subjectHuman.oServer.id = :nID_Server"
            )
                    .setParameter("nID_Server", nID_Server)
                    .list();
            if(aoSubjectHuman_New!=null && !aoSubjectHuman_New.isEmpty()){
                aoSubjectHuman.addAll(
                        aoSubjectHuman_New
                );
            }
        }
        return aoSubjectHuman;
    }
    
    
    @Override
    public List<SubjectHuman> getSubjectHumansBysChain(String sChain) {

        Criteria criteria = getSession().createCriteria(SubjectHuman.class, "subjectHuman");

        DetachedCriteria subquery = DetachedCriteria.forClass(SubjectGroup.class, "subjectGroup")
                .add(Restrictions.eq("subjectGroup.sChain", sChain))
                .add(Restrictions.eqProperty("subjectGroup.oSubject.id", "subjectHuman.oSubject.id"));

        criteria.add(Subqueries.exists(subquery.setProjection(Projections.property("subjectGroup.oSubject.id"))));

        //SubjectHuman oSubjectHuman = (SubjectHuman) criteria.uniqueResult();

        return criteria.list();
    }
    
    @Override
    public List<SubjectHuman> getSubjectHumansByIdRange(int nId, int count) {
        
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
