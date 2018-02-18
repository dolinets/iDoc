package org.igov.model.document;

import java.util.List;
import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

/**
 * @author alex
 */
@Repository
public class TermTypeDaoImpl extends GenericEntityDao<Long, TermType> implements TermTypeDao {

    protected TermTypeDaoImpl() {
        super(TermType.class);
    }

    @Override
    public List<TermType> findAllTermType() {
        return findAll();
    }

}
