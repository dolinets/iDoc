package org.igov.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.activiti.engine.HistoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.task.Task;
import org.igov.model.action.vo.DocumentSubmitedUnsignedVO;
import org.igov.model.document.Document;
import org.igov.model.document.DocumentStep;
import org.igov.model.document.DocumentStepDao;
import org.igov.model.document.DocumentStepSubjectRight;
import org.igov.service.business.action.task.core.ActionTaskService;
import org.igov.service.business.action.task.form.TaskForm;
import org.igov.service.business.action.task.systemtask.DeleteProccess;
import org.igov.service.business.document.DocumentService;
import org.igov.service.business.document.DocumentStepService;
import org.igov.service.business.process.processLink.ProcessLinkService;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Api(tags = {"DocumentCommonController -- Проверки наложености ЭЦП по строкам-подписантам"})
@RequestMapping(value = "/common/document")

public class DocumentCommonController {

    @Autowired private DocumentStepService documentStepService;
    @Autowired private DocumentStepDao oDocumentStepDao;
    @Autowired private DeleteProccess deleteProccess;
    @Autowired private DocumentStepService oDocumentStepService;
    @Autowired private TaskForm oTaskForm;
    @Autowired private ActionTaskService oActionTaskService;
    @Autowired private HistoryService oHistoryService;
    @Autowired private ProcessLinkService oProcessLinkService;
    @Autowired private DocumentService oDocumentService;
    @Autowired private TaskService oTaskService;


    private static final Logger LOG = LoggerFactory.getLogger(DocumentCommonController.class);

    @ApiOperation(value = "проверка все ли подписали документ")
    @RequestMapping(value = "/isDocumentStepSubmitedAll", method = RequestMethod.GET)
    @Transactional
    public @ResponseBody
    Map<String, Object> isDocumentStepSubmitedAll(
            @ApiParam(value = "ИД процесс-активити", required = true) @RequestParam(required = true, value = "nID_Process") String nID_Process,
            @ApiParam(value = "Ключ шага документа", required = true) @RequestParam(required = true, value = "sKey_Step") String sKey_Step,
            @ApiParam(value = "Проверять подпись ЭЦП", required = true) @RequestParam(required = true, value = "bNeedECPCheck") boolean bNeedECPCheck,
            HttpServletResponse httpResponse) throws Exception {

        LOG.info("snID_Process_Activiti: " + nID_Process);
        LOG.info("bNeedECPCheck: " + bNeedECPCheck);
        LOG.info("sKey_Step: " + sKey_Step);
        return documentStepService.isDocumentStepSubmitedAll(nID_Process, sKey_Step, bNeedECPCheck);

    }

    @ApiOperation(value = "Клонирование подписанта-субьекта документа")
    @RequestMapping(value = "/cloneDocumentStepSubject", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    //@Transactional
    public @ResponseBody
    String cloneDocumentStepSubject(
            @ApiParam(value = "ИД процесс-активити", required = false) @RequestParam(required = false, value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "группа", required = false) @RequestParam(required = false, value = "sKeyGroupPostfix") String sKey_GroupPostfix,
            @ApiParam(value = "новая группа", required = false) @RequestParam(required = false, value = "sKeyGroupPostfix_New") String sKey_GroupPostfix_New,
            @ApiParam(value = "степ документа", required = false) @RequestParam(required = false, value = "sID_Step") String sID_Step) throws Exception {

        LOG.info("snID_Process_Activiti: {}", snID_Process_Activiti);
        LOG.info("sKey_GroupPostfix: {}", sKey_GroupPostfix);
        LOG.info("sKey_GroupPostfix_New: {}", sKey_GroupPostfix_New);

        List<DocumentStepSubjectRight> aDocumentStepSubjectRight = documentStepService.cloneDocumentStepSubject(snID_Process_Activiti, sKey_GroupPostfix, sKey_GroupPostfix_New, sID_Step);

        LOG.info("oDocumentStepSubjectRight in cloneDocumentStepSubject is {}", aDocumentStepSubjectRight);

        if (aDocumentStepSubjectRight != null) {
            return JSONValue.toJSONString(aDocumentStepSubjectRight);
        }

        return "DocumentStepSubjectRight is null";

    }

    @ApiOperation(value = "Синхронизация сабмитеров на степе по полю ")
    @RequestMapping(value = "/syncDocumentSubmitedsByField", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    //@Transactional
    public @ResponseBody
    String syncDocumentSubmitedsByField(
            @ApiParam(value = "ИД процесс-активити", required = true) @RequestParam(required = true, value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "группа - эталон/дэфолтная", required = true) @RequestParam(required = true, value = "sKey_Group_Default") String sKey_Group_Default,
            @ApiParam(value = "строка-ИД поля, в рамках значения/ний которого(логинов) пройдет синхронизация", required = true) @RequestParam(required = true, value = "sID_Field") String sID_Field,
            @ApiParam(value = "строка-ИД таблицы, в рамках значения/ний которого(логинов) пройдет синхронизация", required = false) @RequestParam(required = false, value = "sID_FieldTable") String sID_FieldTable,
            @ApiParam(value = "степ документа", required = true) @RequestParam(required = true, value = "sKey_Step") String sKey_Step,
            @ApiParam(value = "перезаписать при совпадении", required = true) @RequestParam(required = true, value = "bReClone") boolean bReClone
    ) throws Exception {

        List<DocumentStepSubjectRight> aDocumentStepSubjectRight = documentStepService
                .syncDocumentSubmitedsByField(snID_Process_Activiti, sKey_Group_Default, sID_Field,
                        sID_FieldTable, sKey_Step, bReClone);
        LOG.info("aDocumentStepSubjectRight is {}", aDocumentStepSubjectRight);

        if (aDocumentStepSubjectRight != null) {
            return JSONValue.toJSONString(aDocumentStepSubjectRight);
        }

        return "DocumentStepSubjectRight is null";
    }

    @ApiOperation(value = "Отмена подписи на степе документа")
    @RequestMapping(value = "/cancelDocumentSubmit", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    //@Transactional
    public @ResponseBody
    Map<String, Boolean> cancelDocumentSubmit(
            @ApiParam(value = "ИД процесс-активити", required = true) @RequestParam(required = true, value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "группа", required = true) @RequestParam(required = true, value = "sKey_Group") String sKey_Group,
            @ApiParam(value = "степ документа", required = true) @RequestParam(required = true, value = "sKey_Step") String sKey_Step,
            @ApiParam(value = "ид таски", required = true) @RequestParam(required = true, value = "nID_Task") String nID_Task
    ) throws Exception {

        LOG.info("snID_Process_Activiti={}", snID_Process_Activiti);
        LOG.info("sKey_Group={}", sKey_Group);
        LOG.info("sKey_Step={}", sKey_Step);
        
        oActionTaskService.validateDocumentStep(snID_Process_Activiti, sKey_Step);
        
        Boolean bCanceled = documentStepService.cancelDocumentSubmit(snID_Process_Activiti, sKey_Step, sKey_Group);
        LOG.info("bCanceled={}", bCanceled);
        Map<String, Boolean> m = new HashMap();
        m.put("bCanceled", bCanceled);
        return m;
    }

    @ApiOperation(value = "Удаление подписанта документа")
    @RequestMapping(value = "/removeDocumentStepSubject", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    //@Transactional
    public @ResponseBody
    Map<String, Object> removeDocumentStepSubject(
            @ApiParam(value = "ИД процесс-активити", required = true) @RequestParam String snID_Process_Activiti,
            @ApiParam(value = "группа", required = true) @RequestParam String sKey_Group,
            @ApiParam(value = "группа того, кто выполняет удаление") @RequestParam(required = false) String sKey_GroupAuthor,
            @ApiParam(value = "степ документа", required = true) @RequestParam String sKey_Step,
            @ApiParam(value = "после удаления: сабмитить  или нет") @RequestParam(required = false, defaultValue = "false") Boolean bSubmit
    ) throws Exception {
        oActionTaskService.validateDocumentStep(snID_Process_Activiti, sKey_Step);
        Map<String, Object> mResult = new HashMap<>();
        documentStepService.removeDocumentStepSubject(snID_Process_Activiti, sKey_Step, sKey_Group, sKey_GroupAuthor);
        if (bSubmit) {
            List<Task> aoTask = oTaskService.createTaskQuery().processInstanceId(snID_Process_Activiti).active().list();
            if (!aoTask.isEmpty()) {
                oTaskService.complete(aoTask.get(0).getId());
            }
        }
        //ищем активную таску после комплита
        String snID_Task = null;
        List<Task> aoTask_Active = oTaskService.createTaskQuery().processInstanceId(snID_Process_Activiti).active().list();
        if (!aoTask_Active.isEmpty()) {
            snID_Task = aoTask_Active.get(0).getId();
        }
        //если добавляли на просмотр из истории
        if (snID_Task == null) {
            List<HistoricTaskInstance> aHistoricTaskInstance = oHistoryService.createHistoricTaskInstanceQuery()
                    .processInstanceId(snID_Process_Activiti)
                    .orderByHistoricTaskInstanceEndTime()
                    .desc()
                    .list();
            snID_Task = aHistoricTaskInstance.get(0).getId();
        }
        mResult.put("snID_Task", snID_Task);
        oProcessLinkService.syncProcessLinksByLogin(snID_Process_Activiti, sKey_Group, sKey_GroupAuthor);

        return mResult;
    }

    @ApiOperation(value = "Добавить согласующего")
    @RequestMapping(value = "/addVisor", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public @ResponseBody
    List<Map<String, Object>> addVisor(
            @ApiParam(value = "ИД процесс-активити", required = true) @RequestParam(required = true, value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "группа", required = true) @RequestParam(required = true, value = "sKey_Group") String sKey_Group,
            @ApiParam(value = "группа делегирования", required = true) @RequestParam(required = true, value = "sKey_Group_Delegate") String sKey_Group_Delegate,
            @ApiParam(value = "степ документа", required = true) @RequestParam(required = true, value = "sKey_Step") String sKey_Step,
            @ApiParam(value = "ид таски", required = true) @RequestParam(required = true, value = "nID_Task") String nID_Task,
            @ApiParam(value = "bHistory", required = false) @RequestParam(value = "bHistory", required = false, defaultValue = "false") Boolean bHistory            
    ) throws Exception {
        LOG.info("addVisor started");
        LOG.info("snID_Process_Activiti={}", snID_Process_Activiti);
        LOG.info("sKey_Group={}", sKey_Group);
        LOG.info("sKey_Group_Delegate={}", sKey_Group_Delegate);
        LOG.info("sKey_Step={}", sKey_Step);
        
        oActionTaskService.validateDocumentStep(snID_Process_Activiti, sKey_Step);

        List<DocumentStepSubjectRight> aDocumentStepSubjectRight_Current = documentStepService
                .delegateDocumentStepSubject(snID_Process_Activiti, sKey_Step, sKey_Group, sKey_Group_Delegate, "AddVisor");

        LOG.info("aDocumentStepSubjectRight_Current={}", aDocumentStepSubjectRight_Current);

        return oDocumentStepService.getDocumentStepLogins(String.valueOf(snID_Process_Activiti), bHistory);
    }
    
    @ApiOperation(value = "Добавить согласующего")
    @RequestMapping(value = "/addViewer", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public @ResponseBody
    List<Map<String, Object>> addViewer(
            @ApiParam(value = "ИД процесс-активити", required = true) @RequestParam(required = true, value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "группа", required = true) @RequestParam(required = true, value = "sKey_Group") String sKey_Group,
            @ApiParam(value = "группа делегирования", required = true) @RequestParam(required = true, value = "sKey_Group_Delegate") String sKey_Group_Delegate,
            @ApiParam(value = "степ документа", required = false) @RequestParam(required = false, value = "sKey_Step") String sKey_Step,
            @ApiParam(value = "ид таски", required = true) @RequestParam(required = true, value = "nID_Task") String nID_Task,
            @ApiParam(value = "bHistory", required = false) @RequestParam(value = "bHistory", required = false, defaultValue = "false") Boolean bHistory
    ) throws Exception {
        LOG.info("addViewer started");
        LOG.info("snID_Process_Activiti={}", snID_Process_Activiti);
        LOG.info("sKey_Group={}", sKey_Group);
        LOG.info("sKey_Group_Delegate={}", sKey_Group_Delegate);
        LOG.info("sKey_Step={}", sKey_Step);
        
        if(sKey_Step == null){
            List<HistoricVariableInstance> aHistoricVariableInstance = oHistoryService.createHistoricVariableInstanceQuery().processInstanceId(snID_Process_Activiti).list();
               
            for(HistoricVariableInstance oHistoricVariableInstance : aHistoricVariableInstance){
                if(oHistoricVariableInstance.getVariableName().startsWith("sKey_Step")){
                    sKey_Step = (String)oHistoricVariableInstance.getValue();
                    LOG.info("sKey_Step is {}", sKey_Step);
                }
            }
        }
        
        List<DocumentStepSubjectRight> aDocumentStepSubjectRight_Current = documentStepService
                .delegateDocumentStepSubject(snID_Process_Activiti, sKey_Step, sKey_Group, sKey_Group_Delegate, "AddViewer");

        LOG.info("aDocumentStepSubjectRight_Current={}", aDocumentStepSubjectRight_Current);

        return oDocumentStepService.getDocumentStepLogins(String.valueOf(snID_Process_Activiti), bHistory);
    }

    @ApiOperation(value = "Добавить согласующего")
    @RequestMapping(value = "/addAcceptor", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public @ResponseBody
    List<Map<String, Object>> addAcceptor(
            @ApiParam(value = "ИД процесс-активити", required = true) @RequestParam(required = true, value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "группа", required = true) @RequestParam(required = true, value = "sKey_Group") String sKey_Group,
            @ApiParam(value = "группа делегирования", required = true) @RequestParam(required = true, value = "sKey_Group_Delegate") String sKey_Group_Delegate,
            @ApiParam(value = "степ документа", required = true) @RequestParam(required = true, value = "sKey_Step") String sKey_Step,
            @ApiParam(value = "ид таски", required = true) @RequestParam(required = true, value = "nID_Task") String nID_Task,
            @ApiParam(value = "bHistory", required = false) @RequestParam(value = "bHistory", required = false, defaultValue = "false") Boolean bHistory
    ) throws Exception {
        LOG.info("addAcceptor started");
        LOG.info("snID_Process_Activiti={}", snID_Process_Activiti);
        LOG.info("sKey_Group={}", sKey_Group);
        LOG.info("sKey_Group_Delegate={}", sKey_Group_Delegate);
        LOG.info("sKey_Step={}", sKey_Step);
        
        oActionTaskService.validateDocumentStep(snID_Process_Activiti, sKey_Step);
        
        List<DocumentStepSubjectRight> aDocumentStepSubjectRight_Current = documentStepService
                .delegateDocumentStepSubject(snID_Process_Activiti, sKey_Step, sKey_Group, sKey_Group_Delegate, "AddAcceptor");

        LOG.info("aDocumentStepSubjectRight_Current={}", aDocumentStepSubjectRight_Current);

        return oDocumentStepService.getDocumentStepLogins(String.valueOf(snID_Process_Activiti), bHistory);
    }

    @ApiOperation(value = "Делегировать подписанта документа")
    @RequestMapping(value = "/delegateDocumentStepSubject", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    //@Transactional
    public @ResponseBody
    List<DocumentStepSubjectRight> delegateDocumentStepSubject(
            @ApiParam(value = "ИД процесс-активити", required = true) @RequestParam(required = true, value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "группа", required = true) @RequestParam(required = true, value = "sKey_Group") String sKey_Group,
            @ApiParam(value = "группа делегирования", required = true) @RequestParam(required = true, value = "sKey_Group_Delegate") String sKey_Group_Delegate,
            @ApiParam(value = "степ документа", required = true) @RequestParam(required = true, value = "sKey_Step") String sKey_Step,
            @ApiParam(value = "ид таски", required = true) @RequestParam(required = true, value = "nID_Task") String nID_Task
    ) throws Exception {

        LOG.info("snID_Process_Activiti={}", snID_Process_Activiti);
        LOG.info("sKey_Group={}", sKey_Group);
        LOG.info("sKey_Group_Delegate={}", sKey_Group_Delegate);
        LOG.info("sKey_Step={}", sKey_Step);
        
        oActionTaskService.validateDocumentStep(snID_Process_Activiti, sKey_Step);
        
        List<DocumentStepSubjectRight> aDocumentStepSubjectRight_Current = documentStepService
                .delegateDocumentStepSubject(snID_Process_Activiti, sKey_Step, sKey_Group, sKey_Group_Delegate, "delegate");

        LOG.info("aDocumentStepSubjectRight_Current={}", aDocumentStepSubjectRight_Current);

        return aDocumentStepSubjectRight_Current;
    }

    
    @ApiOperation(value = "Пометить документ как экстренный")
    @RequestMapping(value = "/setDocumentUrgent", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public @ResponseBody void setDocumentUrgent(
            @ApiParam(value = "ИД процесс-активити", required = true) @RequestParam(required = true, value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "группа", required = true) @RequestParam(required = true, value = "sKey_Group_Editor") String sKey_Group_Editor,
            @ApiParam(value = "группа делегирования", required = false) @RequestParam(required = false, value = "sKey_Group_Urgent") String sKey_Group_Urgent,
            @ApiParam(value = "степ документа", required = false) @RequestParam(required = false, value = "sKey_Step") String sKey_Step,
            @ApiParam(value = "экстренность", required = false) @RequestParam(required = false, value = "bUrgent") Boolean bUrgent
    ) throws Exception {
        LOG.info("setDocumentUrgent started....");
        documentStepService.setDocumentUrgent(snID_Process_Activiti, sKey_Step, sKey_Group_Editor, sKey_Group_Urgent, bUrgent);
        oProcessLinkService.syncProcessLinks(snID_Process_Activiti, sKey_Group_Editor);
    }
    
    
    
    
    @ApiOperation(value = "Удаление степов и процесса")
    @RequestMapping(value = "/removeDocumentSteps", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public @ResponseBody
    void removeDocumentSteps(
            @ApiParam(value = "ИД процесс-активити", required = true) @RequestParam(required = true, value = "snID_Process_Activiti") String snID_Process_Activiti,
            @ApiParam(value = "Логин референта, кто вызвал сервис", required = false) @RequestParam(required = false, value = "sLogin") String sLoginReferent,
            @ApiParam(value = "Группа в которую входит референт", required = false) @RequestParam(required = false, value = "sLogin") String sLogin
    ) throws Exception {

        deleteProccess.closeProcessInstance(snID_Process_Activiti, sLoginReferent);
    }

    @ApiOperation(value = "Получение списка подписанных документов без ЕЦП")
    @RequestMapping(value = "/getDocumentSubmitedUnsigned", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    // @Transactional
    public @ResponseBody
    List<DocumentSubmitedUnsignedVO> getDocumentSubmitedUnsigned(
            @ApiParam(value = "Логин сотрудника", required = false) @RequestParam(required = false, value = "sLogin") String sLogin)
            throws Exception {

        LOG.info("sLogin: ", sLogin);

        List<DocumentSubmitedUnsignedVO> aDocumentSubmitedUnsignedVO = documentStepService.getDocumentSubmitedUnsigned(sLogin);

        //LOG.info("aDocumentSubmitedUnsignedVO in getDocumentSubmitedUnsigned is {}", aDocumentSubmitedUnsignedVO);

        /*if (aDocumentSubmitedUnsignedVO != null) {
                    return JSONValue.toJSONString(aDocumentSubmitedUnsignedVO);
            }

            return "aDocumentSubmitedUnsignedVO is null";*/
        return aDocumentSubmitedUnsignedVO;
    }

    @ApiOperation(value = "/getDocumentStepRights", notes = "##### Получение списка прав у логина по документу#####\n\n")
    @RequestMapping(value = "/getDocumentStepRights", method = RequestMethod.GET)
    public @ResponseBody
    Map<String, Object> getDocumentStepRights(@ApiParam(value = "sLogin", required = true) @RequestParam(value = "sLogin", required = true) String sLogin, //String
            @ApiParam(value = "nID_Process", required = true) @RequestParam(value = "nID_Process", required = true) String nID_Process) throws Exception {

        long startTime = System.nanoTime();
        Map<String, Object> res = oDocumentStepService.getDocumentStepRights(sLogin, nID_Process + "");
        long stopTime = System.nanoTime();
        LOG.info("getDocumentStepRights total time execution is: " + String.format("%,12d", (stopTime - startTime)));
        return res;
    }

    @ApiOperation(value = "/getDocumentStepRights_test", notes = "##### Получение списка прав у логина по документу#####\n\n")
    @RequestMapping(value = "/getDocumentStepRights_test", method = RequestMethod.GET)
    public @ResponseBody
    List<DocumentStepSubjectRight> getDocumentStepRights_test(@ApiParam(value = "sLogin", required = true) @RequestParam(value = "sLogin", required = true) String sLogin, //String
            @ApiParam(value = "nID_Process", required = true) @RequestParam(value = "snID_Process_Activiti", required = true) String snID_Process_Activiti,
            @ApiParam(value = "sKey_Step", required = true)
            @RequestParam(value = "sKey_Step", required = true) String sKey_Step) throws Exception {

        List<DocumentStep> aCheckDocumentStep = oDocumentStepDao.findAllBy("snID_Process_Activiti",
                snID_Process_Activiti);

            for(DocumentStep oDocumentStep_check : aCheckDocumentStep){
                if(oDocumentStep_check.getsKey_Step().equals(sKey_Step)){
                    return oDocumentStep_check.aDocumentStepSubjectRight();
                }
            }

            return new ArrayList<>();
    }
    
    @ApiOperation(value = "/getDocumentStepLogins", notes = "##### Получение списка прав у логина по документу#####\n\n")
    @RequestMapping(value = "/getDocumentStepLogins", method = RequestMethod.GET)
    public @ResponseBody
    List<Map<String, Object>> getDocumentStepLogins(@ApiParam(value = "nID_Process", required = true)
            @RequestParam(value = "nID_Process", required = true) String nID_Process,
            @ApiParam(value = "bHistory", required = false) 
            @RequestParam(value = "bHistory", required = false, defaultValue = "false") Boolean bHistory)
            throws Exception {//String
        
        return oDocumentStepService.getDocumentStepLogins(String.valueOf(nID_Process), bHistory);
    }

    @ApiOperation(value = "/getDocumentStep", notes = "##### Получение степ по документу#####\n\n")
    @RequestMapping(value = "/getDocumentStep", method = RequestMethod.GET)
    public @ResponseBody
    DocumentStep getDocumentStep(
            @ApiParam(value = "snID_Process_Activiti", required = true)
            @RequestParam(value = "snID_Process_Activiti", required = true) String snID_Process_Activiti,
            @ApiParam(value = "sKey_Step", required = true)
            @RequestParam(value = "sKey_Step", required = false) String sKey_Step
    ) throws Exception {
        LOG.info("---------------------Before step getting---------------------");
        DocumentStep oDocumentStep = oDocumentStepService.getDocumentStep(snID_Process_Activiti, sKey_Step);
        LOG.info("---------------------After step getting---------------------");
        return oDocumentStep;
    }

    @ApiOperation(value = "/getValuesFromTableField", notes = "Получение логинов по полю")
    @RequestMapping(value = "/getValuesFromTableField", method = RequestMethod.GET)
    @ResponseBody
    public List<String> getValuesFromTableField(
            @ApiParam(value = "snID_Process_Activiti", required = true)
            @RequestParam(value = "snID_Process_Activiti", required = true) String snID_Process_Activiti,
            @ApiParam(value = "sID_FieldTable", required = true)
            @RequestParam(value = "sID_FieldTable", required = true) String sID_FieldTable,
            @ApiParam(value = "sID_FieldFromTable", required = false)
            @RequestParam(value = "sID_FieldFromTable", required = false) String sID_FieldFromTable
    ) throws Exception {
        return oTaskForm.getValuesFromTableField(snID_Process_Activiti, sID_FieldTable, sID_FieldFromTable);
    }
    
    @ApiOperation(value = "/common/document/isLoginConteinsOnDocumentSteps", notes = "Проверка на содержание логина на других степах с заданным правом подписи")
    @RequestMapping(value = "/isLoginConteinsOnDocumentSteps", method = RequestMethod.GET)
    @ResponseBody
    public Boolean isLoginConteinsOnDocumentSteps(
            @ApiParam(value = "snID_Process_Activiti", required = true)
            @RequestParam(value = "snID_Process_Activiti", required = true) String snID_Process_Activiti,
            @ApiParam(value = "sLogin", required = true)
            @RequestParam(value = "sLogin", required = true) String sLogin,
            @ApiParam(value = "bWrite", required = false)
            @RequestParam(value = "bWrite", required = false) Boolean bWrite
    ) throws Exception {
        
        return documentStepService.isLoginConteinsOnDocumentSteps(snID_Process_Activiti, sLogin, bWrite);
    }

    @ApiOperation(value = "Получение документов по ЕДРПОУ", response = Document.class)
    @RequestMapping(value = "/getListDocument", method = RequestMethod.GET)
    @ResponseBody
    public List<Document> getListDocument(
            @ApiParam(required = true, value = "ЕДРПОУ") @RequestParam String sOKPO) {
        return oDocumentService.getListDocuments(sOKPO);
    }
    
    @ApiOperation("Отправка электронного письма всем владельцам прав на указанный шаг документа")
    @RequestMapping(value = "/notifyAllDocumentStepOwnersViaEmail", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void notifyAllDocumentStepOwnersViaEmail(
            @ApiParam("ID процесса") @RequestParam String snID_Process_Activiti,
            @ApiParam("Шаг документа") @RequestParam String sKey_Step,
            @ApiParam("Заголовок имейла") @RequestParam String sHead,
            @ApiParam("Переменная процесса содержимого имейла") @RequestParam String sContentKey) {
        oDocumentStepService.notifyAllDocumentStepOwnersViaEmail(snID_Process_Activiti, sKey_Step, sHead, sContentKey);
    }
    
    @ApiOperation("Отправка электронного письма владельцу прав на указанный шаг документа")
    @RequestMapping(value = "/notifyDocumentStepOwnerViaEmail", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void notifyDocumentStepOwner(
            @ApiParam("ID процесса") @RequestParam String snID_Process_Activiti,
            @ApiParam("Шаг документа") @RequestParam String sKey_Step,
            @ApiParam("Владелец прав на шаг документа, которому отправить письмо") @RequestParam String sOwnerID,
            @ApiParam("Заголовок имейла") @RequestParam String sHead,
            @ApiParam("Переменная процесса содержимого имейла") @RequestParam String sContentKey) {
        oDocumentStepService.notifyDocumentStepOwnerViaEmail(snID_Process_Activiti, sKey_Step, sOwnerID, sHead, sContentKey);
    }

}
