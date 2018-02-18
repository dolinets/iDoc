package org.igov.model.subject;

import java.util.List;
import java.util.Set;
import org.igov.model.core.EntityDao;

public interface SubjectHumanDao extends EntityDao<Long, SubjectHuman> {
    
    SubjectHuman getSubjectHuman(String sINN);

    SubjectHuman getSubjectHuman(Subject subject);

    SubjectHuman saveSubjectHuman(String sINN);

    SubjectHuman getSubjectHuman(SubjectHumanIdType subjectHumanIdType, String sCode_Subject);

    SubjectHuman saveSubjectHuman(SubjectHumanIdType subjectHumanIdType, String sCode_Subject);

    SubjectHuman saveOrUpdateHuman(SubjectHuman subject);
    
    List<SubjectHuman> getExternalSubjectHumanByIdGroupActiviti(Set<String> asID_Group_Activiti, String sURL);
    
    List<SubjectHuman> getSubjectHumansBysChain(String sChain);
    
    List<SubjectHuman> getSubjectHumansByIdServer(Long nID_Server);
    
    public List<SubjectHuman> getSubjectHumansByIdRange(int nId, int count);

}
