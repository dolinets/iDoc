package org.igov.service.business.document;

import java.util.ArrayList;
import java.util.List;
import org.igov.model.document.Document;
import org.igov.model.document.DocumentStatutory;
import org.igov.model.document.DocumentStatutoryDao;
import org.igov.model.document.DocumentTypeGroupTree;
import org.igov.service.business.subject.SubjectOrganService;
import org.igov.model.subject.organ.SubjectOrgan;
import org.igov.service.exception.CommonServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author alex
 */
@Component("documentStatutoryService")
public class DocumentStatutoryService {

    @Autowired
    private DocumentStatutoryDao documentStatutoryDao;

    @Autowired
    private SubjectOrganService subjectOrganService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentTypeGroupTreeService documentTypeGroupTreeService;

    private static final Logger LOG = LoggerFactory.getLogger(DocumentStatutoryService.class);

    /**
     * Get List of DocumentStatutory entities by sOKPO & nID_DocumentTypeGroup
     *
     * @param sOKPO
     * @param nID_DocumentTypeGroup
     * @return List<DocumentStatutory>
     */
    public List<DocumentStatutory> getDocumentStatutory(String sOKPO, Long nID_DocumentTypeGroup) throws Exception {
        LOG.info(String.format("getDocumentStatutory by sOKPO=%s and nID_DocumentTypeGroup=%s", sOKPO, nID_DocumentTypeGroup + "start..."));
        List<DocumentStatutory> aDocumentStatutory = new ArrayList();

        SubjectOrgan oSubjectOrgan = subjectOrganService.getSubjectOrgan(sOKPO);
        if (oSubjectOrgan == null) {
            LOG.error("No oSubjectOrgan found by sOKPO=%s", sOKPO);
            throw new IllegalArgumentException("No SubjectOrgan entity found by sOKPO=%s" + sOKPO + "!");
        }

        Long nID_Subject = oSubjectOrgan.getoSubject().getId();
        LOG.debug(String.format("SubjectOrgan with nID_Subject=%s found", nID_Subject));
        List<DocumentTypeGroupTree> aDocumentTypeGroupTree = documentTypeGroupTreeService.getListDocumentTypeGroupTree(nID_DocumentTypeGroup);
        if (aDocumentTypeGroupTree == null || aDocumentTypeGroupTree.isEmpty()) {
            LOG.error("No DocumentTypeGroupTree found by nID_DocumentTypeGroup=%s", nID_DocumentTypeGroup);
            throw new IllegalStateException("No DocumentTypeGroupTree entity found by nID_DocumentTypeGroup=%s" + nID_DocumentTypeGroup + "!");
        }
        
        List<Long> anID_DocumentType = new ArrayList();
        for (DocumentTypeGroupTree oDocumentTypeGroupTree : aDocumentTypeGroupTree) {
            anID_DocumentType.add(oDocumentTypeGroupTree.getoDocumentType().getId());
        }

        List<Document> aDocument = documentService.getListDocuments(nID_Subject, anID_DocumentType);
        if (aDocument == null || aDocument.isEmpty()) {
            LOG.error("No Documents found by nID_Subject=%s, and anID_DocumentType=%s", nID_Subject, anID_DocumentType);
            throw new CommonServiceException("404", "За даним ЕДРПОУ документів не знайдено!");
        }
        
        List<Long> anID_Document = new ArrayList();
        for (Document oDocument : aDocument) {
            anID_Document.add(oDocument.getId());
        }

        LOG.debug(String.format("List anID_Document=%s found by nID_Subject=%s and anID_DocumentType=%s", anID_Document, nID_Subject, anID_DocumentType));
        aDocumentStatutory = documentStatutoryDao.findAllByDocumentID(anID_Document);

        LOG.info(String.format("Result List of DocumentStatutory entities aDocumentStatutory=%s", aDocumentStatutory));
        return aDocumentStatutory;
    }

}
