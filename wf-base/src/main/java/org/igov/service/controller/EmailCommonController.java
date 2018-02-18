package org.igov.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.Date;
import javax.mail.Multipart;
import org.igov.service.business.email.EmailProcessSubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Oleksandr Belichenko
 */
@Controller
@Api(tags = {"EmailCommonController — Отправление сообщений"})
@RequestMapping(value = "/action/email")
public class EmailCommonController {

    private static final Logger LOG = LoggerFactory.getLogger(EmailProcessSubjectService.class);

    @Autowired
    private EmailProcessSubjectService oEmailProcessSubjectService;

    @ApiOperation(value = "Отправление сообщения исполнителю и контролирующему")
    @RequestMapping(value = "/sendEmail_createTask", method = RequestMethod.GET)
    @ResponseBody
    public void sendEmail_createTask(
            @ApiParam(value = "логин получателя", required = true)
            @RequestParam(value = "sLoginTo", required = true) String sLoginTo,            
            @ApiParam(value = "текст замечания", required = true)
            @RequestParam(value = "sBody", required = true) String sBody,
            @ApiParam(value = "список исполнителей", required = false)
            @RequestParam(value = "sExecutors", required = false) String sExecutors,            
            @ApiParam(value = "срок", required = true)
            @RequestParam(value = "dDate", required = true) Date dDate,
            @ApiParam(value = "номер", required = true)
            @RequestParam(value = "nID_Process_Activiti", required = true) Long nID_Process_Activiti)
            throws Exception {
        LOG.info("Welcome to sendEmail_createTask");
        oEmailProcessSubjectService.sendEmail_createTask(sLoginTo, sBody, sExecutors, dDate, nID_Process_Activiti);
    }

    @ApiOperation(value = "Отправление сообщения с заменчанием")
    @RequestMapping(value = "/sendEmail_comment", method = RequestMethod.GET)
    @ResponseBody
    public void sendEmail_comment(
            @ApiParam(value = "ид процесса", required = true) 
            @RequestParam(value = "nID_Process_Activiti") Long nID_Process_Activiti,
            @ApiParam(value = "sKeyGroup", required = true) 
            @RequestParam(value = "sKeyGroup", required = true) String sKeyGroup,
            @ApiParam(value = "sKeyGroup_Author", required = true) 
            @RequestParam(value = "sKeyGroup_Author", required = true) String sKeyGroup_Author,
            @ApiParam(value = "текст замечания", required = true)
            @RequestParam(value = "sBody", required = true) String sBody)
            throws Exception {
        LOG.info("Welcome to sendEmail_comment");
        oEmailProcessSubjectService.sendEmail_comment(nID_Process_Activiti, sKeyGroup, sKeyGroup_Author, sBody);
    }
}
