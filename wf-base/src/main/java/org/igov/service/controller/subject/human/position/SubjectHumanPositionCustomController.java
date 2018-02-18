package org.igov.service.controller.subject.human.position;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.igov.model.subject.SubjectHumanPositionCustom;
import org.igov.service.business.subject.SubjectHumanPositionCustomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@Api(description = "Работа с должностями сотрудников", tags = "SubjectHumanPositionCustom")
@RequestMapping(value = "/subject")
public class  SubjectHumanPositionCustomController {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectHumanPositionCustomController.class);

    @Autowired
    private SubjectHumanPositionCustomService oSubjectHumanPositionCustomService;

    @ApiOperation(value = "Получение списка должностей, всех или по критерию",
            response = SubjectHumanPositionCustom.class,
            responseContainer = "List",
            notes = "##### Пример:\n"
                    + "https://alpha.test.region.igov.org.ua/wf/service/subject/getSubjectHumanPositionCustom?sFind=Началь\n"
    )
    @ApiResponse(code = 200, message = "[\n"
            + "  {\n"
            + "    \"sNote\": \"Начальник відділу\",\n"
            + "    \"nID\": 159,\n"
            + "    \"sName\": \"AZOT_Head of Department\"\n"
            + "  },\n"
            + "  {\n"
            + "    \"sNote\": \"Начальник дільниці\",\n"
            + "    \"nID\": 160,\n"
            + "    \"sName\": \"AZOT_Head of the district\"\n"
            + "  },\n"
            + "  ...\n"
            + "]")
    @RequestMapping(value = "/getSubjectHumanPositionCustom")
    @ResponseBody
    public List<SubjectHumanPositionCustom> getSubjectHumanPositionCustom(
            @ApiParam("Поисковое значение по названию должности") @RequestParam(required = false) String sFind,
            @ApiParam("Логин сотрудника, в чьей компании искать должности") @RequestParam String sLogin,
            @ApiParam("Логин-референт") @RequestParam String sLoginReferent) {
        return oSubjectHumanPositionCustomService.getSubjectHumanPositionCustoms(sLogin, sFind, sLoginReferent);
    }

    @ApiOperation(value = "Сохранение/редактирование должности",
            response = SubjectHumanPositionCustom.class,
            notes = "Посылать без nID для сохранения, с - для обновления.\n ##### Пример:\n"
                    + "https://alpha.test.region.igov.org.ua/wf/service/subject/setSubjectHumanPositionCustom?sNote=Тестовая позиция"
    )
    @ApiResponse(code = 200, message = "{\n"
            + "  \"sNote\": \"Тестовая должность\",\n"
            + "  \"nID\": 2351,\n"
            + "  \"sName\": \"testovaya dolgnost'\"\n"
            + "}")
    @RequestMapping(value = "/setSubjectHumanPositionCustom")
    @ResponseBody
    public SubjectHumanPositionCustom setSubjectHumanPositionCustom(
            @ApiParam(required = true, value = "Наименование должности") @RequestParam String sNote,
            @ApiParam("Строковый ID должности") @RequestParam(required = false) String sName,
            @ApiParam("Вызывается ли сервис через механизм синхронизации") @RequestParam(required = false) Boolean isSync,
            @ApiParam("Признак создания/редактирования") @RequestParam(required = false) Boolean bCreate,
            @ApiParam("Логин, под кем заводится новая должность") @RequestParam(required = false) String sLogin,
            @ApiParam("Цепь") @RequestParam(required = false) String sChain,
            @ApiParam("Логин-референт") @RequestParam(required = false) String sLoginReferent) {
        return oSubjectHumanPositionCustomService.setSubjectHumanPositionCustom(sNote, sName, isSync, bCreate, sLogin, sLoginReferent, sChain);
    }
}
