package org.igov.model.relation;

import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

@Repository
public class AttributeObjectDaoImpl extends GenericEntityDao<Long, AttributeObject> implements AttributeObjectDao {

    public AttributeObjectDaoImpl() {
        super(AttributeObject.class);
    }
}
