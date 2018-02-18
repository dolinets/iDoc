package org.igov.model.subject;

import com.google.common.base.Optional;
import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SubjectHumanPositionCustomDaoImpl extends GenericEntityDao<Long, SubjectHumanPositionCustom>
        implements SubjectHumanPositionCustomDao {

    public SubjectHumanPositionCustomDaoImpl() {
        super(SubjectHumanPositionCustom.class);
    }

    @Override
    public Optional<SubjectHumanPositionCustom> getGroupDepartment() {
        return findBy("name", "groupDepartment");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SubjectHumanPositionCustom> findAllByStartLikeName(String sName) {
        Criteria criteria = createCriteria().add(Restrictions.ilike("name", sName, MatchMode.START));
        return criteria.list();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<SubjectHumanPositionCustom> findAllByStartLikeNameAndAroundLikeNote(String sName, String sNote) {
        Criteria criteria = createCriteria().add(
                Restrictions.and(
                    Restrictions.ilike("name", sName, MatchMode.START),
                    Restrictions.ilike("sNote", sNote, MatchMode.ANYWHERE)
            ));
        return criteria.list();
    }
}
