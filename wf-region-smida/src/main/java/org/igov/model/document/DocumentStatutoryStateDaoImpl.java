package org.igov.model.document;

import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

/**
 *
 * @author alex
 */
@Repository
public class DocumentStatutoryStateDaoImpl extends GenericEntityDao<Long, DocumentStatutoryState> implements DocumentStatutoryStateDao {
    
        protected DocumentStatutoryStateDaoImpl() {
        super(DocumentStatutoryState.class);
    }
    
}
