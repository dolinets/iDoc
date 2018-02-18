package org.igov.service.controller.subject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.igov.model.subject.*;
import org.igov.service.business.subject.SubjectContactTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Api(description = "Работа с типами контактов", tags = "SubjectContactType")
@RequestMapping(value = "/subject")
public class SubjectContactTypeController {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectContactTypeController.class);

    @Autowired
    private SubjectContactTypeService oSubjectContactTypeService;

    @ApiOperation(value = "Получение всех типов контактов",
            response = SubjectContactType.class,
            responseContainer = "List",
            notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/getSubjectContactType\n"
    )
    @ApiResponse(code = 200, message = "[\n"
            + "  {\n"
            + "    \"sName_EN\": \"Phone\",\n"
            + "    \"sName_UA\": \"Телефон\",\n"
            + "    \"sName_RU\": \"Телефон\",\n"
            + "    \"nID\": 0\n"
            + "  },\n"
            + "  ...\n"
            + "]")
    @RequestMapping(value = "/getSubjectContactType", method = RequestMethod.GET)
    @ResponseBody
    public List<SubjectContactType> getSubjectContactType() {
        return oSubjectContactTypeService.getSubjectContactTypes();
    }

    @ApiOperation(value = "Создание/обновление типа контакта",
            response = SubjectContactType.class,
            notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/setSubjectContactType?sName_EN=Bell&sName_RU=Колокол&sName_UA=Дзвін\n"
    )
    @ApiResponse(code = 200, message = "{\n"
            + "  \"sName_EN\": \"Bell\",\n"
            + "  \"sName_UA\": \"Дзвін\",\n"
            + "  \"sName_RU\": \"Колокол\",\n"
            + "  \"nID\": 10\n"
            + "}")
    @RequestMapping(value = "/setSubjectContactType", method = RequestMethod.GET)
    @ResponseBody
    public SubjectContactType setSubjectContactType(
            @ApiParam("Порядковый ID типа контакта, используется для обновления записи") @RequestParam(value = "nID", required = false) Long nID,
            @ApiParam("Название на английском") @RequestParam(required = false) String sName_EN,
            @ApiParam("Название на русском") @RequestParam(required = false) String sName_RU,
            @ApiParam("Название на украинском") @RequestParam(required = false) String sName_UA) {
        return oSubjectContactTypeService.setSubjectContactType(nID, sName_EN, sName_RU, sName_UA);
    }

    @ApiResponse(code = 204, message = "")
    @RequestMapping(value = "/deleteSubjectContactType", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubjectContactType(
            @ApiParam(value = "Порядковый ID типа контакта", required = true) @RequestParam("nID") Long id) {
        oSubjectContactTypeService.deleteSubjectContactType(id);
    }
    
}
