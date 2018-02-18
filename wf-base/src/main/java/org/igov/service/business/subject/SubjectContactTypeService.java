package org.igov.service.business.subject;

import org.igov.model.subject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SubjectContactTypeService {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectContactTypeService.class);

    @Autowired
    private SubjectContactTypeDao subjectContactTypeDao;

    public List<SubjectContactType> getSubjectContactTypes() {
        return subjectContactTypeDao.findAll();
    }

    /**
     * create, update(by nID)
     *
     * @param sName_EN supposed to be unique
     */
    public SubjectContactType setSubjectContactType(Long nID, String sName_EN, String sName_RU, String sName_UA) {
        boolean bExists = subjectContactTypeDao.findBy("sName_EN", sName_EN).isPresent();
        if (nID == null && bExists) {
            throw new RuntimeException("SubjectContactType with sName_EN = '" + sName_EN + "' already exists");
        }
        SubjectContactType oSubjectContactType = new SubjectContactType();
        oSubjectContactType.setId(nID);
        oSubjectContactType.setsName_EN(sName_EN);
        oSubjectContactType.setsName_RU(sName_RU);
        oSubjectContactType.setsName_UA(sName_UA);
        return subjectContactTypeDao.saveOrUpdate(oSubjectContactType);
    }

    public void deleteSubjectContactType(Long id) {
        subjectContactTypeDao.delete(id);
    }

}
