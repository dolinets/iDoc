package org.igov.service.business.subject.organ;

import org.igov.model.subject.organ.SubjectOrgan;
import org.igov.model.subject.organ.SubjectOrganDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author alex
 */
@Component("subjectOrganService")
public class SubjectOrganService {
    
     private static final Logger LOG = LoggerFactory.getLogger(org.igov.service.business.subject.SubjectOrganService.class);
    
    @Autowired
    private SubjectOrganDao subjectOrganDao;
    
     public SubjectOrgan getSubjectOrgan(String sOKPO) {
        LOG.info(String.format("find SubjectOrgan entity by sOKPO=%s", sOKPO));
        return subjectOrganDao.getSubjectOrgan(sOKPO);
    }
    
}
