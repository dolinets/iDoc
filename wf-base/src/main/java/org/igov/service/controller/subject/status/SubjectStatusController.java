package org.igov.service.controller.subject.status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.igov.model.subject.SubjectStatus;
import org.igov.service.business.subject.SubjectStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@Api(description = "Работа со статусами сабджектов", tags = "SubjectStatus")
@RequestMapping(value = "/subject")
public class SubjectStatusController {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectStatusController.class);

    @Autowired private SubjectStatusService oSubjectStatusService;

    @ApiOperation(value = "Получение всех типов контактов",
            response = SubjectStatus.class,
            responseContainer = "List",
            notes = "##### Пример:" +
                    " https://alpha.test.region.igov.org.ua/wf/service/subject/getSubjectStatus - получить все статусы\n" +
                    "https://alpha.test.region.igov.org.ua/wf/service/subject/getSubjectStatus?sName_SubjectType=Human - получить все статусы для Human\n" +
                    "https://alpha.test.region.igov.org.ua/wf/service/subject/getSubjectStatus?sName_SubjectType=Organ - получить все статусы для Organ\n")
    @ApiResponse(code = 200, message = "[  \n" +
                    "   {  \n" +
                    "      \"sName\":\"Working\",\n" +
                    "      \"sNote\":\"Работает\",\n" +
                    "      \"oSubjectType\":{  \n" +
                    "         \"sName\":\"Human\",\n" +
                    "         \"sDescription\":\"Людина\",\n" +
                    "         \"nID\":1\n" +
                    "      },\n" +
                    "      \"nID\":1\n" +
                    "   },\n" +
                    "   {  \n" +
                    "      \"sName\":\"Vacation\",\n" +
                    "      \"sNote\":\"В отпуске\",\n" +
                    "      \"oSubjectType\":{  \n" +
                    "         \"sName\":\"Organ\",\n" +
                    "         \"sDescription\":\"Орган\",\n" +
                    "         \"nID\":2\n" +
                    "      },\n" +
                    "      \"nID\":2\n" +
                    "   }\n" +
                    "]")
    @RequestMapping(value = "/getSubjectStatus", method = RequestMethod.GET)
    @ResponseBody
    public List<SubjectStatus> getSubjectStatus(
            @ApiParam("Тип сабджекта") @RequestParam(required = false) String sName_SubjectType
    ) {
        return oSubjectStatusService.getSubjectStatus(sName_SubjectType);
    }
}
