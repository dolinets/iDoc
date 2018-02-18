package org.igov.service.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.igov.model.process.ProcessSubject;
import org.igov.model.process.ProcessSubjectResult;
import org.igov.model.process.ProcessSubjectResultTree;
import org.igov.model.process.ProcessSubjectTask;
import org.igov.service.business.process.ProcessSubjectService;
import org.igov.service.business.process.ProcessSubjectTreeService;
import org.igov.service.business.process.ProcessSubjectTaskService;
import org.igov.io.db.kv.temp.exception.RecordInmemoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.List;
import java.util.Map;
import org.activiti.engine.IdentityService;
import org.igov.service.exception.CommonServiceException;

import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
@Api(tags = {"ProcessSubjectController — Иерархия процессов"})
@RequestMapping(value = "/subject/process")
public class ProcessSubjectCommonController {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessSubjectCommonController.class);

    @Autowired
    private ProcessSubjectService processSubjectService;
    
    @Autowired
    private ProcessSubjectTreeService processSubjectTreeService;
    
    @Autowired
    ProcessSubjectTaskService oProcessSubjectTaskService;
    
    @Autowired
    private IdentityService identityService;

    @ApiOperation(value = "Получение иерархии процессов", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/process/getProcessSubject?snID_Process_Activiti=MJU_Dnipro&nDeepLevel=1 \n")
    @RequestMapping(value = "/getProcessSubject", method = RequestMethod.GET)
    @ResponseBody
    public ProcessSubjectResult getProcessSubject(@ApiParam(value = "ид процесса", required = true) @RequestParam(value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "глубина выборки", required = false) @RequestParam(value = "nDeepLevel", required = false) Long nDeepLevel,
            @ApiParam(value = "текст поиска (искать в ФИО, по наличию вхождения текста в ФИО)", required = false) @RequestParam(value = "sFind", required = false) String sFind)
            throws Exception {
        ProcessSubjectResult processSubjectResult = null;
        try {
            processSubjectResult = processSubjectService.getCatalogProcessSubject(snID_Process_Activiti, nDeepLevel, sFind);

        } catch (Exception e) {
            LOG.error("FAIL: ", e);
        }
        return processSubjectResult;
    }
    
    @ApiOperation(value = "Получение иерархии процессов", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/process/getProcessSubjectTree?snID_Process_Activiti=MJU_Dnipro&nDeepLevel=1 \n")
    @RequestMapping(value = "/getProcessSubjectTree", method = RequestMethod.GET)
    @ResponseBody
    public ProcessSubjectResultTree getProcessSubjectTree(@ApiParam(value = "ид процесса", required = true) @RequestParam(value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "глубина выборки", required = false) @RequestParam(value = "nDeepLevel", required = false) Long nDeepLevel,
            @ApiParam(value = "текст поиска (искать в ФИО, по наличию вхождения текста в ФИО)", required = false) @RequestParam(value = "sFind", required = false) String sFind,
            @ApiParam(value = "Флаг отображения рутового элемента для всей иерархии (true-отоборажаем, false-нет, по умолчанию Y)", required = false) @RequestParam(value = "bIncludeRoot", required = false) Boolean bIncludeRoot,
            @ApiParam(value = "Ширина выборки", required = false) @RequestParam(value = "nDeepLevelWidth", required = false) Long nDeepLevelWidth)
            throws Exception {
    	ProcessSubjectResultTree processSubjectResultTree = null;
        try {
        	processSubjectResultTree = processSubjectTreeService.getCatalogProcessSubjectTree(snID_Process_Activiti, nDeepLevel, sFind,bIncludeRoot,nDeepLevelWidth);

        } catch (Exception e) {
            LOG.error("FAIL: ", e);
        }
        return processSubjectResultTree;
    }

    @ApiOperation(value = "Сохранить процесс", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/process/setProcessSubject?snID_Process_Activiti=MJU_Dnipro&sLogin=1&sDatePlan=19-11-2016&nOrder=1 \n")
    @RequestMapping(value = "/setProcessSubject", method = RequestMethod.GET)
    @ResponseBody
    public ProcessSubject setProcessSubject(@ApiParam(value = "ид процесса", required = true) @RequestParam(value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "sLogin", required = false) @RequestParam(value = "sLogin", required = false) String sLogin,
            @ApiParam(value = "sDatePlan", required = false) @RequestParam(value = "sDatePlan", required = false) String sDatePlan,
            @ApiParam(value = "nOrder", required = false) @RequestParam(value = "nOrder", required = false) Long nOrder)
            throws Exception {
        ProcessSubject processSubject = null;
        try {
            processSubject = processSubjectService.setProcessSubject(snID_Process_Activiti, sLogin, sDatePlan, nOrder);

        } catch (Exception e) {
            LOG.error("FAIL: ", e);
        }
        return processSubject;
    }

    @ApiOperation(value = "Задать логин", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/process/setProcessSubjectLogin?snID_Process_Activiti=MJU_Dnipro&sLogin=1 \n")
    @RequestMapping(value = "/setProcessSubjectLogin", method = RequestMethod.GET)
    @ResponseBody
    public ProcessSubject setProcessSubjectLogin(@ApiParam(value = "ид процесса", required = true) @RequestParam(value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "sLogin", required = false) @RequestParam(value = "sLogin", required = false) String sLogin)
            throws Exception {
        ProcessSubject processSubjectResult = null;
        try {
            processSubjectResult = processSubjectService.setProcessSubjectLogin(snID_Process_Activiti, sLogin);

        } catch (Exception e) {
            LOG.error("FAIL: ", e);
        }
        return processSubjectResult;
    }

    @ApiOperation(value = "Задать номер заявки", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/process/setProcessSubjectOrder?snID_Process_Activiti=MJU_Dnipro&nOrder=1 \n")
    @RequestMapping(value = "/setProcessSubjectOrder", method = RequestMethod.GET)
    @ResponseBody
    public ProcessSubject setProcessSubjectOrder(@ApiParam(value = "ид процесса", required = true) @RequestParam(value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "nOrder", required = false) @RequestParam(value = "nOrder", required = false) Long nOrder)
            throws Exception {
        ProcessSubject processSubjectResult = null;
        try {
            processSubjectResult = processSubjectService.setProcessSubjectOrder(snID_Process_Activiti, nOrder);

        } catch (Exception e) {
            LOG.error("FAIL: ", e);
        }
        return processSubjectResult;
    }
    /*
    @ApiOperation(value = "Задать статус процесса", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/process/setProcessSubjectStatus?snID_Process_Activiti=MJU_Dnipro&nID_ProcessSubjectStatus=1 \n")
    @RequestMapping(value = "/setProcessSubjectStatus", method = RequestMethod.GET)
    @ResponseBody
    public ProcessSubject setProcessSubjectStatus(@ApiParam(value = "ид процесса", required = true) @RequestParam(value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "nID_ProcessSubjectStatus", required = false) @RequestParam(value = "sID_ProcessSubjectStatus", required = false) String sID_ProcessSubjectStatus)
            throws Exception {
        ProcessSubject processSubjectResult = null;
        try {
            processSubjectResult = processSubjectService.setProcessSubjectStatus(snID_Process_Activiti, sID_ProcessSubjectStatus);

        } catch (Exception e) {
            LOG.error("FAIL: ", e);
        }
        return processSubjectResult;
    }*/

    @ApiOperation(value = "Сохранить процесс", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/process/setProcessSubjectDatePlan?snID_Process_Activiti=MJU_Dnipro&sDatePlan=2016-11-19 \n")
    @RequestMapping(value = "/setProcessSubjectDatePlan", method = RequestMethod.GET)
    @ResponseBody
    public ProcessSubject setProcessSubjectDatePlan(@ApiParam(value = "ид процесса", required = true) @RequestParam(value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "sDatePlan", required = false) @RequestParam(value = "sDatePlan", required = false) String sDatePlan)
            throws Exception {
        ProcessSubject processSubjectResult = null;
        try {
            processSubjectResult = processSubjectService.setProcessSubjectDatePlan(snID_Process_Activiti, sDatePlan);

        } catch (Exception e) {
            LOG.error("FAIL: ", e);
        }
        return processSubjectResult;
    }
    
    @ApiOperation(value = "Вернуть массив логинов которые еще не являются участниками здания (из джейсона в редисе)", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/process/getProcessSubjectLoginsWithoutTask?snID_Process_Activiti=33267773&sFilterLoginRole=Executor \n")
    @RequestMapping(value = "/getProcessSubjectLoginsWithoutTask", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getProcessSubjectLoginsWithoutTask(@ApiParam(value = "ид процесса", required = true) @RequestParam(value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "фильтр ролей", required = false) @RequestParam(value = "sFilterLoginRole", required = false) String sFilterLoginRole) throws RecordInmemoryException, ParseException
    {
        return oProcessSubjectTaskService.getProcessSubjectLoginsWithoutTask(snID_Process_Activiti, sFilterLoginRole);
    }
    
    @ApiOperation(value = "Вернуть массив логинов-участников здания по ид-процесса-документа", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/process/getProcessSubjectByDocument?snID_Process_Activiti_Root=33267773&sFilterLoginRole=Executor \n")
    @RequestMapping(value = "/getProcessSubjectByDocument", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getProcessSubjectByDocument(@ApiParam(value = "ид процесса - документа", required = true) @RequestParam(value = "snID_Process_Activiti_Root") String snID_Process_Activiti_Root,
            @ApiParam(value = "фильтр ролей", required = false) @RequestParam(value = "sFilterLoginRole", required = false) String sFilterLoginRole) throws RecordInmemoryException, ParseException
    {
        return oProcessSubjectTaskService.getProcessSubjectByDocument(snID_Process_Activiti_Root, sFilterLoginRole);
    }
    
    @ApiOperation(value = "Вернуть объект ProcessSubjectTask", notes = "##### Пример:\n"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/process/getProcessSubjectTask?snID_Process_Activiti=33267773 \n")
    @RequestMapping(value = "/getProcessSubjectTask", method = RequestMethod.GET)
    @ResponseBody
    public List<ProcessSubjectTask> getProcessSubjectTask(@ApiParam(value = "ид процесса", required = true) 
            @RequestParam(value = "snID_Process_Activiti") String snID_Process_Activiti
    ) throws RecordInmemoryException, ParseException {   
       
        return oProcessSubjectTaskService.getProcessSubjectTask(snID_Process_Activiti);
    }
    
    @ApiOperation(value = "Задать статус процесса ", notes = "##### Пример:"
            + "{\n"
            + "  \"queryParams\": {\n"
            + "    \"sText\": \"lorem ipsum lorem ipsum lorem ipsum lorem ipsum\",\n"
            + "    \"sID_ProcessSubjectStatus\": \"testoviy\",\n"
            + "    \"snID_Task_Activiti\": \"111111111\",\n"
            + "    \"sLoginController\": \"login login\",\n"
            + "    \"sLoginExecutor\": null,\n"
            + "    \"snID_ProcessSubjectTask\": \"2222222\",\n"
            + "    \"sDatePlaneNew\": \"20-02-2012\"\n"
            + "  }\n"
            + "}")
    @RequestMapping(value = "/setProcessSubjectStatus", method = {RequestMethod.POST},
            produces = "application/json; charset=utf-8")
    @ResponseBody
    public ProcessSubject setProcessSubjectStatus(
                @ApiParam(value = "JSON c параметрами", required = false) @RequestBody(required = true) String sBody
    ) throws CommonServiceException {    

        JsonParser parser = new JsonParser();
        JsonObject jsonBody = parser.parse(sBody).getAsJsonObject();
        LOG.debug("jsonBody={}", jsonBody);
        JsonObject jsonQueryParams = jsonBody.get("queryParams").getAsJsonObject();

        String sID_ProcessSubjectStatus = jsonQueryParams.get("sID_ProcessSubjectStatus").getAsString();
        String snID_Task_Activiti = jsonQueryParams.get("snID_Task_Activiti").getAsString();
        
        String sLoginController = jsonQueryParams.get("sLoginController") == null
                ? null : jsonQueryParams.get("sLoginController").getAsString();
        
        String sLoginExecutor = jsonQueryParams.get("sLoginExecutor") == null
                ? null : jsonQueryParams.get("sLoginExecutor").getAsString();
        
        String sText = jsonQueryParams.get("sText") == null
                ? null : jsonQueryParams.get("sText").getAsString();
        
        String snID_ProcessSubjectTask = jsonQueryParams.get("snID_ProcessSubjectTask") == null
                ? null : jsonQueryParams.get("snID_ProcessSubjectTask").getAsString();
        
        String sDatePlaneNew = jsonQueryParams.get("sDatePlaneNew") == null
                ? null : jsonQueryParams.get("sDatePlaneNew").getAsString();
        
        LOG.info("setProcessSubjectStatus: sID_ProcessSubjectStatus={}, snID_Task_Activiti={}, sLoginController={},"
                + "sLoginExecutor={}, sText={}, snID_ProcessSubjectTask={}, sDatePlaneNew={}", sID_ProcessSubjectStatus,
                snID_Task_Activiti, sLoginController, sLoginExecutor, sText, snID_ProcessSubjectTask, sDatePlaneNew);

        return processSubjectService.setProcessSubjectStatus(sID_ProcessSubjectStatus, snID_Task_Activiti,
                sLoginController, sLoginExecutor, sText, sDatePlaneNew, snID_ProcessSubjectTask);
    }
    
    @ApiOperation(value = "Синхронизировать ProcessSubject", notes = "Пример вызова:"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/process/syncProcessSubject?snID_Process_Activiti=свое значение&snID_Task_Activiti=свое значение&sLogin=свое значение")
    @RequestMapping(value = "/syncProcessSubject", method = RequestMethod.GET)
    @ResponseBody
    public ProcessSubject syncProcessSubject(
            @ApiParam(value = "ид процесса", required = true) @RequestParam(value = "snID_Process_Activiti", required = true) String snID_Process_Activiti,
            @ApiParam(value = "ид таски", required = true) @RequestParam(value = "snID_Task_Activiti", required = true) String snID_Task_Activiti,
            @ApiParam(value = "логин", required = true) @RequestParam(value = "sLogin", required = true) String sLogin,
            @ApiParam(value = "роль логина (Executor, Controller)", required = true) @RequestParam(value = "sLoginRole", required = true) String sLoginRole
    ) {

        return processSubjectService.syncProcessSubject(snID_Process_Activiti, snID_Task_Activiti, sLogin, sLoginRole);
    }
        
    @ApiOperation(value = "старт процесса по мультитаскам", notes = "Пример вызова:"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/process/startProcess?snID_Process_Activiti_Root=свое значение"
            + "sLogin=свое значение")
    @RequestMapping(value = "/startProcess", method = RequestMethod.GET)
    @ResponseBody
    public Long startProcess(@ApiParam(value = "ид процесса документа", required = true) @RequestParam(value = "snID_Process_Activiti_Root") String snID_Process_Activiti_Root,
            @ApiParam(value = "логин", required = false) @RequestParam(value = "sLogin", required = false) String sLogin
    ) throws ParseException, CommonServiceException, Exception {
    
        return oProcessSubjectTaskService.startProcess(snID_Process_Activiti_Root);
    }
    
    @ApiOperation(value = "проверка закрытия задач контролирующим", notes = "Пример вызова:"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/process/isDocumentTaskFinishedAll?snID_Process_Activiti=свое значение")
    @RequestMapping(value = "/isDocumentTaskFinishedAll", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> isDocumentTaskFinishedAll(@ApiParam(value = "ид процесса", required = true) @RequestParam(value = "snID_Process_Activiti") String snID_Process_Activiti) throws ParseException, CommonServiceException {
    
        return oProcessSubjectTaskService.isDocumentTaskFinishedAll(snID_Process_Activiti);
    }
    
    @ApiOperation(value = "Удвлить запись из act_hi_identitylink", notes = "Пример вызова:"
            + "https://alpha.test.region.igov.org.ua/wf/service/subject/process/deleteIdentityValue?snID=свое значение")
    @RequestMapping(value = "/deleteIdentityValue", method = RequestMethod.GET)
    @ResponseBody
    public void deleteIdentityValue(@ApiParam(value = "ид записи в бд", required = true) @RequestParam(value = "snID") String snID) throws ParseException, CommonServiceException {
        identityService.createNativeGroupQuery().sql("DELETE FROM \"public\".\"act_hi_identitylink\" WHERE (\"id_\") = \'" + snID + "\'").singleResult();
    }   
}