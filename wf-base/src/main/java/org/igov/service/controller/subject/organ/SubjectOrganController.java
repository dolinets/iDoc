package org.igov.service.controller.subject.organ;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.igov.model.subject.SubjectGroup;
import org.igov.service.business.subject.SubjectOrganService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Api(description = "Управление организацией/отделами", tags = {"SubjectOrgan", "SubjectGroup", "Subject"})
@RequestMapping(value = "/subject")
public class SubjectOrganController {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectOrganController.class);

    @Autowired private SubjectOrganService oSubjectOrganService;

    @ApiOperation(value = "Создание/обновление данных о компании (отделе)",
            response = SubjectGroup.class,
            notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/setSubjectOrgan?sName=ТК&sNameFull=Тестовая компания&sID_Group_Activiti=TK&sOKPO=ОКПО"
    )
    @ApiResponse(code = 200, message = "{\n"
            + "  \"sID_Group_Activiti\": \"TK\",\n"
            + "  \"sChain\": \"TK\",\n"
            + "  \"aUser\": null,\n"
            + "  \"oSubject\": {\n"
            + "    \"sID\": \"TK\",\n"
            + "    \"sLabel\": \"Тестовая компания\",\n"
            + "    \"sLabelShort\": \"ТК\",\n"
            + "    \"oSubjectStatus\": null,\n"
            + "    \"aSubjectAccountContact\": [],\n"
            + "    \"nID\": 1603248\n"
            + "  },\n"
            + "  \"oSubjectHumanPositionCustom\": {\n"
            + "    \"sNote\": \"група департаменту\",\n"
            + "    \"nID\": 21,\n"
            + "    \"sName\": \"groupDepartment\"\n"
            + "  },\n"
            + "  \"nID\": 1603250,\n"
            + "  \"sName\": \"ТК\",\n"
            + "  \"aSubjectGroupChilds\": null,\n"
            + "  \"sName_SubjectGroupCompany\": \"ТК\"\n"
            + "}"
    )
    @RequestMapping(value = "/setSubjectOrgan", method = RequestMethod.GET)
    @ResponseBody
    public SubjectGroup setSubjectOrgan(
            @ApiParam(required = true, value = "Название организации/отдела") @RequestParam String sName,
            @ApiParam(required = true, value = "Флаг редактирования записи") @RequestParam Boolean bCreate,
            @ApiParam("Строковый ID компании. Отделам назначается автоматически") @RequestParam(required = false) String sID_Group_Activiti,
            @ApiParam("Значение единого гос. реестра предприятий и организаций") @RequestParam(required = false, defaultValue = "") String sOKPO,
            @ApiParam("Вызывается ли сервис через механизм синхронизации") @RequestParam(required = false) Boolean isSync,
            @ApiParam("Строковый ID родительского отдела. Без этого параметра подразумевается компания") @RequestParam(required = false) String sID_Group_Activiti_Parent,
            @ApiParam("") @RequestParam(required = false, defaultValue = "") String sFormPrivacy,
            @ApiParam("Контактная почта организации") @RequestParam(required = false) String sEmail,
            @ApiParam("Статус") @RequestParam(required = false) String sStatus) {

        return oSubjectOrganService.setSubjectOrgan(sID_Group_Activiti, sName, sID_Group_Activiti_Parent, sOKPO,
                sFormPrivacy, sEmail, bCreate, isSync, sStatus);
    }
    
}
