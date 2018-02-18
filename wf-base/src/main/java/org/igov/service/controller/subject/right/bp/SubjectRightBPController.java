package org.igov.service.controller.subject.right.bp;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.igov.service.business.subject.SubjectRightBPService;
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
@Api(description = "Управление правами на создание БП", tags = {"SubjectRightBP"})
@RequestMapping(value = "/subject/right")
public class SubjectRightBPController {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectRightBPController.class);

    @Autowired private SubjectRightBPService oSubjectRightBPService;

    @ApiOperation(
            value = "Создать права на создание БП. Можно добавлять как все критерии сразу так и комбинациями:\n"
                    + " ид БП - ид группы, ид БП - ид должности.",
            httpMethod = "GET",
            notes = "##### Пример:\n"
                    + "https://alpha.test.region.igov.org.ua/wf/service/subject/right/setBP?sID_BP=_doc_justice_22&sID_Group_Referent=justice_common\n"
                    + " - добавить по ид БП и группе\n"
                    + "https://alpha.test.region.igov.org.ua/wf/service/subject/right/setBP?sID_BP=_doc_justice_22&nID_SubjectHumanPosition=22\n"
                    + " - добавить по должности"
    )
    @ApiResponse(code = 200, message = "")
    @RequestMapping(value = "/setBP", method = RequestMethod.GET)
    @ResponseBody
    public void setBP(
            @ApiParam("Ид бизнес процесса") @RequestParam String sID_BP,
            @ApiParam("Группа") @RequestParam (required = false) String sID_Group_Referent,
            @ApiParam("Должность") @RequestParam (required = false) Long nID_SubjectHumanPosition
    ) {

        oSubjectRightBPService.setBP(sID_BP, sID_Group_Referent, nID_SubjectHumanPosition);
    }

    @ApiOperation(
            value = "Удалить права на создание БП. Можно добавлять как все критерии сразу так и комбинациями:\n"
                    + " ид БП - ид группы, ид БП - ид должности.",
            httpMethod = "GET",
            notes = "##### Пример:\n"
                    + "https://alpha.test.region.igov.org.ua/wf/service/subject/right/removeBP?sID_BP=_doc_justice_22&sID_Group_Referent=justice_common\n"
                    + " - удалить по ид БП и группе\n"
                    + "https://alpha.test.region.igov.org.ua/wf/service/subject/right/removeBP?sID_BP=_doc_justice_22&nID_SubjectHumanPosition=22\n"
                    + " - удалить по должности"
    )
    @ApiResponse(code = 200, message = "")
    @RequestMapping(value = "/removeBP", method = RequestMethod.GET)
    @ResponseBody
    public void removeBP(
            @ApiParam("Ид бизнес процесса") @RequestParam String sID_BP,
            @ApiParam("Группа") @RequestParam (required = false) String sID_Group_Referent,
            @ApiParam("Должность") @RequestParam (required = false) Long nID_SubjectHumanPosition
    ) {

        oSubjectRightBPService.removeBP(sID_BP, sID_Group_Referent, nID_SubjectHumanPosition);
    }

    @ApiOperation(
            value = "Получить уникальный список бизнесс процессов",
            httpMethod = "GET",
            notes = "##### Пример:\n https://alpha.test.idoc.com.ua/wf/service/subject/right/getAllBPs"
    )
    @ApiResponse(
            code = 200,
            message = "[{\"sName\":\"Викопіювання з топографічного плану міста 1:500\",\"sID\":\"APU_734_Vuk500_Dnp\"},\n"
            + "....\n"
            + "{\"sName\":\"Видача/заміна/продовження паспорту прив’язки тимчасової споруди\",\"sID\":\"zovtyvodu_cnap_430\"}]"
    )
    @RequestMapping(value = "/getAllBPs", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, String>> getAllBp() {
        return oSubjectRightBPService.getAllBPs();
    }
}
