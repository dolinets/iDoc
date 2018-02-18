package org.igov.model.document;

import java.util.List;
import org.igov.model.core.EntityDao;

/**
 *
 * @author alex
 */
public interface DocumentTypeGroupTreeDao extends EntityDao<Long, DocumentTypeGroupTree>{
    
    public List<DocumentTypeGroupTree> findAllByDocumentTypeGroupID(Long nID_DocumentTypeGroup);
    
}
