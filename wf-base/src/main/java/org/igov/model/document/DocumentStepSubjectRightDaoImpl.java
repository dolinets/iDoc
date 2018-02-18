package org.igov.model.document;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.igov.model.core.GenericEntityDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class DocumentStepSubjectRightDaoImpl extends GenericEntityDao<Long, DocumentStepSubjectRight> implements DocumentStepSubjectRightDao {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentStepSubjectRightDaoImpl.class);

    public DocumentStepSubjectRightDaoImpl() {
        super(DocumentStepSubjectRight.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<String> findDocumentParticipant(String snID_Process) {
        List<String> asID_Group_Activiti = getSession().createQuery(
                "select distinct rights.sKey_GroupPostfix from DocumentStepSubjectRight rights,"
                        + " DocumentStep step "
                        + " where rights.documentStep.id = step.id"
                        + " and step.snID_Process_Activiti = :snID_Process "
                        + " and rights.sKey_GroupPostfix not like '_default%'")
                .setParameter("snID_Process", snID_Process)
                .list();

        return new HashSet<>(asID_Group_Activiti);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DocumentStepSubjectRight> findUnsignedRights(String snID_Process, String sKey_Step, boolean bNeedECPCheck) {
        LOG.info("findUnsignedRights start with {} , {}, {}", snID_Process, sKey_Step, bNeedECPCheck);
        Criteria criteria = getSession().createCriteria(DocumentStepSubjectRight.class, "rights");
        criteria.createAlias("rights.documentStep", "step");
        criteria.add(Restrictions.eq("step.snID_Process_Activiti", snID_Process));
        criteria.add(Restrictions.ne("step.sKey_Step", "_"));
        if (sKey_Step != null) {
            criteria.add(Restrictions.eq("step.sKey_Step", sKey_Step));
        }
        criteria.add(Restrictions.eq("rights.bWrite", true));
        if (bNeedECPCheck) {
            criteria.add(Restrictions.eq("rights.bNeedECP", true));
            criteria.add(Restrictions.isNull("rights.sDateECP"));
        } else {
            criteria.add(Restrictions.isNull("rights.sDate"));
        }

        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DocumentStepSubjectRight> getRightsByProcessAndGroup(String snID_process_activiti, String sKey_groupPostfix) {
        LOG.info("getRightsByProcessAndGroup start with {} , {}", snID_process_activiti, sKey_groupPostfix);
        Criteria criteria = getSession().createCriteria(DocumentStepSubjectRight.class, "rights");
        criteria.createAlias("rights.documentStep", "step");
        criteria.add(Restrictions.eq("step.snID_Process_Activiti", snID_process_activiti));
        criteria.add(Restrictions.eq("rights.sKey_GroupPostfix", sKey_groupPostfix));

        return criteria.list();
    }
}
