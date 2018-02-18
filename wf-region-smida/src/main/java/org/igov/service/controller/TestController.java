/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.controller;

import io.swagger.annotations.ApiOperation;
import org.igov.model.process.processChat.ProcessChatResult;
import org.igov.service.business.reference.book.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author alex
 */
@RestController
@RequestMapping(value = "/test")
public class TestController {

    @Autowired
    private TestService oTestService;

    @ApiOperation(value = "/getProcessChat", notes = "#### getProcessChat - тестовій контроллер для проверки работоспособности модуля")
    @RequestMapping(value = "/getProcessChat", method = RequestMethod.GET)
    ProcessChatResult getProcessChat(@RequestParam(value = "nID_Process_Activiti") Long nID_Process_Activiti)
            throws Exception {
        return oTestService.getProcessChat(nID_Process_Activiti);
    }
    
    @ApiOperation(value = "/hello", notes = "#### hello - тестовій контроллер для проверки работоспособности")
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    String getGreeting(@RequestParam(value = "nID") Long nID)
            throws Exception {
        return "Hello all great!" + nID;
    }
    
}