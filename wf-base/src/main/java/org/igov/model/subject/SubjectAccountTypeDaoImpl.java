package org.igov.model.subject;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SubjectAccountTypeDaoImpl extends GenericEntityDao<Long, SubjectAccountType>
        implements SubjectAccountTypeDao {

    public SubjectAccountTypeDaoImpl() {
        super(SubjectAccountType.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SubjectAccountType> getSubjectAccountTypes(String sID, String sNote) {
        Criteria criteria = getSession().createCriteria(SubjectAccountType.class);
        if (sID != null) {
            criteria.add(Restrictions.eq("sID", sID).ignoreCase());
        }
        if (sNote != null) {
            criteria.add(Restrictions.eq("sNote", sNote).ignoreCase());
        }
        return criteria.list();
    }
}
 