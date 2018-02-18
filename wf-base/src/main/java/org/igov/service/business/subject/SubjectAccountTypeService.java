package org.igov.service.business.subject;

import org.igov.model.subject.SubjectAccountType;
import org.igov.model.subject.SubjectAccountTypeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectAccountTypeService {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectAccountTypeService.class);

    @Autowired
    private SubjectAccountTypeDao subjectAccountTypeDao;

    public List<SubjectAccountType> getSubjectAccountTypes(String sNote) {
        if (sNote == null || sNote.isEmpty()) {
            return subjectAccountTypeDao.findAll();
        }
        return subjectAccountTypeDao.findAllByLikeAttributeCriteria("sNote", sNote);
    }

    public SubjectAccountType setSubjectAccountType(Long nID, String sID, String sNote) {
        boolean bExists = subjectAccountTypeDao.findBy("sNote", sNote).isPresent();
        if (nID == null && bExists) {
            throw new RuntimeException(String.format("sNote = '%s' already exists", sNote));
        }
        SubjectAccountType accountType = new SubjectAccountType();
        accountType.setId(nID);
        accountType.setsID(sID);
        accountType.setsNote(sNote);
        return subjectAccountTypeDao.saveOrUpdate(accountType);
    }

    public void deleteSubjectAccountType(Long id) {
        subjectAccountTypeDao.delete(id);
    }

}