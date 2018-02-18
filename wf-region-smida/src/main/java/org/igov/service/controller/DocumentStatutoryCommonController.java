package org.igov.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import org.igov.model.document.DocumentStatutory;
import org.igov.service.business.document.DocumentStatutoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author alex
 */
@RestController
@Api(tags = {"DocumentStatutoryCommonController — Организация взаимосвязей с сущностями статутного документа"})
@RequestMapping(value = "/document/statutory")
public class DocumentStatutoryCommonController {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentStatutoryCommonController.class);

    @Autowired
    private DocumentStatutoryService documentStatutoryService;

    @ApiOperation(value = "Возврат списка Статутных документов", notes = "##### Пример:\n"
            + "https://theta.test.region.igov.org.ua/wf/service/document/statutory/getListDocumentStatutory?sOKPO=<sOKPO>&nID_DocumentTypeGroup=<nID_DocumentTypeGroup> \n")
    @RequestMapping(value = "/getListDocumentStatutory", method = RequestMethod.GET)
    public List<DocumentStatutory> getListDocumentStatutory(
            @ApiParam(value = "ЕДРПОУ", required = true) @RequestParam(value = "sOKPO") String sOKPO,
            @ApiParam(value = "ид группы типа документа", required = true) @RequestParam(value = "nID_DocumentTypeGroup") Long nID_DocumentTypeGroup)
            throws Exception {
        return documentStatutoryService.getDocumentStatutory(sOKPO, nID_DocumentTypeGroup);
    }

}
