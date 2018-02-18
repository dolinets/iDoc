package org.igov.service.controller.launch;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.igov.service.business.launch.LaunchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Api(tags = {"LaunchController", "Launch"})
@RequestMapping(value = "/launch")
public class LaunchController {

    private static final Logger LOG = LoggerFactory.getLogger(LaunchController.class);

    @Autowired private LaunchService oLaunchService;

    @ApiOperation(value = "Запустить обработку методов", httpMethod = "GET", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/launch/processLaunchers")
    @ApiResponse(code = 200, message = "")
    @RequestMapping(value = "/processLaunchers", method = RequestMethod.GET)
    @ResponseBody
    public void processLaunchers(
            @ApiParam("Количество попыток") @RequestParam(required = false) Integer nTry,
            @ApiParam("Ид сервера") @RequestParam(required = false) Long nID_Server,
            @ApiParam("Дата редактирования от") @RequestParam(required = false) String sDateFrom,
            @ApiParam("Дата редактирования до") @RequestParam(required = false) String sDateTo
    ) throws JsonProcessingException {
        oLaunchService.processLaunchers(nTry, nID_Server, sDateFrom, sDateTo);
    }
}
