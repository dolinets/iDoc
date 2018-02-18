package org.igov.model.process.processLink;

import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

/**
 *
 * @author idenysenko
 */
@Repository
public class ProcessLinkTypeDaoImpl extends GenericEntityDao<Long, ProcessLink_Type> implements ProcessLinkTypeDao {
    
    ProcessLinkTypeDaoImpl() {
        super(ProcessLink_Type.class);
    }
}
