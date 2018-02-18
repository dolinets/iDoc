package org.igov.model.process.processLink;

import org.hibernate.criterion.Restrictions;
import org.igov.model.core.GenericEntityDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author idenysenko
 */
@Repository
public class ProcessLinkDaoImpl extends GenericEntityDao<Long, ProcessLink> implements ProcessLinkDao {
    
    private static final Logger LOG = LoggerFactory.getLogger(ProcessLinkDaoImpl.class);

    protected ProcessLinkDaoImpl() {
        super(ProcessLink.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ProcessLink> getProcessLinks(String sID_Group_Activiti, String sType, String sSubType) {
        LOG.info("getProcessLinks:  sID_Group_Activiti={}, sType={}, sSubType={}", sID_Group_Activiti, sType, sSubType);

        List<ProcessLink> aoProcessLink = getSession().createCriteria(ProcessLink.class, "processLink")
                .add(Restrictions.eq("processLink.sID_Group_Activiti", sID_Group_Activiti))
                .createAlias("processLink.oProcessLinkType", "processLinkType")
                .add(Restrictions.eq("processLinkType.sName", sType))
                .createAlias("processLink.oProcessLinkSubType", "processLinkSubType")
                .add(Restrictions.eq("processLinkSubType.sName", sSubType))
                .list();
        LOG.info("aProcessLink.size={}", aoProcessLink.size());

        return aoProcessLink;
    }

    @Override
    public ProcessLink findProcessLink(String snID_Process_Activiti, String sID_Group_Activiti, Long nID_Server) {
       LOG.info("findByUniqueKey: snID_Process_Activiti={}, sID_Group_Activiti={}, nID_Server={}",
               snID_Process_Activiti, sID_Group_Activiti, nID_Server);
       ProcessLink oProcessLink = (ProcessLink) getSession().createCriteria(ProcessLink.class, "processLink")
               .add(Restrictions.eq("processLink.snID_Process_Activiti", snID_Process_Activiti))
               .add(Restrictions.eq("processLink.sID_Group_Activiti", sID_Group_Activiti))
               .createAlias("processLink.oServer", "server")
               .add(Restrictions.eq("server.id", nID_Server))
               .uniqueResult();
        LOG.info("oProcessLink={}", oProcessLink);

        return oProcessLink;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ProcessLink> getDeletedProcessLinks(String sID_Group_Activiti) {

        return (List<ProcessLink>) getSession().createCriteria(ProcessLink.class)
                .add(Restrictions.eq("sID_Group_Activiti", sID_Group_Activiti))
                .add(Restrictions.eq("sStatus", "deleted"))
                .list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ProcessLink> getProcessLinksByProcessAndGroup(String snID_Process_Activiti, String sID_Group_Activiti) {

        return (List<ProcessLink>) getSession().createCriteria(ProcessLink.class)
                .add(Restrictions.eq("snID_Process_Activiti", snID_Process_Activiti))
                .add(Restrictions.eq("sID_Group_Activiti", sID_Group_Activiti))
                .list();
    }
    
}
