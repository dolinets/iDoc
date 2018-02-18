package org.igov.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.igov.model.process.processChat.ProcessChatMessage;
import org.igov.model.process.processChat.ProcessChatResult;
import org.igov.service.business.document.DocumentStepService;
import org.igov.service.business.process.processChat.ProcessChatMessageService;
import org.igov.service.business.process.processChat.ProcessChatService;
import org.igov.util.JSON.JsonRestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Api(tags = {"ProcessChatController — Организация взаимосвязей чата"})
@RequestMapping(value = "/chat/process")
public class ProcessChatCommonController {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessSubjectCommonController.class);

    @Autowired
    private ProcessChatService processChatService;

    @Autowired
    private ProcessChatMessageService processChatMessageService;

    @Autowired
    private DocumentStepService oDocumentStepService;

    @ApiOperation(value = "Возврат иерархии чатов", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/chat/process/getProcessChat?nID_Process_Activiti=3805456 \n")
    @RequestMapping(value = "/getProcessChat", method = RequestMethod.GET)
    public ProcessChatResult getProcessChat(@ApiParam(value = "ид процесса", required = true) @RequestParam(value = "nID_Process_Activiti") Long nID_Process_Activiti)
            throws Exception {
        ProcessChatResult oProcessChatResult = null;
        try {
            oProcessChatResult = processChatService.findByProcess_Activiti(nID_Process_Activiti);

        } catch (Exception e) {
            LOG.error("FAIL: ", e);
        }
        return oProcessChatResult;
    }

    @ApiOperation(value = "Сохранить сообщение чата", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/chat/process/setProcessChatMessage?nID_Process_Activiti=3805456&sKeyGroup=1&sKeyGroup_Author=Author&sLoginReferent=Referent&sBody=test&nID_ProcessChatMessage_Parent=1000 \n")
    @RequestMapping(value = "/setProcessChatMessage", method = RequestMethod.POST, produces = "application/json;charset=UTF-8", consumes = "application/json;charset=UTF-8")
    public ProcessChatMessage setProcessChatMessage(@ApiParam(value = "ид процесса", required = true) @RequestParam(value = "nID_Process_Activiti") Long nID_Process_Activiti,
            @ApiParam(value = "sKeyGroup", required = true) @RequestParam(value = "sKeyGroup", required = true) String sKeyGroup,
            @ApiParam(value = "sLogin", required = true) @RequestParam(value = "sLogin", required = true) String sLogin,
            @ApiParam(value = "sLoginReferent", required = true) @RequestParam(value = "sLoginReferent", required = true) String sLoginReferent,
            @ApiParam(value = "nID_ProcessChatMessage_Parent", required = false) @RequestParam(value = "nID_ProcessChatMessage_Parent", required = false) Long nID_ProcessChatMessage_Parent,
            @ApiParam(value = "JSON-объект с параметрами: sBody - строка тела сообщения-коммента (required = true)", required = true) @RequestBody String sJsonBody)
            throws Exception {
        oDocumentStepService.validateStepRights(String.valueOf(nID_Process_Activiti), "_", sKeyGroup);
        String sBody = getsBody(sJsonBody);
        return processChatService.setProcessChat(nID_Process_Activiti, sKeyGroup,
                sLogin, sLoginReferent, sBody, nID_ProcessChatMessage_Parent);
    }

    @ApiOperation(value = "Редактировать сообщение чата", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/chat/process/updateProcessChatMessage?nID_Process_Activiti=3805456&sKeyGroup=1&sKeyGroup_Author=Author&sLoginReferent=Referent&sBody=test&nID_ProcessChatMessage=1002 \n")
    @RequestMapping(value = "/updateProcessChatMessage", method = RequestMethod.PUT, produces = "application/json;charset=UTF-8", consumes = "application/json;charset=UTF-8")
    public ProcessChatMessage updateProcessChatMessage(@ApiParam(value = "ид процесса", required = true) @RequestParam(value = "nID_Process_Activiti") Long nID_Process_Activiti,
            @ApiParam(value = "sKeyGroup", required = true) @RequestParam(value = "sKeyGroup", required = true) String sKeyGroup,
            @ApiParam(value = "sLogin", required = true) @RequestParam(value = "sLogin", required = true) String sLogin,
            @ApiParam(value = "sLoginReferent", required = true) @RequestParam(value = "sLoginReferent", required = true) String sLoginReferent,
            @ApiParam(value = "nID_ProcessChatMessage", required = true) @RequestParam(value = "nID_ProcessChatMessage", required = true) Long nID_ProcessChatMessage,
            @ApiParam(value = "JSON-объект с параметрами: sBody - строка тела сообщения-коммента (required = true)", required = true) @RequestBody String sJsonBody)            
            throws Exception {
        String sBody = getsBody(sJsonBody);
        ProcessChatMessage oProcessChatMessage = null;
        try {
            oProcessChatMessage = processChatMessageService.updateProcessChatMessage(nID_ProcessChatMessage, sLogin, sLoginReferent, sBody, nID_Process_Activiti, sKeyGroup);
        } catch (Exception e) {
            LOG.error("FAIL: ", e);
            throw e;
        }
        return oProcessChatMessage;
    }

    @ApiOperation(value = "Удалить сообщение", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/chat/process/deleteProcessChatMessage?nID_ProcessChatMessage=3805456&sKeyGroup_Author=Author \n")
    @RequestMapping(value = "/deleteProcessChatMessage", method = RequestMethod.DELETE)
    public void deleteProcessChatMessage(@ApiParam(value = "ид процесса", required = true) @RequestParam(value = "nID_ProcessChatMessage") Long nID_ProcessChatMessage,
            @ApiParam(value = "sLogin", required = true) @RequestParam(value = "sLogin", required = true) String sLogin)
            throws Exception {
        processChatMessageService.removeProcessChatMessage(nID_ProcessChatMessage, sLogin);
    }
    
    private String getsBody(String sJsonBody){
        Map<String, String> mJsonBody;
        String sBody = null;
        try {
            mJsonBody = JsonRestUtils.readObject(sJsonBody, Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parse JSON sJsonBody in request setTaskQuestions: " + e.getMessage());
        }
        if (mJsonBody != null) {
            if (mJsonBody.containsKey("sBody")) {
                sBody = (String) mJsonBody.get("sBody");
                LOG.info("sBody: ", sBody);
            }
        }
        return sBody;
    }
}
