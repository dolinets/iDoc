package org.igov.service.business.document;

import org.igov.model.document.Document;
import org.igov.model.document.DocumentDao;
import org.igov.model.subject.Subject;
import org.igov.model.subject.organ.SubjectOrgan;
import org.igov.model.subject.organ.SubjectOrganDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component("documentService")
public class DocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentService.class);

    @Autowired
    private DocumentDao oDocumentDao;
    @Autowired
    private SubjectOrganDao oSubjectOrganDao;

    public List<Document> getListDocuments(Long nID_Subject, List<Long> anID_DocumentType) {
        LOG.info(String.format("find Document entities with nID_Subject=%s and anID_DocumentType=%s", nID_Subject, anID_DocumentType));
        return oDocumentDao.getDocuments(nID_Subject, anID_DocumentType);
    }

    public List<Document> getListDocuments(String sOKPO) {
        List<Document> aoDocumentResult = new ArrayList<>();
        List<Subject> aoSubject = oSubjectOrganDao
                .findAllBy("sOKPO", sOKPO)
                .stream()
                .map(SubjectOrgan::getoSubject)
                .collect(Collectors.toList());
        if (aoSubject.size() > 0) {
            return oDocumentDao.findAllBy("subject", aoSubject);
        }
        return aoDocumentResult;
    }

}
