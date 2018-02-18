package org.igov.service.controller.subject.group;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectGroupResultTree;
import org.igov.service.business.subject.SubjectGroupService;
import org.igov.service.business.subject.SubjectOrganService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@Api(tags = {"SubjectGroup", "Subject", "SubjectHuman", "SubjectOrgan"})
@RequestMapping(value = "/subject/group")
public class SubjectGroupController {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectGroupController.class);

    @Autowired
    private SubjectGroupService oSubjectGroupService;
    @Autowired
    private SubjectOrganService oSubjectOrganService;

    @ApiOperation(value = "Получение человека или органа по его логину",
            response = SubjectGroup.class,
            notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/group/getSubjectGroup?sID_Group_Activiti=btsol_010188FKL\n"
    )
    @RequestMapping(value = "/getSubjectGroup", method = RequestMethod.GET)
    @ResponseBody
    public SubjectGroup getSubjectGroup(
            @ApiParam(value = "ид группы", required = true) @RequestParam String sID_Group_Activiti) {
        return oSubjectGroupService.getSubjectGroup(sID_Group_Activiti);
    }

    @RequestMapping(value = "/findInCompany", method = RequestMethod.GET)
    @ResponseBody
    public List<SubjectGroup> findSubjectGroup(
            @ApiParam(required = true, value = "Строковый ID компании или отдела в компании")
            @RequestParam String sID_Group_Activiti_Company,
            @ApiParam(required = true, value = "Поисковое значение по вхождению ФИО")
            @RequestParam String sFind,
            @ApiParam(required = true, allowableValues = "Human,Organ", value = "Тип сущности") @RequestParam String sSubjectType
    ) {
        return oSubjectGroupService.findSubjectGroupInCompany(sID_Group_Activiti_Company, sFind, sSubjectType);
    }

    @ApiOperation(value = "Получение организационной иерархии",
            response = SubjectGroupResultTree.class,
            notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/group/getSubjectGroups?sID_Group_Activiti=MJU_Dnipro&nDeepLevel=1 \n"
            + "Ответ: HTTP STATUS 200\n\n"
            + "\n```json\n"
            + "{\n"
            + "\"aSubjectGroup\": [\n"
            + "{\n"
            + "\"sID_Group_Activiti\": \"MJU_Dnipro_Top3\",\n"
            + "\"sChain\": \"MJU_Dnipro_\",\n"
            + "\"nID\": 172,\n"
            + "\"sName\": \"Управління державної виконавчої служби-начальник управління\"\n"
            + "	}\n"
            + "	],\n"
            + "aSubjectUser\": [\n"
            + "{\n"
            + "\"sLogin\": \"MJU_common\",\n"
            + "\"sFirstName\": \"мінюст\",\n"
            + "sLastName\": \"тестовий користувач\",\n"
            + "sEmail\":,\n"
            + "sPicture\": \"null\n"
            + "        }\n"
            + "    ]\n"
            + "}\n\n"
            + "\n```\n"
    )
    @RequestMapping(value = "/getSubjectGroups", method = RequestMethod.GET)
    @ResponseBody
    public SubjectGroupResultTree getSubjectGroups(
            @ApiParam(required = true, value = "ид группы")
            @RequestParam String sID_Group_Activiti,
            @ApiParam("глубина выборки")
            @RequestParam(value = "nDeepLevel", required = false) Long nDeepLevel,
            @ApiParam(required = true, value = "текст поиска (искать в ФИО, по наличию вхождения текста в ФИО)")
            @RequestParam String sFind) {
        return oSubjectGroupService.getaSubjectGroup(sID_Group_Activiti, sFind);
    }

    @ApiOperation("Получение иерархии сотрудников и отделов вниз без соблюдения связей")
    @ApiResponse(code = 200, message = "{\n"
            + "  \"oChildSubject\": {\n"
            + "    \"aoChildHuman\": [\n"
            + "      {\n"
            + "        \"aSubjectHumanRole\": null,\n"
            + "        \"subjectHumanIdType\": \"INN\",\n"
            + "        \"aContact\": null,\n"
            + "        \"nID\": 1,\n"
            + "        \"sName\": \"Тимур Заурбекович\",\n"
            + "        \"oSubject\": {},\n"
            + "        \"sINN\": null,\n"
            + "        \"sSB\": null,\n"
            + "        \"sPassportSeria\": null,\n"
            + "        \"sPassportNumber\": null,\n"
            + "        \"sFamily\": null,\n"
            + "        \"sSurname\": \"Хромаєв\",\n"
            + "        \"oDefaultEmail\": null,\n"
            + "        \"oDefaultPhone\": null,\n"
            + "        \"oSex\": null,\n"
            + "        \"oServer\": null,\n"
            + "        \"sDateBirth\": null\n"
            + "      }\n"
            + "    ],\n"
            + "    \"aoChildOrgan\": [\n"
            + "      {\n"
            + "        \"aContact\": null,\n"
            + "        \"nID\": 220,\n"
            + "        \"sName\": \"НКЦПФР\",\n"
            + "        \"oSubject\": {},\n"
            + "        \"sOKPO\": \"37956207\",\n"
            + "        \"sFormPrivacy\": \"Орган державної влади\",\n"
            + "        \"sNameFull\": \"Керівництво Комісії\",\n"
            + "        \"nSizeCharterCapital\": null,\n"
            + "        \"sSeriesRegistrationEDR\": null,\n"
            + "        \"sNumberRegistrationEDR\": null,\n"
            + "        \"sDateRegistrationEDR\": null,\n"
            + "        \"sOKPOForeign\": null,\n"
            + "        \"oSubjectOrganType\": null,\n"
            + "        \"oCountry\": null\n"
            + "      }\n"
            + "    ]\n"
            + "  },\n"
            + "  \"oParentSubject\": {\n"
            + "    \"aContact\": null,\n"
            + "    \"nID\": 1,\n"
            + "    \"sName\": \"НКЦПФР\",\n"
            + "    \"oSubject\": {},\n"
            + "    \"sOKPO\": \"37956207\",\n"
            + "    \"sFormPrivacy\": \"Орган державної влади\",\n"
            + "    \"sNameFull\": \"Національна Комісія з Цінних Паперів та Фондового Ринку\",\n"
            + "    \"nSizeCharterCapital\": null,\n"
            + "    \"sSeriesRegistrationEDR\": null,\n"
            + "    \"sNumberRegistrationEDR\": null,\n"
            + "    \"sDateRegistrationEDR\": null,\n"
            + "    \"sOKPOForeign\": null,\n"
            + "    \"oSubjectOrganType\": null,\n"
            + "    \"oCountry\": null\n"
            + "  }\n"
            + "}")
    @RequestMapping(value = "/getSubjectTree", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getChildren(@ApiParam(required = true, value = "ЕДРПОУ") @RequestParam String sOKPO) {
        return oSubjectOrganService.getChildren(sOKPO);
    }

}
