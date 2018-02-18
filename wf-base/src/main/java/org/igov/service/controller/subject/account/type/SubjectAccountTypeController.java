package org.igov.service.controller.subject.account.type;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.igov.model.subject.SubjectAccountType;
import org.igov.service.business.subject.SubjectAccountTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Api(description = "Типы аккаунтов", tags = "SubjectAccountType")
@RequestMapping(value = "/subject")
public class SubjectAccountTypeController {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectAccountTypeController.class);

    @Autowired
    private SubjectAccountTypeService subjectAccountTypeService;

    @ApiOperation(value = "Получение типов аккаунта, всех или по вхождению",
            response = SubjectAccountType.class,
            responseContainer = "List",
            notes = "##### Пример:\n"
                    + "https://alpha.test.region.igov.org.ua/wf/service/subject/getSubjectAccountTypes?sNote=iGov\n"
    )
    @ApiResponse(code = 200, message = "[\n"
            + "  {\n"
            + "    \"sID\": \"iGov\",\n"
            + "    \"sNote\": \"iGov\",\n"
            + "    \"nID\": 1\n"
            + "  },\n"
            + "  {\n"
            + "    \"sID\": \"PD\",\n"
            + "    \"sNote\": \"PD\",\n"
            + "    \"nID\": 2\n"
            + "  },\n"
            + "  {\n"
            + "    \"sID\": \"1C\",\n"
            + "    \"sNote\": \"1C\",\n"
            + "    \"nID\": 3\n"
            + "  }\n"
            + "]")
    @RequestMapping(value = "/getSubjectAccountTypes", method = RequestMethod.GET)
    @ResponseBody
    public List<SubjectAccountType> getSubjectAccountTypes(
            @ApiParam("Название типа аккаунта") @RequestParam(required = false) String sNote
    ) {
        return subjectAccountTypeService.getSubjectAccountTypes(sNote);
    }

    @ApiOperation(value = "Создание нового типа аккаунта",
            response = SubjectAccountType.class,
            notes = "##### Пример:\n"
                    + "https://alpha.test.region.igov.org.ua/wf/service/subject/setSubjectAccountType?nID=007&sID=IAmNewAccountType&sNote=IAmAnyNote\n"
    )
    @ApiResponse(code = 200, message = "")
    @RequestMapping(value = "/setSubjectAccountType", method = RequestMethod.GET)
    @ResponseBody
    public SubjectAccountType setSubjectAccountType(
            @ApiParam(required = true, value = "Название типа аккаунта") @RequestParam String sNote,
            @ApiParam("Порядковый ИД") @RequestParam(required = false) Long nID,
            @ApiParam("Строковый  ИД") @RequestParam(required = false) String sID
    ) {
        return subjectAccountTypeService.setSubjectAccountType(nID, sID, sNote);
    }

    @ApiOperation(value = "Удаление типа аккаунта по его ID", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/deleteSubjectAccountType?nID=targetID\n"
    )
    @RequestMapping(value = "/deleteSubjectAccountType")
    @ApiResponse(code = 204, message = "Ожидаемый результат")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubjectAccountType(
            @ApiParam(required = true, value = "Порядковый ИД типа аккаунта") @RequestParam Long nID
    ) {
        subjectAccountTypeService.deleteSubjectAccountType(nID);
    }

}