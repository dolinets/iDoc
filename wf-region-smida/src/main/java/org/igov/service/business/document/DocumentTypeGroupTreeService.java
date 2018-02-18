package org.igov.service.business.document;

import java.util.List;
import org.igov.model.document.DocumentTypeGroupTree;
import org.igov.model.document.DocumentTypeGroupTreeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author alex
 */
@Component("documentTypeGroupTreeService")
public class DocumentTypeGroupTreeService {

    @Autowired
    private DocumentTypeGroupTreeDao documentTypeGroupTreeDao;

    private static final Logger LOG = LoggerFactory.getLogger(DocumentTypeGroupTreeService.class);

    public List<DocumentTypeGroupTree> getListDocumentTypeGroupTree(Long nID_DocumentTypeGroup) {
        return documentTypeGroupTreeDao.findAllByDocumentTypeGroupID(nID_DocumentTypeGroup);
    }

}
