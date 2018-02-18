package org.igov.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.igov.model.process.processLink.ProcessLink;
import org.igov.model.process.processLink.ProcessLinkDao;
import org.igov.model.process.processLink.ProcessLinkVO;
import org.igov.service.business.action.task.systemtask.DeleteProccess;
import org.igov.service.business.process.processLink.ProcessLinkService;
import org.igov.service.exception.CommonServiceException;
import org.igov.service.exception.RecordNotFoundException;
import org.igov.util.JSON.JsonRestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author idenysenko
 */
@Controller
@Api(tags = {"ProcessLinkCommonController — работа с внешними тасками"})
@RequestMapping(value = "/process/processlink")
public class ProcessLinkCommonController {
    
    private static final Logger LOG = LoggerFactory.getLogger(ProcessLinkCommonController.class);
    
    @Autowired private ProcessLinkService oProcessLinkService;
    @Autowired private ProcessLinkDao oProcessLinkDao;
    @Autowired private DeleteProccess oDeleteProcess;
    
    @ApiOperation(value = "Получение ProcessLink'ов", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/process/processlink/getProcessLinks?nID=1 \n")
    @RequestMapping(value = "/getProcessLinks", method = RequestMethod.GET)
    @ResponseBody
    public List<ProcessLink> getProcessLinks(
            @ApiParam(value = "персонализированная группа", required = true) @RequestParam(value = "sID_Group_Activiti", required = true) String sID_Group_Activiti,
            @ApiParam(value = "тип закладки", required = true) @RequestParam(value = "sType", required = true) String sType,
            @ApiParam(value = "тип под-закладки", required = true) @RequestParam(value = "sSubType", required = true) String sSubType
    ) {
        
        return oProcessLinkService.getProcessLinks(sID_Group_Activiti, sType, sSubType);
    }
    
    @ApiOperation(value = "Создание/редактирование ProcessLink", notes = "##### Пример:\n"
            + "{ \n"
            + "    \"nID_Server\": \"5\",\n"
            + "    \"sType\": \"Document\",\n"
            + "    \"sSubType\": \"OpenedUnassigned\",\n"
            + "    \"sID_Group_Activiti\": \"btsol\",\n"
            + "    \"sLogin\": \"btsol_011272SVL\",\n"
            + "    \"sTaskName\": \"task name\",\n"
            + "    \"sProcessName\": \"process name\",\n"
            + "    \"sProcessDateCreate\": \"2017-08-10\",\n"
            + "    \"sProcessDateModify\": \"2017-08-11\",\n"
            + "    \"sTaskDateCreate\": \"2017-08-12\",\n"
            + "    \"sTaskDateModify\": \"2017-08-13\",\n"
            + "    \"snID_Process_Activiti\": \"11111111\",\n"
            + "    \"snID_Task\": \"22222222\",\n"
            + "    \"nID_DocumentStepType\": \"3\"\n"
            + "}")
    @RequestMapping(value = "/setProcessLink", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ProcessLink setProcessLink(
            @ApiParam(value = "JSON c параметрами", required = false) @RequestBody(required = true) String sBody
    ) throws CommonServiceException, RecordNotFoundException {
      
        ProcessLinkVO oProcessLinkVO = JsonRestUtils.readObject(sBody, ProcessLinkVO.class);
        
        return oProcessLinkService.setProcessLink(oProcessLinkVO.getnID_Server(),
                oProcessLinkVO.getsType(),
                oProcessLinkVO.getsSubType(),
                oProcessLinkVO.getsID_Group_Activiti(),
                oProcessLinkVO.getsLogin(),
                oProcessLinkVO.getsTaskName(),
                oProcessLinkVO.getsProcessName(),
                oProcessLinkVO.getsProcessDateCreate(),
                oProcessLinkVO.getsProcessDateModify(),
                oProcessLinkVO.getsTaskDateCreate(),
                oProcessLinkVO.getsTaskDateModify(),
                oProcessLinkVO.getSnID_Process_Activiti(),
                oProcessLinkVO.getSnID_Task(), 
                oProcessLinkVO.getnID_DocumentStepType(),
                oProcessLinkVO.getbUrgent()
        );
    }
    
    @ApiOperation(value = "Удаление ProcessLink'ов", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/process/processlink/removeProcessLinks?snID_Process_Activiti=37527622 \n")
    @RequestMapping(value = "/removeProcessLinks", method = RequestMethod.GET)
    @ResponseBody
    public List<ProcessLink> removeProcessLinks(
            @ApiParam(value = "Ид процесса") @RequestParam(value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "Персонализированная группа") @RequestParam(value = "sID_Group_Activiti", required = false) String sID_Group_Activiti
    ) {
        LOG.info("removeProcessLinks started for snID_Process_Activiti={}, sID_Group_Activiti={}", snID_Process_Activiti, sID_Group_Activiti);
        List<ProcessLink> aoProcessLink_ForRemoving = new ArrayList<>();
        if (sID_Group_Activiti != null) {
            aoProcessLink_ForRemoving.addAll(oProcessLinkService.getProcessLinksByProcessAndGroup(snID_Process_Activiti, sID_Group_Activiti));
        } else {
            aoProcessLink_ForRemoving.addAll(oProcessLinkDao.findAllBy("snID_Process_Activiti", snID_Process_Activiti));
        }
        LOG.info("Founded {} ProcessLinks for deleting", aoProcessLink_ForRemoving.size());

        return oProcessLinkDao.delete(aoProcessLink_ForRemoving);
    }
    
    @ApiOperation(value = "Удаление всех ProcessLink'ов", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/process/processlink/removeAllProcessLinks \n")
    @RequestMapping(value = "/removeAllProcessLinks", method = RequestMethod.GET)
    @ResponseBody
    public List<ProcessLink> removeAllProcessLinks() {
        List<ProcessLink> aoProcessLink = oProcessLinkDao.findAll();

        return oProcessLinkDao.delete(aoProcessLink);
    }
    
    @ApiOperation(value = "Удаление всех Process'ов", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/process/processlink/removeAllProcess \n")
    @RequestMapping(value = "/removeAllProcess", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> removeAllProcess() throws Exception {
        return oDeleteProcess.removeAllProcess();
    }
    
    @ApiOperation(value = "Установить статус ProcessLink'ов", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/process/processlink/setProcessLinkStatus \n")
    @RequestMapping(value = "/setProcessLinkStatus", method = RequestMethod.GET)
    @ResponseBody
    public List<ProcessLink> setProcessLinkStatus(
            @ApiParam(value = "ид процесса", required = true) @RequestParam(value = "snID_Process_Activiti", required = true) String snID_Process_Activiti,
            @ApiParam(value = "статус", required = true) @RequestParam(value = "sStatus", required = true) String sStatus
    ) {
        LOG.info("setProcessLinkStatus started for snID_Process_Activiti={}", snID_Process_Activiti);
        List<ProcessLink> aoProcessLink = oProcessLinkDao.findAllBy("snID_Process_Activiti", snID_Process_Activiti);
        LOG.info("Founded {} ProcessLinks for status setting", aoProcessLink.size());
        aoProcessLink.forEach(oProcessLink -> oProcessLink.setsStatus(sStatus));
        
        oProcessLinkDao.saveOrUpdate(aoProcessLink);
        
        return aoProcessLink;
    }

}
