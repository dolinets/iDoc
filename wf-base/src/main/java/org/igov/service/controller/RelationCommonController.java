package org.igov.service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.igov.model.action.vo.Relation_VO;
import org.igov.model.relation.ObjectGroup;
import org.igov.service.business.relation.RelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.transaction.annotation.Transactional;
 

/**
 *
 * @author Kovilin
 */
@Controller
@Api(tags = {"RelationController — Обработка Relation"})
@RequestMapping(value = "/relation")
public class RelationCommonController {
    
    private static final Logger LOG = LoggerFactory.getLogger(RelationCommonController.class);
    
    @Autowired
    RelationService oRelationService;
    
    @ApiOperation(value = "Обработка Relation", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/relation/getRelations?sID_Relation=1&sFindChild=gro \n")
    @RequestMapping(value = "/getRelations", method = RequestMethod.GET)
    @Transactional
    public @ResponseBody
    List<Relation_VO> getRelations (@RequestParam(value = "sID_Relation", required = true) String sID_Relation,
                                    @RequestParam(value = "sFindChild", required = false) String sFindChild,
                                    @RequestParam(value = "nID_Parent", required = false) Long nID_Parent) throws Exception
    {
        LOG.info("getRelations started");
        return oRelationService.getRelations(sID_Relation, nID_Parent, sFindChild);
    }
    
    @ApiOperation(value = "Обработка Relation", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/relation/getObjectGroupByPrivate_Source?sID_Private_Source=1 \n")
    @RequestMapping(value = "/getObjectGroupByPrivate_Source", method = RequestMethod.GET)
    @Transactional
    public @ResponseBody
    ObjectGroup getObjectGroupByPrivate_Source (@RequestParam(value = "sID_Private_Source", required = true) String sID_Private_Source) throws Exception
    {
        LOG.info("getObjectGroupByPrivate_Source");
        return oRelationService.getObjectGroupBySubject_Source(sID_Private_Source);
    }
    
    @ApiOperation(value = "Обработка Relation", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/relation/getObjectGroup?sLogin=1 \n")
    @RequestMapping(value = "/getObjectGroup", method = RequestMethod.GET)
    @Transactional
    public @ResponseBody
    ObjectGroup getObjectGroup (@RequestParam(value = "sLogin", required = true) String sLogin) throws Exception
    {
        LOG.info("getObjectGroup");
        return oRelationService.getCompany(sLogin);
    }
    
    @ApiOperation(value = "Обработка Relation", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/relation/getObjectGroupParent?sLogin=1 \n")
    @RequestMapping(value = "/getObjectGroupParent", method = RequestMethod.GET)
    @Transactional
    public @ResponseBody
    ObjectGroup getObjectGroupParent (@RequestParam(value = "sLogin", required = true) String sLogin) throws Exception
    {
        LOG.info("getObjectGroupParent");
        return oRelationService.getObjectGroupParent(sLogin);
    }
}