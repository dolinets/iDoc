package org.igov.service.business.document;

import java.util.List;
import org.igov.model.document.Document;
import org.igov.model.document.DocumentDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author alex
 */
@Component("documentService")
public class DocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentStatutoryService.class);

    //interface realization TODO
    @Autowired
    private DocumentDaoImpl documentDaoImpl;

    public List<Document> getListDocuments(Long nID_Subject, List<Long> anID_DocumentType) {
        LOG.info(String.format("find Document entities with nID_Subject=%s and anID_DocumentType=%s", nID_Subject, anID_DocumentType));
        return documentDaoImpl.getListDocument(nID_Subject, anID_DocumentType);
    }
}
