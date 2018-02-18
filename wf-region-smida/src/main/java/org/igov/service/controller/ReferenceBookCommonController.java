package org.igov.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import org.igov.model.action.event.ActionEventType;
import org.igov.model.dictionary.Dictionary;
import org.igov.model.document.TermType;
import org.igov.model.registry.nssmc.RegistryNSSMC;
import org.igov.service.business.reference.book.ReferenceBookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author alex
 */
@RestController
@Api(tags = {"ReferenceBookCommonController — Организация взаимосвязей с сущностями справочников"})
@RequestMapping(value = "/reference/book")
public class ReferenceBookCommonController {

    private static final Logger LOG = LoggerFactory.getLogger(ReferenceBookCommonController.class);

    @Autowired
    private ReferenceBookService referenceBookService;
    
    @ApiOperation(value = "Возврат списка Справочников", notes = "##### Пример:\n"
            + "https://theta.test.region.igov.org.ua/wf/service/reference/book/getListDictionary?nID_DictionaryType=<nID_DictionaryType> \n")
    @RequestMapping(value = "/getListDictionary", method = RequestMethod.GET)
    public List<Dictionary> getListDictionary(@ApiParam(value = "ид типа справочника", required = true) @RequestParam(value = "nID_DictionaryType") Long nID_DictionaryType)
            throws Exception {
        return referenceBookService.getListDictionary(nID_DictionaryType);
    }
    
    @ApiOperation(value = "Возврат списка Группы Типа Действий", notes = "##### Пример:\n"
            + "https://theta.test.region.igov.org.ua/wf/service/reference/book/getListActionEventType?nID_ActionEventTypeGroup=<nID_ActionEventTypeGroup> \n")
    @RequestMapping(value = "/getListActionEventType", method = RequestMethod.GET)
    public List<ActionEventType> getListActionEventType(@ApiParam(value = "ид группы типа действия", required = true) @RequestParam(value = "nID_ActionEventTypeGroup") Long nID_ActionEventTypeGroup)
            throws Exception {
        return referenceBookService.getListActionEventType(nID_ActionEventTypeGroup);
    }
    
    @ApiOperation(value = "Возврат списка Типов Действий", notes = "##### Пример:\n"
            + "https://theta.test.region.igov.org.ua/wf/service/reference/book/getListTermType \n")
    @RequestMapping(value = "/getListTermType", method = RequestMethod.GET)
    public List<TermType> getListTermType()
            throws Exception {
        return referenceBookService.getListTermType();
    }
    
    @ApiOperation(value = "Возврат список реестров НКЦПФР", notes = "##### Пример:\n"
            + "https://theta.test.region.igov.org.ua/wf/service/reference/book/getRegisryListNSSMC \n")
    @RequestMapping(value = "/getRegisryListNSSMC", method = RequestMethod.GET)
    public List<RegistryNSSMC> getRegisryListNSSMC()
            throws Exception {
        return referenceBookService.getListRegistryNSSMC();
    }

}
