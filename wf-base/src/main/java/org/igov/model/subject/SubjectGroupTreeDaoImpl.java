package org.igov.model.subject;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

/**
 *
 * @author idenysenko
 */
@Repository
public class SubjectGroupTreeDaoImpl extends GenericEntityDao<Long, SubjectGroupTree> implements SubjectGroupTreeDao{
    
    public SubjectGroupTreeDaoImpl() {
        super(SubjectGroupTree.class);
    }
    
    @Override
    public List<SubjectGroupTree> getaSubjectGroupTreeByCompany(String sCompany_Name)
    {
        Criteria criteria = createCriteria();
        if(sCompany_Name != null){
            
            criteria.createAlias("oSubjectGroup_Parent", "oSubjectGroup_Parent")
                    .add(Restrictions.eq("oSubjectGroup_Parent.sChain", sCompany_Name));
        }
        
        return criteria.list();
    }

    @Override
    public List<SubjectGroupTree> getaSubjectGroupTreeChildren(SubjectGroup subjectGroup) {
        return findAllBy("oSubjectGroup_Parent", subjectGroup);
    }

    @Override
    public List<SubjectGroupTree> getaSubjectGroupTreeParent(SubjectGroup subjectGroup) {
        return findAllBy("oSubjectGroup_Child", subjectGroup);
    }

    @Override
    public boolean exists(SubjectGroup subjectGroupChild, SubjectGroup subjectGroupParent) {
        Object oResult = createCriteria()
                .setReadOnly(true)
                .add(Restrictions.and(
                        Restrictions.eq("oSubjectGroup_Child", subjectGroupChild),
                        Restrictions.eq("oSubjectGroup_Parent", subjectGroupParent)
                ))
                .uniqueResult();
        return oResult != null;
    }

    @Override
    public SubjectGroupTree saveOrUpdate(SubjectGroup subjectGroupChild, SubjectGroup subjectGroupParent) {
        SubjectGroupTree oSubjectGroupTree = new SubjectGroupTree();
        oSubjectGroupTree.setoSubjectGroup_Child(subjectGroupChild);
        oSubjectGroupTree.setoSubjectGroup_Parent(subjectGroupParent);
        return saveOrUpdate(oSubjectGroupTree);
    }

    @Override
    public List<SubjectGroupTree> deleteChildrenOf(SubjectGroup oSubjectGroup, SubjectGroup... exclude) {
        Set<Long> anID = Stream.of(exclude).map(SubjectGroup::getId).collect(Collectors.toSet());
        return delete(findAllBy("oSubjectGroup_Parent", oSubjectGroup)
                .stream()
                .filter(sgt -> !anID.contains(sgt.getoSubjectGroup_Child().getId()))
                .collect(Collectors.toList()));
    }

    @Override
    public List<SubjectGroupTree> deleteParentsOf(SubjectGroup oSubjectGroup, SubjectGroup... exclude) {
        Set<Long> anID = Stream.of(exclude).map(SubjectGroup::getId).collect(Collectors.toSet());
        return delete(findAllBy("oSubjectGroup_Child", oSubjectGroup)
                .stream()
                .filter(sgt -> !anID.contains(sgt.getoSubjectGroup_Parent().getId()))
                .collect(Collectors.toList()));
    }
}
