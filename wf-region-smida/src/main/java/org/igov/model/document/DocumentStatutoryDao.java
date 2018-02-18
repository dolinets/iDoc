package org.igov.model.document;

import java.util.List;
import org.igov.model.core.EntityDao;

/**
 *
 * @author alex
 */
public interface DocumentStatutoryDao extends EntityDao<Long, DocumentStatutory>{
    
    public List<DocumentStatutory> findAllByDocumentID(List<Long> anID_Document);
    
}
