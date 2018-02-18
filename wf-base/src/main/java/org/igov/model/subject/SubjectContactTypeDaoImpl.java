package org.igov.model.subject;

import com.google.common.base.Optional;
import org.hibernate.criterion.Restrictions;
import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * User: goodg_000
 * Date: 27.12.2015
 * Time: 17:53
 */
@Repository
public class SubjectContactTypeDaoImpl extends GenericEntityDao<Long, SubjectContactType>
        implements SubjectContactTypeDao {

    private final static String NAME_FIELD = "sName_EN";

    public SubjectContactTypeDaoImpl() {
        super(SubjectContactType.class);
    }

    @Override
    public SubjectContactType getEmailType() {

        return findBy(NAME_FIELD, "Email").get();
    }

    @Override
    public SubjectContactType getPhoneType() {
        return findBy(NAME_FIELD, "Phone").get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<SubjectContactType> findByName(String sName) {
        List<SubjectContactType> aoResult = (List<SubjectContactType>) createCriteria()
                .add(Restrictions.or(
                Restrictions.eq(NAME_FIELD, sName),
                Restrictions.eq("sName_UA", sName),
                Restrictions.eq("sName_RU", sName))
        ).list();
        return aoResult.size() > 0 ? Optional.of(aoResult.get(0)) : Optional.absent();
    }
}
