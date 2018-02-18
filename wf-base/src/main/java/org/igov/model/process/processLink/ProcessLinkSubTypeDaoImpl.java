package org.igov.model.process.processLink;

import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

/**
 *
 * @author idenysenko
 */
@Repository
public class ProcessLinkSubTypeDaoImpl extends GenericEntityDao<Long, ProcessLink_SubType> implements ProcessLinkSubTypeDao {
    
    ProcessLinkSubTypeDaoImpl() {
        super(ProcessLink_SubType.class);
    }
}
