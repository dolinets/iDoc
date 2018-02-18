package org.igov.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectGroupResultTree;
import org.igov.model.subject.SubjectRightBP;
import org.igov.service.business.subject.*;
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
@Api(tags = {"SubjectGroupController — Организационная иерархия"})
@RequestMapping(value = "/subject/group")
public class SubjectGroupCommonController {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectGroupCommonController.class);

    @Autowired
    private SubjectGroupService subjectGroupService;

    @Autowired
    private SubjectGroupTreeService subjectGroupTreeService;

    @Autowired
    private SubjectOrganService oSubjectOrganService;

    @Autowired
    SubjectRightBPService subjectRightBPService;

    @ApiOperation(value = "Получение организационной иерархии вниз", notes = "##### Пример:"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/group/getSubjectGroupsTree?sID_Group_Activiti=MJU_Dnipro&sSubjectType=Organ&nDeepLevel=1&sFind=Управління"
    )
    @ApiResponse(code = 200, message = "{\n"
            + "  \"aSubjectGroupTree\": [\n"
            + "    {\n"
            + "      \"sID_Group_Activiti\": \"MJU_Dnipro_2\",\n"
            + "      \"sChain\": \"MJU_Dnipro_\",\n"
            + "      \"aUser\": [],\n"
            + "      \"oSubject\": {\n"
            + "        \"sID\": \"\",\n"
            + "        \"sLabel\": \"Управління державної реєстрації\",\n"
            + "        \"sLabelShort\": \"\",\n"
            + "        \"oSubjectStatus\": null,\n"
            + "        \"aSubjectAccountContact\": [],\n"
            + "        \"nID\": 273\n"
            + "      },\n"
            + "      \"oSubjectHumanPositionCustom\": {\n"
            + "        \"sNote\": \"Программист\",\n"
            + "        \"nID\": 1,\n"
            + "        \"sName\": \"Programmer\"\n"
            + "      },\n"
            + "      \"nID\": 273,\n"
            + "      \"sName\": \"Управління державної реєстрації\",\n"
            + "      \"aSubjectGroupChilds\": null,\n"
            + "      \"sName_SubjectGroupCompany\": \"ГТУЮ\"\n"
            + "    },\n"
            + "    ...\n"
            + "  ]\n"
            + "}")
    @RequestMapping(value = "/getSubjectGroupsTree", method = RequestMethod.GET)
    @ResponseBody
    public SubjectGroupResultTree getSubjectGroupsTree(
            @ApiParam(value = "ид группы", required = true)
            @RequestParam(value = "sID_Group_Activiti") String sID_Group_Activiti,
            @ApiParam(value = "глубина выборки", required = false)
            @RequestParam(value = "nDeepLevel", required = false) Long nDeepLevel,
            @ApiParam(value = "текст поиска (искать в ФИО, по наличию вхождения текста в ФИО)", required = false)
            @RequestParam(value = "sFind", required = false) String sFind,
            @ApiParam(value = "Флаг отображения рутового элемента для всей иерархии (true-отоборажаем, false-нет, по умолчанию false)", required = false)
            @RequestParam(value = "bIncludeRoot", required = false) Boolean bIncludeRoot,
            @ApiParam(value = "Ширина выборки", required = false)
            @RequestParam(value = "nDeepLevelWidth", required = false) Long nDeepLevelWidth,
            @ApiParam(value = "Тип выборки: Organ- иерархия в разрезе органы,  Human -иерархия в разрезе людей, * - иерархия органы+люди", required = false)
            @RequestParam(value = "sSubjectType", required = false) String sSubjectType)
            throws Exception {
        return subjectGroupTreeService.getCatalogSubjectGroupsTree(sID_Group_Activiti, nDeepLevel,
                sFind, bIncludeRoot, nDeepLevelWidth, sSubjectType);
    }

    @ApiOperation(value = "Получение организационной иерархии вверх", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/group/getSubjectGroupsTreeUp?sID_Group_Activiti=OGOK_010158KAV&sSubjectType=Organ"
    )
    @ApiResponse(code = 200, message = "[\n"
            + "  {\n"
            + "    \"sID_Group_Activiti\": \"OGOK_OZK602000300\",\n"
            + "    \"sChain\": \"OGOK_\",\n"
            + "    \"aUser\": null,\n"
            + "    \"oSubject\": {\n"
            + "      \"sID\": \"\",\n"
            + "      \"sLabel\": \"Ділянка № 3 ПММ СОП\",\n"
            + "      \"sLabelShort\": \"\",\n"
            + "      \"oSubjectStatus\": null,\n"
            + "      \"aSubjectAccountContact\": [],\n"
            + "      \"nID\": 7388\n"
            + "    },\n"
            + "    \"oSubjectHumanPositionCustom\": {\n"
            + "      \"sNote\": \"група департаменту\",\n"
            + "      \"nID\": 1111,\n"
            + "      \"sName\": \"OGOK_groupDepartment\"\n"
            + "    },\n"
            + "    \"nID\": 7388,\n"
            + "    \"sName\": \"Ділянка № 3 ПММ СОП\",\n"
            + "    \"aSubjectGroupChilds\": null,\n"
            + "    \"sName_SubjectGroupCompany\": null\n"
            + "  }\n"
            + "]"
    )
    @RequestMapping(value = "/getSubjectGroupsTreeUp", method = RequestMethod.GET)
    @ResponseBody
    public List<SubjectGroup> getSubjectGroupsTreeUp(
            @ApiParam(value = "Идентификатор группы", required = true)
            @RequestParam(value = "sID_Group_Activiti", required = true) String sID_Group_Activiti,
            @ApiParam(value = "глубина выборки", required = false)
            @RequestParam(value = "nDeepLevel", required = false) Long nDeepLevel,
            @ApiParam(value = "Флаг отображения рутового элемента для всей иерархии (true-отоборажаем, false-нет, по умолчанию yes)", required = false)
            @RequestParam(value = "bIncludeRoot", required = false) Boolean bIncludeRoot,
            @ApiParam(value = "Тип выборки: Organ- иерархия в разрезе органы,  Human -иерархия в разрезе людей", required = false)
            @RequestParam(value = "sSubjectType", required = false) String sSubjectType) {

        return subjectGroupTreeService.getSubjectGroupsTreeUp(sID_Group_Activiti, sSubjectType, nDeepLevel);

    }

    @ApiOperation(value = "Получение доступных бизнес-процессов по критерию", notes = "##### Пример:\n" +
        "https://alpha.test.region.igov.org.ua/wf/service/subject/group/getBPs_ForReferent?nID_SubjectHumanPositionCustom_Referent=1")
    @ApiResponse(code = 200, responseContainer = "List", response = SubjectRightBP.class, message = "[\n"
            + "  {\n"
            + "    \"oSubjectRightBP\": {\n"
            + "      \"sID_BP\": \"_doc_nssmc_enforcement\",\n"
            + "      \"sID_Place_UA\": \"\",\n"
            + "      \"sID_Group_Referent\": \"justice_common\",\n"
            + "      \"asID_Group_Export\": \"justice_common\",\n"
            + "      \"sFormulaFilter_Export\": null,\n"
            + "      \"sNote\": null,\n"
            + "      \"nID_SubjectHumanPositionCustom_Referent\": null,\n"
            + "      \"nID\": 443\n"
            + "    },\n"
            + "    \"sName_BP\": \"Правозастосування\"\n"
            + "  },\n"
            + "  ...\n"
            + "]")
    @RequestMapping(value = "/getBPs_ForReferent", method = RequestMethod.GET)
    @ResponseBody
    public List<SubjectRightBPVO> getBPs_ForReferent(
            @ApiParam("Логин сотрудника") @RequestParam(required = false) String sLogin,
            @ApiParam("Логин сотрудника (админка)") @RequestParam(required = false) String sLoginStaff,
            @ApiParam("sID органа-департамента") @RequestParam(required = false) String sID_Group_Referent,
            @ApiParam("nID должности сотрудника") @RequestParam(required = false) Long nID_SubjectHumanPositionCustom_Referent
    ) {
        if(sLoginStaff != null){
            sLogin = sLoginStaff;
        }
        return subjectRightBPService.getBPs_ForReferent(sLogin, sID_Group_Referent, nID_SubjectHumanPositionCustom_Referent);
    }

    @RequestMapping(value = "/getBPs_ForExport", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, String>> getBPs_ForExport(
            @ApiParam(value = "Логин сотрудника", required = false)
            @RequestParam(required = false, value = "sLogin") String sLogin)
            throws Exception {

        return subjectRightBPService.getBPs_ForExport(sLogin);
    }

    @RequestMapping(value = "/checkAllSubjects", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List<SubjectGroup>> checkAllSubjects(
            @ApiParam(value = "id для старта (по органам и департаментам - одновременно)", required = false)
            @RequestParam(required = true, value = "nID") Integer nID,
            @ApiParam(value = "Количество обрабатываемых элементов", required = false)
            @RequestParam(required = true, value = "count") Integer count,
            @ApiParam(value = "Тип проверяемого справочника - Organ или Human", required = false)
            @RequestParam(required = true, value = "sType") String sType) {
        return subjectGroupTreeService.checkAllSubjects(nID, count, sType);
    }

    @RequestMapping("/removeSelfReference")
    public void temp_removeSelfReference(@RequestParam String sID) {
        subjectGroupTreeService.tempRemoveSelfReference(sID);
    }

}
