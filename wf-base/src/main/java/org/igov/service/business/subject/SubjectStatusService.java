package org.igov.service.business.subject;

import org.igov.model.subject.SubjectStatus;
import org.igov.model.subject.SubjectStatusDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SubjectStatusService {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectStatusService.class);

    @Autowired private SubjectStatusDao subjectStatusDao;

    public List<SubjectStatus> getSubjectStatus(String sName_SubjectType) {
        List<SubjectStatus> aoSubjectStatus = new ArrayList<>();
        if (sName_SubjectType == null) {
            aoSubjectStatus.addAll(subjectStatusDao.findAll());
        } else {
            if (sName_SubjectType.equals("Human")) {
                aoSubjectStatus.addAll(subjectStatusDao.findAllBy("oSubjectType.id", 1L));
            }
            if (sName_SubjectType.equals("Organ")) {
                aoSubjectStatus.addAll(subjectStatusDao.findAllBy("oSubjectType.id", 2L));
            }
        }
        LOG.info("Founded {} status.", aoSubjectStatus.size());

        return aoSubjectStatus;

    }
}
