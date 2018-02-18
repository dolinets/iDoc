package org.igov.model.subject;

import java.util.List;
import org.igov.model.core.EntityDao;

/**
 *
 * @author idenysenko
 */
public interface SubjectGroupTreeDao extends EntityDao<Long, SubjectGroupTree> {
    
    public List<SubjectGroupTree> getaSubjectGroupTreeByCompany(String sCompany_Name);

    List<SubjectGroupTree> getaSubjectGroupTreeChildren(SubjectGroup subjectGroup);

    List<SubjectGroupTree> getaSubjectGroupTreeParent(SubjectGroup subjectGroup);

    boolean exists(SubjectGroup subjectGroupChild, SubjectGroup subjectGroupParent);

    SubjectGroupTree saveOrUpdate(SubjectGroup subjectGroupChild, SubjectGroup subjectGroupParent);

    List<SubjectGroupTree> deleteChildrenOf(SubjectGroup oSubjectGroup, SubjectGroup... exclude);

    List<SubjectGroupTree> deleteParentsOf(SubjectGroup oSubjectGroup, SubjectGroup... exclude);

}
