package org.igov.model.relation;

import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

@Repository
public class ObjectGroupAttributeDaoImpl extends GenericEntityDao<Long, ObjectGroupAttribute>
        implements ObjectGroupAttributeDao {

    public ObjectGroupAttributeDaoImpl() {
        super(ObjectGroupAttribute.class);
    }
}
