package org.igov.service.controller.action.event;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.igov.model.action.vo.HistoryEventVO;
import org.igov.service.business.action.event.ActionEventHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Api(description = "История документов/задач", tags = {"HistoryEvent"})
@RequestMapping(value = "/action/event")
public class HistoryEventController {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryEventController.class);

    @Autowired private ActionEventHistoryService oHistoryEventService;

    @ApiOperation(
            value = "Получение истории по документу/задаче",
            httpMethod = "GET",
            notes = "##### Пример:\n"
            + "https://alpha.test.idoc.com.ua/wf/service/action/event/getHistoryEvents?sID_Order=15-628359"
    )
    @ApiResponse(
            code = 200,
            message = "{\"aoHistoryEvent\":[  \n" +
                    "      {  \n" +
                    "         \"sMessage\":\"Смоктій О. Д.(Смоктій О. Д.). До документу №15-1050014 запрошено Біла Ю. Д. (Біла Ю. Д.) у ролі: переглядача.\",\n" +
                    "         \"sDate\":\"2018-01-15 14:00:03.038\",\n" +
                    "         \"oHistoryEvent_Service\":{  \n" +
                    "            \"sID\":null,\n" +
                    "            \"nID_Subject\":null,\n" +
                    "            \"sUserTaskName\":\"закрита\",\n" +
                    "            \"sDate\":\"2018-01-15 14:00:02.948\",\n" +
                    "            \"nID_Service\":null,\n" +
                    "            \"nID_Region\":null,\n" +
                    "            \"sID_UA\":null,\n" +
                    "            \"nRate\":null,\n" +
                    "            \"soData\":\"[]\",\n" +
                    "            \"sToken\":null,\n" +
                    "            \"sHead\":null,\n" +
                    "            \"sBody\":\"Смоктій О. Д.\",\n" +
                    "            \"nTimeMinutes\":null,\n" +
                    "            \"sID_Order\":\"15-1050014\",\n" +
                    "            \"nID_Server\":15,\n" +
                    "            \"nID_Proccess_Feedback\":null,\n" +
                    "            \"nID_Proccess_Escalation\":null,\n" +
                    "            \"sID_Rate_Indirectly\":null,\n" +
                    "            \"nID_StatusType\":0,\n" +
                    "            \"nID_ServiceData\":null,\n" +
                    "            \"sID_StatusType\":\"Created\",\n" +
                    "            \"sName_UA_StatusType\":\"Заявка подана\",\n" +
                    "            \"sDateCreate\":\"2018-01-13 13:27:02.536\",\n" +
                    "            \"sDateClose\":null,\n" +
                    "            \"sID_Public_SubjectOrganJoin\":null,\n" +
                    "            \"nID_Protected\":null,\n" +
                    "            \"nID\":2029964,\n" +
                    "            \"nID_Task\":105001\n" +
                    "         },\n" +
                    "         \"oDocument\":null,\n" +
                    "         \"sSubjectInfo\":null,\n" +
                    "         \"oSubject\":null,\n" +
                    "         \"nID\":2030509,\n" +
                    "         \"nID_Subject\":0,\n" +
                    "         \"nID_HistoryEventType\":34,\n" +
                    "         \"sEventName\":\"Смоктій О. Д.(Смоктій О. Д.). До документу №15-1050014 запрошено Біла Ю. Д. (Біла Ю. Д.) у ролі: переглядача.\"\n" +
                    "      }\n" +
                    "   ],\n" +
                    "   \"nTotalCount\":13,\n" +
                    "   \"nStart\":0,\n" +
                    "   \"nSize\":1\n" +
                    "}"
    )
    @RequestMapping(value = "/getHistoryEventsByProcess", method = RequestMethod.GET)
    @ResponseBody
    public HistoryEventVO getHistoryEvents(
            @ApiParam("Ид документа/задачи") @RequestParam(required = true) String sID_Order,
            @ApiParam("Номер с которого начать выборку") @RequestParam(required = true) Integer nStart,
            @ApiParam("Количество записей") @RequestParam(required = true) Integer nSize
    ) {

        return oHistoryEventService.getHistoryEvents(sID_Order, nSize, nStart);
    }

}
