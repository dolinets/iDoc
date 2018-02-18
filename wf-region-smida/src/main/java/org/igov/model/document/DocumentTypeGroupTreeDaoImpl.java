package org.igov.model.document;

import java.util.List;
import org.igov.model.core.GenericEntityDao;
import org.springframework.stereotype.Repository;

/**
 *
 * @author alex
 */
@Repository
public class DocumentTypeGroupTreeDaoImpl extends GenericEntityDao<Long, DocumentTypeGroupTree> implements DocumentTypeGroupTreeDao {
    
        protected DocumentTypeGroupTreeDaoImpl() {
        super(DocumentTypeGroupTree.class);
    }

    @Override
    public List<DocumentTypeGroupTree> findAllByDocumentTypeGroupID(Long nID_DocumentTypeGroup) {
        return findAllBy("oDocumentTypeGroup.id", nID_DocumentTypeGroup);
    }
    
}
