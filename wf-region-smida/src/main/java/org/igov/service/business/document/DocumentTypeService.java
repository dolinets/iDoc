/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.business.document;

import org.igov.model.document.DocumentType;
import org.igov.model.document.DocumentTypeDao;
import org.igov.service.business.reference.book.ReferenceBookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author alex
 */
@Component("documentTypeService")
public class DocumentTypeService {
    
    @Autowired
    private DocumentTypeDao documentTypeDao;
    
    private static final Logger LOG = LoggerFactory.getLogger(ReferenceBookService.class);
    
    public DocumentType getDocumentType (Long nID){
        LOG.info(String.format("find DocumentType entity with nID=%s", nID));
        return documentTypeDao.findByIdExpected(nID);
    }
    
}
