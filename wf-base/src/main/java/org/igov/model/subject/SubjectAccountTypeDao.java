package org.igov.model.subject;

import org.igov.model.core.EntityDao;

import java.util.List;

public interface SubjectAccountTypeDao extends EntityDao<Long, SubjectAccountType> {

    List<SubjectAccountType> getSubjectAccountTypes(String sID, String sNote);

}
