package org.igov.service.controller.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.igov.model.server.Server;
import org.igov.service.business.server.ServerService;
import org.igov.service.exception.RecordNotFoundException;
import org.igov.util.JSON.JsonRestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@Controller
@Api(tags = "Server")
@RequestMapping(value = "/subject")
public class ServerController {

    private static final Logger LOG = LoggerFactory.getLogger(ServerController.class);

    @Autowired
    private ServerService oServerService;

    @ApiOperation(value = "Получение информации о сервере", response = Server.class, notes = "#####Получение информации о сервере #####\n\n"
            + "HTTP Context: https://alpha.test.region.igov.org.ua/wf/service/subject/getServer?nID=nID\n\n\n"
            + "возвращает json представление сущности Server, которая содержит информацию о сервере.\n\n"
            + "Примеры:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/getServer?nID=0\n\n"
            + "Ответ:\n"
            + "\n```json\n"
            + "{\n"
            + "    \"sID\": \"Common_Region\",\n"
            + "    \"sType\": \"Region\",\n"
            + "    \"sURL_Alpha\": \"https://test.region.igov.org.ua/wf\",\n"
            + "    \"sURL_Beta\": \"https://test-version.region.igov.org.ua/wf\",\n"
            + "    \"sURL_Omega\": \"https://master-version.region.igov.org.ua/wf\",\n"
            + "    \"sURL\": \"https://region.igov.org.ua/wf\",\n"
            + "    \"nID\": 0\n"
            + "}\n"
            + "\n```\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/getServer?nID=-1\n"
            + "Ответ:\n"
            + "HTTP Status: 500 (internal server error)\n"
            + "\n```json\n"
            + "{\n"
            + "    \"code\": \"BUSINESS_ERR\",\n"
            + "    \"message\": \"Record not found\"\n"
            + "}\n"
            + "\n```\n")
    @RequestMapping(value = "/getServer", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity getServer(@ApiParam(value = "nID сервера", required = true) @RequestParam Integer nID
    ) throws ExecutionException {
        return JsonRestUtils.toJsonResponse(oServerService.getServer(nID));
    }

    @ApiOperation(value = "Создание/редактирование записи Server", response = Server.class, notes = "#####Пример: #####\n\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/setServer?sID=Common_Region&sType=Region&sURL=https://alpha.test.region.igov.org.ua/wf")
    @RequestMapping(value = "/setServer", method = RequestMethod.GET)
    @ResponseBody
    public Server setServer(
            @ApiParam(value = "Название сервера", required = true) @RequestParam String sID,
            @ApiParam("Тип сервера (Region/Central)") @RequestParam(required = false) String sType,
            @ApiParam("sURL_Alpha") @RequestParam(required = false) String sURL_Alpha,
            @ApiParam("sURL_Beta") @RequestParam(required = false) String sURL_Beta,
            @ApiParam("sURL_Omega") @RequestParam(required = false) String sURL_Omega,
            @ApiParam("sURL") @RequestParam(required = false) String sURL
    ) throws RecordNotFoundException {
        return oServerService.setServer(sID, sType, sURL_Alpha, sURL_Beta, sURL_Omega, sURL);
    }
    
}
