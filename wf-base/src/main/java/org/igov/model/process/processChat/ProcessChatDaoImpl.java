package org.igov.model.process.processChat;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.igov.model.core.GenericEntityDao;
import org.igov.model.process.ProcessSubjectDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProcessChatDaoImpl extends GenericEntityDao<Long, ProcessChat> implements ProcessChatDao {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessSubjectDaoImpl.class);

    public ProcessChatDaoImpl() {
        super(ProcessChat.class);
    }

    @Transactional
    @Override
    public ProcessChat setProcessChat(ProcessChat oProcessChat) {
        return saveOrUpdate(oProcessChat);
    }

    @Override
    public List<ProcessChat> findByProcessActivitiId(Long nID_Process_Activiti) {
        LOG.info("findByProcessActivitiId: nID_Process_Activiti={} ", nID_Process_Activiti);
        return findAllBy("nID_Process_Activiti", nID_Process_Activiti);
    }

    @Override
    public ProcessChat findByProcessActivitiIdAndsKeyGroup(Long nID_Process_Activiti, String sKeyGroup) {
        ProcessChat oProcessChat = null;

        if (nID_Process_Activiti != null && sKeyGroup != null) {

            Criteria criteria = getSession().createCriteria(ProcessChat.class);

            criteria.add(Restrictions.eq("nID_Process_Activiti", nID_Process_Activiti));
            criteria.add(Restrictions.eq("sKeyGroup", sKeyGroup));

            oProcessChat = (ProcessChat) criteria.uniqueResult();
            LOG.info("findByProcessActivitiIdAndsKeyGroup: oProcessChat={} ", oProcessChat);

        } else {

            LOG.warn("findByProcessActivitiIdAndLogin: пустые аргументы nID_Process_Activiti={} и sKeyGroup={}", nID_Process_Activiti, sKeyGroup);

        }

        return oProcessChat;
    }
}
