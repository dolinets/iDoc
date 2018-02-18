package org.igov.model.subject;

import org.igov.model.core.EntityDao;

import java.util.List;

public interface SubjectRightBPDao extends EntityDao<Long, SubjectRightBP> {
    
    SubjectRightBP getSubjectRightBP(String sID_BP, String sLogin);

    List<SubjectRightBP> findBy(String sID_BP, String sID_Group_Referent, Long nID_SubjectHumanPosition);
}
