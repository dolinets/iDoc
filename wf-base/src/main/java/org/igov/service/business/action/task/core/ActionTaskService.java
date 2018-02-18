/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.service.business.action.task.core;

import com.google.common.collect.Iterables;
import javassist.NotFoundException;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FormValue;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.*;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.*;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.igov.io.GeneralConfig;
import org.igov.model.access.vo.HistoryVariableVO;
import org.igov.model.action.event.HistoryEvent_Service_StatusType;
import org.igov.model.action.task.core.ProcessDTOCover;
import org.igov.model.action.task.core.TaskAssigneeCover;
import org.igov.model.action.task.core.entity.TaskAssigneeI;
import org.igov.model.action.vo.*;
import org.igov.model.document.DocumentStep;
import org.igov.model.document.DocumentStepDao;
import org.igov.model.document.DocumentStepSubjectRight;
import org.igov.model.document.DocumentStepType;
import org.igov.model.flow.FlowSlot;
import org.igov.model.flow.FlowSlotTicket;
import org.igov.model.flow.FlowSlotTicketDao;
import org.igov.model.process.ProcessSubject;
import org.igov.model.process.ProcessSubjectDao;
import org.igov.model.process.ProcessSubjectStatus;
import org.igov.model.process.processLink.ProcessLink;
import org.igov.model.process.processLink.ProcessLinkDao;
import org.igov.service.business.action.event.HistoryEventService;
import org.igov.service.business.action.task.form.QueueDataFormType;
import org.igov.service.business.document.DocumentStepService;
import org.igov.service.business.email.EmailService;
import org.igov.service.business.flow.FlowService;
import org.igov.service.business.process.ActionProcessServcie;
import org.igov.service.business.subject.ProcessInfoShortVO;
import org.igov.service.business.subject.SubjectRightBPService;
import org.igov.service.business.subject.SubjectRightBPVO;
import org.igov.service.controller.ExceptionCommonController;
import org.igov.service.exception.*;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.igov.util.ToolFS;
import org.igov.util.ToolJS;
import org.igov.util.ToolLuna;
import org.igov.util.cache.CachedInvocationBean;
import org.igov.util.cache.SerializableResponseEntity;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.MimeMultipart;
import javax.script.ScriptException;
import java.io.File;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.igov.io.fs.FileSystemData.getFiles_PatternPrint;
import static org.igov.service.business.flow.FlowService.bFlowOut;
import static org.igov.util.Tool.sO;

//import org.igov.service.business.access.BankIDConfig;

/**
 * @author bw
 */
//@Component
@Service
public class ActionTaskService implements ExceptionMessage {

    public static final String GET_ALL_TASK_FOR_USER_CACHE = "getAllTaskForUser";
    public static final String GET_ALL_TICKETS_CACHE = "getAllTickets";
    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss", Locale.ENGLISH);
    public static final String CANCEL_INFO_FIELD = "sCancelInfo";
    private static final int DEFAULT_REPORT_FIELD_SPLITTER = 59;
    private static final int MILLIS_IN_HOUR = 1000 * 60 * 60;

    private static final String THE_STATUS_OF_TASK_IS_CLOSED = "Closed";
    private static final String THE_STATUS_OF_TASK_IS_OPENED_UNASSIGNED = "OpenedUnassigned";
    private static final String THE_STATUS_OF_TASK_IS_CONTROL = "Control";
    private static final String THE_STATUS_OF_TASK_IS_CONTROL_FINISHED = "ControlFinished";
    private static final String THE_STATUS_OF_TASK_IS_OPENED_ASSIGNED = "OpenedAssigned";
    private static final String THE_STATUS_OF_TASK_IS_OPENED = "Opened";
    private static final String THE_STATUS_OF_TASK_IS_EXECUTION = "Execution";
    private static final String THE_STATUS_OF_TASK_IS_EXECUTION_FINISHED = "ExecutionFinished";
    private static final String THE_STATUS_OF_TASK_IS_DOCUMENTS = "Documents";
    private static final String THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_ASSIGNED = "DocumentOpenedAssigned";
    private static final String THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_UNASSIGNED_PROCESSED = "DocumentOpenedUnassignedProcessed";
    private static final String THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_UNASSIGNED_UNPROCESSED = "DocumentOpenedUnassignedUnprocessed";
    private static final String THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_UNASSIGNED_WITHOUTECP = "DocumentOpenedUnassignedWithoutECP";
    private static final String THE_STATUS_OF_TASK_IS_DOCUMENT_CLOSED = "DocumentClosed";
    private static final String THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_CREATOR = "OpenedCreatorDocument";
    private static final String THE_STATUS_OF_TASK_IS_DOCUMENT_ALL = "DocumentAll";
    private static final String THE_STATUS_OF_TASK_IS_TASK_ALL = "TaskAll";
    private static final String TASK = "Task";
    private static final String DOCUMENT = "Document";

    static final Comparator<FlowSlotTicket> FLOW_SLOT_TICKET_ORDER_CREATE_COMPARATOR = new Comparator<FlowSlotTicket>() {
        @Override
        public int compare(FlowSlotTicket e1, FlowSlotTicket e2) {
            return e2.getsDateStart().compareTo(e1.getsDateStart());
        }
    };

    private static final Logger LOG = LoggerFactory.getLogger(ActionTaskService.class);

    @Autowired
    private RuntimeService oRuntimeService;
    @Autowired
    private TaskService oTaskService;
    @Autowired
    private HistoryEventService oHistoryEventService;
    @Autowired
    private RepositoryService oRepositoryService;
    @Autowired
    private FormService oFormService;
    @Autowired
    private IdentityService oIdentityService;
    @Autowired
    private HistoryService oHistoryService;
    @Autowired
    private GeneralConfig oGeneralConfig;
    @Autowired
    private FlowSlotTicketDao oFlowSlotTicketDao;
    @Autowired
    private CachedInvocationBean oCachedInvocationBean;
    @Autowired
    private SubjectRightBPService oSubjectRightBPService;
    @Autowired
    private ProcessSubjectDao oProcessSubjectDao;
    @Autowired
    private EmailService oEmailService;
    @Autowired
    private DocumentStepService oDocumentStepService;
    @Autowired
    private ProcessLinkDao oProcessLinkDao;
    @Autowired
    private FlowService oFlowService;
    @Autowired
    DocumentStepDao oDocumentStepDao;
    @Autowired
    private ActionProcessServcie oActionProcessServcie;


    public static String parseEnumValue(String sEnumName) {
        LOG.info("(sEnumName={})", sEnumName);
        String res = StringUtils.defaultString(sEnumName);
        LOG.info("(sEnumName(2)={})", sEnumName);
        if (res.contains("|")) {
            String[] as = sEnumName.split("\\|");
            LOG.info("(as.length - 1={})", (as.length - 1));
            LOG.info("(as={})", (Object) as);
            res = as[as.length - 1];
        }
        return res;
    }

    public static String parseEnumProperty(FormProperty property) {
        Object oValues = property.getType().getInformation("values");
        if (oValues instanceof Map) {
            Map<String, String> mValue = (Map) oValues;
            LOG.info("(m={})", mValue);
            String sName = property.getValue();
            LOG.info("(sName={})", sName);
            String sValue = mValue.get(sName);
            LOG.info("(sValue={})", sValue);
            return parseEnumValue(sValue);
        } else {
            LOG.error("Cannot parse values for property - {}", property);
            return "";
        }
    }

    public static String parseEnumProperty(FormProperty property, String sName) {
        Object oValues = property.getType().getInformation("values");
        if (oValues instanceof Map) {
            Map<String, String> mValue = (Map) oValues;
            LOG.info("(m={})", mValue);
            LOG.info("(sName={})", sName);
            String sValue = mValue.get(sName);
            LOG.info("(sValue={})", sValue);
            return parseEnumValue(sValue);
        } else {
            LOG.error("Cannot parse values for property - {}", property);
            return "";
        }
    }
    
    public static String parseEnumProperty(org.activiti.bpmn.model.FormProperty property, String sName) {
        return property.getFormValues()
                .stream()
                .filter(formValue -> formValue.getId().equalsIgnoreCase(sName))
                .findAny()
                .map(FormValue::getName)
                .orElseGet(() -> {
                    LOG.warn("value not found for, propertyId: '{}'", sName);
                    return "";
                });
    }

    public static List<Map<String, String>> amFieldMessageQuestion(String saField, Boolean bNew) throws CommonServiceException {
        if (saField == null || "".equals(saField.trim()) || "[]".equals(saField.trim())) {
            throw new CommonServiceException(
                    ExceptionCommonController.BUSINESS_ERROR_CODE,
                    "Can't make task question with no fields! (saField=" + saField + ")",
                    HttpStatus.FORBIDDEN);
        }
        List<Map<String, String>> amReturn = new LinkedList();
        JSONObject oFields = new JSONObject("{ \"soData\":" + saField + "}");
        LOG.info("<<<<<<<<<<<<<<saField {}", saField);
        JSONArray aField = oFields.getJSONArray("soData");
        if (aField.length() == 0) {
            throw new CommonServiceException(
                    ExceptionCommonController.BUSINESS_ERROR_CODE,
                    "Can't make task question with no fields! (saField=" + saField + ")",
                    HttpStatus.FORBIDDEN);
        }
        LOG.info("saField {}", saField);
        for (int i = 0; i < aField.length(); i++) {
            JSONObject oField = aField.getJSONObject(i);
            Map<String, String> m = new HashMap();

            Object osID;
            if ((osID = oField.opt("sID")) == null) {
                if ((osID = oField.opt("id")) == null) {
                    throw new CommonServiceException(
                            ExceptionCommonController.BUSINESS_ERROR_CODE,
                            "Field sID and id of array is null",
                            HttpStatus.FORBIDDEN);
                }
            }
            m.put("sID", osID.toString());

            Object osName;
            if ((osName = oField.opt("sName")) == null) {
                osName = osID.toString();
            }
            m.put("sName", osName.toString());

            Object osValue;
            if ((osValue = oField.opt("sValue")) == null) {
                if ((osValue = oField.opt("value")) == null) {
                    throw new CommonServiceException(
                            ExceptionCommonController.BUSINESS_ERROR_CODE,
                            "Field sValue and value of array is null",
                            HttpStatus.FORBIDDEN);
                }
            }
            m.put("sValue", osValue.toString());

            if (bNew) {
                Object osValueNew;
                if ((osValueNew = oField.opt("sValueNew")) == null) {
                    throw new CommonServiceException(
                            ExceptionCommonController.BUSINESS_ERROR_CODE,
                            "Field sValueNew of array is null",
                            HttpStatus.FORBIDDEN);
                }
                m.put("sValueNew", osValueNew.toString());
            } else {
                Object osNotify;
                if ((osNotify = oField.opt("sNotify")) == null) {
                    throw new CommonServiceException(
                            ExceptionCommonController.BUSINESS_ERROR_CODE,
                            "Field sNotify of array is null",
                            HttpStatus.FORBIDDEN);
                }
                m.put("sNotify", osNotify.toString());
            }
            amReturn.add(m);
        }
        return amReturn;
    }

    public static String createTable_TaskProperties(List<Map<String, String>> amReturn, Boolean bNew, Boolean bNotification) {
        if (amReturn.isEmpty()) {
            return "";
        }
        StringBuilder osTable = new StringBuilder();
        osTable.append("<style>table.QuestionFields td { border-style: solid;}</style>");
        osTable.append("<table class=\"QuestionFields\">");
        if (bNotification) {
            osTable.append("<style>table"
                    + " { border-collapse: collapse;"
                    + " width: 100%;"
                    + " max-width: 800px;}"
                    + " table td {"
                    + " border: 1px solid #ddd;"
                    + " text-align:left;"
                    + " padding: 4px;"
                    + " height:40px;}"
                    + " table th {"
                    + " background: #65ABD0;"
                    + " vertical-align: middle;"
                    + " padding: 10px;"
                    + " width:200px;"
                    + " text-align:left;"
                    + " color:#fff;"
                    + " }"
                    + "</style>");
            osTable.append("<table>");
        } else {
            osTable.append("<style>table.QuestionFields td { border-style: solid;}</style>");
            osTable.append("<table class=\"QuestionFields\">");
        }
        osTable.append("<tr>");
        osTable.append("<td>").append("Поле").append("</td>");
        if (bNew) {
            osTable.append("<td>").append("Старе значення").append("</td>");
            osTable.append("<td>").append("Нове значення").append("</td>");
        } else {
            osTable.append("<td>").append("Значення").append("</td>");
        }
        osTable.append("<td>").append("Коментар").append("</td>");
        osTable.append("</tr>");
        for (Map<String, String> m : amReturn) {
            osTable.append("<tr>");
            osTable.append("<td>").append(m.get("sName")).append("</td>");
            osTable.append("<td>").append(m.get("sValue")).append("</td>");
            if (bNew) {
                osTable.append("<td>").append(m.get("sValueNew")).append("</td>");
                osTable.append("<td>").append(m.get("sValueNew").equals(m.get("sValue")) ? "(Не змінилось)" : "(Змінилось)").append("</td>");
            } else {
                osTable.append("<td>").append(m.get("sNotify")).append("</td>");
            }
            osTable.append("</tr>");
        }
        osTable.append("</table>");
        return osTable.toString();
    }

    public TaskQuery buildTaskQuery(String sLogin, String bAssigned) {
        TaskQuery taskQuery = oTaskService.createTaskQuery();
        if (bAssigned != null) {
            if (!Boolean.valueOf(bAssigned)) {
                taskQuery.taskUnassigned();
                if (sLogin != null && !sLogin.isEmpty()) {
                    taskQuery.taskCandidateUser(sLogin);
                }
            } else if (sLogin != null && !sLogin.isEmpty()) {
                taskQuery.taskAssignee(sLogin);
            }
        } else if (sLogin != null && !sLogin.isEmpty()) {
            taskQuery.taskCandidateOrAssigned(sLogin);
        }
        return taskQuery;
    }

    public void cancelTasksInternal(Long nID_Order, String sInfo) throws CommonServiceException, CRCInvalidException, RecordNotFoundException, TaskAlreadyUnboundException {
        LOG.info("cancelTasksInternal started...");
        String nID_Process = getOriginalProcessInstanceId(nID_Order);
        getTasksByProcessInstanceId(nID_Process);
        LOG.info("(nID_Order={},nID_Process={},sInfo={})", nID_Order, nID_Process, sInfo);
        HistoricProcessInstance processInstance = oHistoryService.createHistoricProcessInstanceQuery().processInstanceId(nID_Process).singleResult();
        FormData formData = oFormService.getStartFormData(processInstance.getProcessDefinitionId());
        List<String> asID_Field = AbstractModelTask.getListField_QueueDataFormType(formData);
        LOG.info("asID_Field: " + asID_Field);
        List<String> queueDataList = AbstractModelTask.getVariableValues(oRuntimeService, nID_Process, asID_Field);
        LOG.info("queueDataList: " + queueDataList);
        if (queueDataList.isEmpty()) {
            LOG.error(String.format("Queue data list for Process Instance [id = '%s'] not found", nID_Process));
            throw new RecordNotFoundException("\u041c\u0435\u0442\u0430\u0434\u0430\u043d\u043d\u044b\u0435 \u044d\u043b\u0435\u043a\u0442\u0440\u043e\u043d\u043d\u043e\u0439 \u043e\u0447\u0435\u0440\u0435\u0434\u0438 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u044b");
        }
        for (String queueData : queueDataList) {
            Map<String, Object> m = QueueDataFormType.parseQueueData(queueData);
            Long nID_FlowSlotTicket = null;
            try {
                nID_FlowSlotTicket = QueueDataFormType.get_nID_FlowSlotTicket(m);
            } catch (Exception ex) {
                LOG.info("QueueDataFormType throw an error: " + ex);
            }
            if (nID_FlowSlotTicket != null) {
                FlowSlotTicket flowSlotTicket = oFlowSlotTicketDao.findByIdExpected(nID_FlowSlotTicket);
                List<FlowSlot> aFlowSlot = flowSlotTicket.getaFlowSlot();
                DateTimeFormatter dtf = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
                for (FlowSlot oFlowSlot : aFlowSlot) {
                    LOG.info("oFlowSlot name: {}", oFlowSlot.getFlow().getName());
                    LOG.info("oFlowSlot date: {}", oFlowSlot.getsDate());
                    HistoricVariableInstance historicVariableInstance = oHistoryService
                            .createHistoricVariableInstanceQuery()
                            .processInstanceId(nID_Process)
                            .variableName("email").singleResult();
                    LOG.info("email {}", historicVariableInstance.getValue());
                    oEmailService.sendEmail((String) historicVariableInstance.getValue(),
                            "Ви скасували Ваш візит",
                            "Ви скасували Ваш візит. Деталі: " + oFlowSlot.getFlow().getName() + " " + dtf.print(oFlowSlot.getsDate()),
                            new MimeMultipart());
                    break;
                }

                LOG.info("(nID_Order={},nID_FlowSlotTicket={})", nID_Order, nID_FlowSlotTicket);
                if (!oFlowSlotTicketDao.unbindFromTask(nID_FlowSlotTicket)) {
                    throw new TaskAlreadyUnboundException("\u0417\u0430\u044f\u0432\u043a\u0430 \u0443\u0436\u0435 \u043e\u0442\u043c\u0435\u043d\u0435\u043d\u0430");
                }
            }
        }
        DateFormat df_StartProcess = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        oRuntimeService.setVariable(nID_Process, CANCEL_INFO_FIELD, String.format(
                "[%s] \u0417\u0430\u044f\u0432\u043a\u0430 \u0441\u043a\u0430\u0441\u043e\u0432\u0430\u043d\u0430: %s",
                df_StartProcess.format(new Date()), sInfo == null ? "" : sInfo));
    }

    private String addCalculatedFields(String saFieldsCalc, TaskInfo curTask, String currentRow) {
        HistoricTaskInstance details = oHistoryService.createHistoricTaskInstanceQuery().includeProcessVariables().taskId(curTask.getId()).singleResult();
        LOG.info("Process variables of the task {}:{}: {}", curTask.getId(), saFieldsCalc, details.getProcessVariables());
        if (details.getProcessVariables() != null) {
            Set<String> headersExtra = new HashSet<>();
            for (String key : details.getProcessVariables().keySet()) {
                if (!key.startsWith("sBody")) {
                    headersExtra.add(key);
                }
            }
            saFieldsCalc = StringUtils.substringAfter(saFieldsCalc, "\"");
            saFieldsCalc = StringUtils.substringBeforeLast(saFieldsCalc, "\"");
            for (String expression : saFieldsCalc.split(";")) {
                LOG.info("Processing expression: {}", expression);
                String variableName = StringUtils.substringBefore(expression, "=");
                String condition = StringUtils.substringAfter(expression, "=");
                LOG.info("Checking variable with (name={}, condition={}, expression={}) ", variableName, condition, expression);
                try {
                    Object conditionResult = getObjectResultofCondition(headersExtra, details, details, condition);
                    currentRow = currentRow + ";" + conditionResult;
                    LOG.info("Adding calculated field {} with the value {}", variableName, conditionResult);
                } catch (Exception oException) {
                    LOG.error("Error: {}, occured while processing (variable={}) ", oException.getMessage(), variableName);
                    LOG.debug("FAIL:", oException);
                }
            }
        }
        return currentRow;
    }

    public ResponseEntity<String> unclaimUserTask(String nID_UserTask) throws CommonServiceException, RecordNotFoundException {
        Task task = oTaskService.createTaskQuery().taskId(nID_UserTask).singleResult();
        if (task == null) {
            throw new RecordNotFoundException();
        }
        if (task.getAssignee() == null || task.getAssignee().isEmpty()) {
            return new ResponseEntity<>("Not assigned UserTask", HttpStatus.OK);
        }
        oTaskService.unclaim(task.getId());
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    public void loadFormPropertiesToMap(FormData formData, Map<String, Object> variables, Map<String, String> formValues) {
        List<FormProperty> aFormProperty = formData.getFormProperties();
        if (!aFormProperty.isEmpty()) {
            for (FormProperty oFormProperty : aFormProperty) {
                String sType = oFormProperty.getType().getName();
                if (variables.containsKey(oFormProperty.getId())) {
                    if ("enum".equals(sType)) {
                        Object variable = variables.get(oFormProperty.getId());
                        if (variable != null) {
                            String sID_Enum = variable.toString();
                            LOG.info("execution.getVariable()(sID_Enum={})", sID_Enum);
                            String sValue = parseEnumProperty(oFormProperty, sID_Enum);
                            formValues.put(oFormProperty.getId(), sValue);
                        }
                    } else {
                        formValues.put(oFormProperty.getId(), variables.get(oFormProperty.getId()) != null ? String.valueOf(variables.get(oFormProperty.getId())) : null);
                    }
                }
            }
        }
    }

    public Date getBeginDate(DateTime date) {
        if (date == null) {
            return DateTime.now().minusDays(1).toDate();
        }
        return date.toDate();
    }

    private Object getObjectResultofCondition(Set<String> headersExtra, HistoricTaskInstance currTask, HistoricTaskInstance details, String condition) throws ScriptException, NoSuchMethodException {
        Map<String, Object> params = new HashMap<>();
        for (String headerExtra : headersExtra) {
            Object variableValue = details.getProcessVariables().get(headerExtra);
            String propertyValue = sO(variableValue);
            params.put(headerExtra, propertyValue);
        }
        params.put("sAssignedLogin", currTask.getAssignee());
        params.put("sID_UserTask", currTask.getTaskDefinitionKey());
        LOG.info("Calculating expression with (params={})", params);
        Object conditionResult = new ToolJS().getObjectResultOfCondition(new HashMap<String, Object>(),
                params, condition);
        LOG.info("Condition of the expression is {}", conditionResult.toString());
        return conditionResult;
    }

    public ProcessDefinition getProcessDefinitionByTaskID(String nID_Task) {
        HistoricTaskInstance historicTaskInstance = oHistoryService.createHistoricTaskInstanceQuery()
                .taskId(nID_Task).singleResult();
        String sBP = historicTaskInstance.getProcessDefinitionId();
        ProcessDefinition processDefinition = oRepositoryService.createProcessDefinitionQuery()
                .processDefinitionId(sBP).singleResult();
        return processDefinition;
    }

    protected void processExtractFieldsParameter(Set<String> headersExtra, HistoricTaskInstance currTask, String saFields, Map<String, Object> line) {
        HistoricTaskInstance details = oHistoryService.createHistoricTaskInstanceQuery().includeProcessVariables().taskId(currTask.getId()).singleResult();
        LOG.info("Process variables of the task {}:{}", currTask.getId(), details.getProcessVariables());
        if (details.getProcessVariables() != null) {
            LOG.info("(Cleaned saFields={})", saFields);
            String[] expressions = saFields.split(";");
            if (expressions != null) {
                for (String expression : expressions) {
                    String variableName = StringUtils.substringBefore(expression, "=");
                    String condition = StringUtils.substringAfter(expression, "=");
                    LOG.info("Checking variable with (name={}, condition={}, expression={})", variableName, condition, expression);
                    try {
                        Object conditionResult = getObjectResultofCondition(headersExtra, currTask, details, condition);
                        line.put(variableName, conditionResult);
                    } catch (Exception oException) {
                        LOG.error("Error: {}, occured while processing variable {}", oException.getMessage(), variableName);
                        LOG.debug("FAIL:", oException);
                    }
                }
            }
        }
    }

    private void loadCandidateStarterGroup(ProcessDefinition processDef, Set<String> candidateCroupsToCheck) {
        List<IdentityLink> identityLinks = oRepositoryService.getIdentityLinksForProcessDefinition(processDef.getId());
        LOG.info(String.format("Found %d identity links for the process %s", identityLinks.size(), processDef.getKey()));
        for (IdentityLink identity : identityLinks) {
            if (IdentityLinkType.CANDIDATE.equals(identity.getType())) {
                String groupId = identity.getGroupId();
                candidateCroupsToCheck.add(groupId);
                LOG.info("Added candidate starter (group={})", groupId);
            }
        }
    }

    public String getOriginalProcessInstanceId(Long nID_Protected) throws CRCInvalidException {
        return Long.toString(ToolLuna.getValidatedOriginalNumber(nID_Protected));
    }

    public Attachment getAttachment(String attachmentId, Integer nFile, String processInstanceId) {
        String st = "Attachment for attachmentId = " + attachmentId
                + " processInstanceId = " + processInstanceId
                + " nFile = " + nFile;
        LOG.info("Find " + st);
        List<Attachment> attachments = oTaskService.getProcessInstanceAttachments(processInstanceId);
        LOG.info("Attachments list size = " + attachments.size());
        Attachment attachmentRequested = null;
        for (int i = 0; i < attachments.size(); i++) {
            LOG.info("Check attachment ID = " + attachments.get(i).getId() + "; name = " + attachments.get(i).getName());
            if (attachments.get(i).getId().equalsIgnoreCase(attachmentId) || (null != nFile && nFile.equals(i + 1))) {
                LOG.info("attachments.get(i).getId().equalsIgnoreCase(attachmentId) = " + attachments.get(i).getId().equalsIgnoreCase(attachmentId));
                LOG.info("(null != nFile && nFile.equals(i + 1)) = " + (null != nFile && nFile.equals(i + 1)));
                attachmentRequested = attachments.get(i);
                break;
            }
        }
        if (attachmentRequested == null) {
            try {
                attachmentRequested = oTaskService.getAttachment(attachmentId);
                LOG.info("Get attachment from taskService ID = " + attachmentRequested.getId() + "; name = " + attachmentRequested.getName());
            } catch (Exception oException) {
                LOG.info("Attachment not found in task service");
            }
        }
        if (attachmentRequested == null && !attachments.isEmpty()) {
            LOG.info("(attachmentRequested == null && !attachments.isEmpty()) = TRUE");
            attachmentRequested = attachments.get(0);
        }
        if (attachmentRequested == null) {
            throw new ActivitiObjectNotFoundException(st + " not found!");
        }
        LOG.info("Return attachment whith ID = " + attachmentRequested.getId());
        return attachmentRequested;
    }

    public void fillTheCSVMapHistoricTasks(String sID_BP, Date dateAt, Date dateTo, List<HistoricTaskInstance> foundResults, SimpleDateFormat sDateCreateDF, List<Map<String, Object>> csvLines, String pattern,
                                           Set<String> tasksIdToExclude, String saFieldsCalc, String[] headers, String sID_State_BP, String asField_Filter) {
        ToolJS oToolJs = new ToolJS();
        LOG.info("<--------------------------------fillTheCSVMapHistoricTasks_begin---------------------------------------------------------->");
        if (CollectionUtils.isEmpty(foundResults)) {
            LOG.info(String.format("No historic tasks found for business process %s for date period %s - %s", sID_BP, DATE_TIME_FORMAT.format(dateAt), DATE_TIME_FORMAT.format(dateTo)));
            return;
        }
        LOG.info(String.format("Found %s historic tasks for business process %s for date period %s - %s", foundResults.size(), sID_BP, DATE_TIME_FORMAT.format(dateAt), DATE_TIME_FORMAT.format(dateTo)));
        if (pattern != null) {
            LOG.info("List of fields to retrieve: {}", pattern);
        } else {
            LOG.info("Will retreive all fields from tasks");
        }
        LOG.info("Tasks to skip {}", tasksIdToExclude);
    
        Map<String, org.activiti.bpmn.model.FormProperty> enumProperties = getLastUserTask(foundResults.get(0).getProcessDefinitionId())
                .map(UserTask::getFormProperties)
                .orElse(new ArrayList<>())
                .stream()
                .filter(formProperty -> "enum".equalsIgnoreCase(formProperty.getType()))
                .collect(Collectors.toMap(org.activiti.bpmn.model.FormProperty::getId, formProperty -> formProperty));
        
        for (HistoricTaskInstance curTask : foundResults) {
            if (tasksIdToExclude.contains(curTask.getId())) {
                LOG.info("Skipping historic task {} from processing as it is already in the response", curTask.getId());
                continue;
            }

            LOG.info("Enum properties of the process: " + enumProperties);
            String currentRow = pattern;
            Map<String, Object> variables = curTask.getProcessVariables();
            LOG.info("!!!!!!!!!!!!!!!variablessb= " + variables);
            LOG.info("Loaded historic variables for the task {}|{}", curTask.getId(), variables);
            try {
                if (asField_Filter != null) {
                    Map<String, Object> variablesToFilter = new HashMap<>();
                    variablesToFilter.putAll(curTask.getProcessVariables());
                    variablesToFilter.putAll(curTask.getTaskLocalVariables());
                    if (!(Boolean) (oToolJs.getObjectResultOfCondition(new HashMap<>(), variables, asField_Filter))) {
                        LOG.info("filtered Task Id in fillTheCSVMapHistoricTasks {curTask.getId()}", curTask.getId());
                        continue;
                    }
                }
            } catch (ScriptException | NoSuchMethodException ex) {
                LOG.info("Error during fillTheCSVMapHistoricTasks filtering {}", ex);
            }
            currentRow = replaceEnumFormProperties(currentRow, variables, enumProperties);
            if (saFieldsCalc != null) {
                currentRow = addCalculatedFields(saFieldsCalc, curTask, currentRow);
            }
            if (pattern != null) {
                currentRow = replaceReportFields(sDateCreateDF, curTask, currentRow);
                currentRow = currentRow.replaceAll("\\$\\{.*?\\}", "");
            }
            String[] values = currentRow.split(";");
            LOG.info("values= " + values);
            if (headers.length != values.length) {
                LOG.info("Size of header :{} Size of values array:{}", headers.length, values.length);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < headers.length; i++) {
                    sb.append(headers[i]);
                    LOG.info("!!!!!!!!!!!!!!!sb= " + sb);
                    sb.append(";");
                    LOG.info("!!!!!!!!!!!!!!!sb= " + sb);
                }
                LOG.info("(headers={})", sb.toString());
                sb = new StringBuilder();
                LOG.info("!!!!!!!!!!!!!!!sb= " + sb);
                for (int i = 0; i < values.length; i++) {
                    sb.append(values[i]);
                    LOG.info("!!!!!!!!!!!!!!!sb= " + sb);
                    sb.append(";");
                    LOG.info("!!!!!!!!!!!!!!!sb= " + sb);
                }
                LOG.info("(values={})", sb.toString());
            }
            Map<String, Object> currRow = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                currRow.put(headers[i], i < values.length ? values[i] : "");
            }
            csvLines.add(currRow);
            LOG.info("csvLines= " + csvLines);
            LOG.info("<--------------------------------fillTheCSVMapHistoricTasks_end---------------------------------------------------------->");
        }
    }
    
    private Optional<UserTask> getLastUserTask(String sID_BP) {
        BpmnModel oBpmnModel = oRepositoryService.getBpmnModel(sID_BP);
        if (oBpmnModel == null) {
            LOG.warn("requested processDefinitionId does not exist, id: {}", sID_BP);
            return Optional.empty();
        }
        List<UserTask> aoUserTask = oBpmnModel
                .getProcesses()
                .stream()
                .flatMap(p -> p.getFlowElements().stream())
                .filter(element -> element instanceof UserTask)
                .map(element -> (UserTask) element)
                .collect(Collectors.toList());
        if (aoUserTask.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(Iterables.getLast(aoUserTask));
        }
    }

    /*
     * private void clearEmptyValues(Map<String, Object> params) {
     * Iterator<String> iterator = params.keySet().iterator(); while
     * (iterator.hasNext()){ String key = iterator.next(); if (params.get(key)
     * == null){ iterator.remove(); } } }
     */
    private void addTasksDetailsToLine(Set<String> headersExtra, HistoricTaskInstance currTask, Map<String, Object> resultLine) {
        LOG.debug("(currTask={})", currTask.getId());
        HistoricTaskInstance details = oHistoryService.createHistoricTaskInstanceQuery().includeProcessVariables().taskId(currTask.getId()).singleResult();
        if (details != null && details.getProcessVariables() != null) {
            for (String headerExtra : headersExtra) {
                Object variableValue = details.getProcessVariables().get(headerExtra);
                resultLine.put(headerExtra, variableValue);
            }
        }
    }

    private Set<String> findExtraHeadersForDetail(List<HistoricTaskInstance> foundResults, List<String> headers) {
        Set<String> headersExtra = new TreeSet<>();
        for (HistoricTaskInstance currTask : foundResults) {
            HistoricTaskInstance details = oHistoryService.createHistoricTaskInstanceQuery().includeProcessVariables().taskId(currTask.getId()).singleResult();
            if (details != null && details.getProcessVariables() != null) {
                LOG.info("(proccessVariavles={})", details.getProcessVariables());
                for (String key : details.getProcessVariables().keySet()) {
                    if (!key.startsWith("sBody")) {
                        headersExtra.add(key);
                    }
                }
            }
        }
        headers.addAll(headersExtra);
        return headersExtra;
    }
    
    private String replaceEnumFormProperties(String currentRow, Map<String, Object> data, Map<String, org.activiti.bpmn.model.FormProperty> enumProperties) {
        String res = currentRow;
        for (Map.Entry<String, Object> property : data.entrySet()) {
            LOG.info(String.format("Matching property %s:%s with fieldNames", property.getKey(), property.getValue()));
            //LOG.info("!!!!!!!!!!data: " + data);
            if (currentRow != null && res.contains("${" + property.getKey() + "}")) {
                LOG.info(String.format("Found field with id %s in the pattern. Adding value to the result", "${" + property.getKey() + "}"));
                String sValue = null;
                if (enumProperties.containsKey(property.getKey())) {
                    sValue = parseEnumProperty(enumProperties.get(property.getKey()), (String) property.getValue());
                } else if (property.getValue() != null) {
                    sValue = property.getValue().toString();
                }
                LOG.info("(sValue={})", sValue);
                if (sValue != null) {
                    LOG.info(String.format("Replacing field with the value %s", sValue));
                    res = res.replace("${" + property.getKey() + "}", sValue);
                }
            }
        }
        return res;
    }

    private String replaceFormProperties(String currentRow, Map<String, Object> data, Map<String, FormProperty> enumProperties) {
        String res = currentRow;
        for (Map.Entry<String, Object> property : data.entrySet()) {
            LOG.info(String.format("Matching property %s:%s with fieldNames", property.getKey(), property.getValue()));
            //LOG.info("!!!!!!!!!!data: " + data);
            if (currentRow != null && res.contains("${" + property.getKey() + "}")) {
                LOG.info(String.format("Found field with id %s in the pattern. Adding value to the result", "${" + property.getKey() + "}"));
                String sValue = null;
                if (enumProperties.containsKey(property.getKey())) {
                    sValue = parseEnumProperty(enumProperties.get(property.getKey()), (String) property.getValue());
                } else if (property.getValue() != null) {
                    sValue = property.getValue().toString();
                }
                LOG.info("(sValue={})", sValue);
                if (sValue != null) {
                    LOG.info(String.format("Replacing field with the value %s", sValue));
                    res = res.replace("${" + property.getKey() + "}", sValue);
                }
            }
        }
        return res;
    }

    private String replaceFormProperties(String currentRow, TaskFormData data) {
        String res = currentRow;
        for (FormProperty property : data.getFormProperties()) {
            LOG.info(String.format("Matching property %s %s %s with fieldNames", property.getId(), property.getName(), property.getType().getName()));
            //LOG.info("!!!!!!!!!!getId: " + property.getId() + " getName: " + property.getName() + " getType: " +  property.getType().getName() + " getValue: " +  property.getValue() + "!");
            if (currentRow != null && res.contains("${" + property.getId() + "}")) {
                LOG.info(String.format("Found field with id %s in the pattern. Adding value to the result", "${" + property.getId() + "}"));
                String sValue = getPropertyValue(property);
                if (sValue != null) {
                    LOG.info(String.format("Replacing field with the value %s", sValue));
                    res = res.replace("${" + property.getId() + "}", sValue);
                }
            }
        }
        return res;
    }

    public Charset getCharset(String sID_Codepage) {
        Charset charset;
        String codePage = sID_Codepage.replaceAll("-", "");
        try {
            if ("win1251".equalsIgnoreCase(codePage) || "CL8MSWIN1251".equalsIgnoreCase(codePage)) {
                codePage = "CP1251";
            }
            charset = Charset.forName(codePage);
            LOG.debug("use charset - {}", charset);
        } catch (IllegalArgumentException e) {
            LOG.error("Error: {}. Do not support charset - {}", e.getMessage(), codePage);
            throw new ActivitiObjectNotFoundException("Statistics for the business task for charset '" + codePage + "' cannot be construct.", Task.class, e);
        }
        return charset;
    }

    public String getFileExtention(MultipartFile file) {
        String[] parts = file.getOriginalFilename().split("\\.");
        if (parts.length != 0) {
            return parts[parts.length - 1];
        }
        return "";
    }

    public String getFileExtention(String fileName) {
        String[] parts = fileName.split("\\.");
        if (parts.length != 0) {
            return parts[parts.length - 1];
        }
        return "";
    }

    /*private static class TaskAlreadyUnboundException extends Exception {
    private TaskAlreadyUnboundException(String message) {
    super(message);
    }
    }*/
    public Map<String, Object> createCsvLine(boolean bDetail, Set<String> headersExtra, HistoricTaskInstance currTask, String saFields) {
        Map<String, Object> line = new HashMap<>();
        line.put("nID_Process", currTask.getProcessInstanceId());
        line.put("sLoginAssignee", currTask.getAssignee());
        Date startDate = currTask.getStartTime();
        line.put("sDateTimeStart", DATE_TIME_FORMAT.format(startDate));
        line.put("nDurationMS", String.valueOf(currTask.getDurationInMillis()));
        long durationInHours = currTask.getDurationInMillis() / MILLIS_IN_HOUR;
        line.put("nDurationHour", String.valueOf(durationInHours));
        line.put("sName", currTask.getName());
        if (bDetail) {
            addTasksDetailsToLine(headersExtra, currTask, line);
        }
        if (saFields != null) {
            processExtractFieldsParameter(headersExtra, currTask, saFields, line);
        }
        return line;
    }

    public String getSeparator(String sID_BP, String nASCI_Spliter) {
        if (nASCI_Spliter == null) {
            return String.valueOf(Character.toChars(DEFAULT_REPORT_FIELD_SPLITTER));
        }
        if (!StringUtils.isNumeric(nASCI_Spliter)) {
            LOG.error("ASCI code is not a number {}", nASCI_Spliter);
            throw new ActivitiObjectNotFoundException("Statistics for the business task with name '" + sID_BP + "' not found. Wrong splitter.", Task.class);
        }
        return String.valueOf(Character.toChars(Integer.valueOf(nASCI_Spliter)));
    }

    public String formHeader(String saFields, List<HistoricTaskInstance> foundHistoricResults, String saFieldsCalc) {
        String res = null;
        if (saFields != null && !"".equals(saFields.trim())) {
            LOG.info("Fields have custom header names");
            StringBuilder sb = new StringBuilder();
            String[] fields = saFields.split(";");
            LOG.info("fields: " + fields);
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].contains("\\=")) {
                    sb.append(StringUtils.substringBefore(fields[i], "\\="));
                    LOG.info("if (fields[i].contains(\"\\\\=\"))_sb: " + sb);
                } else {
                    sb.append(fields[i]);
                    LOG.info("else_sb: " + sb);
                }
                if (i < fields.length - 1) {
                    sb.append(";");
                    LOG.info("(i < fields.length - 1)_sb: " + sb);
                }
            }
            res = sb.toString();
            res = res.replaceAll("\\$\\{", "");
            res = res.replaceAll("\\}", "");
            LOG.info("Formed header from list of fields: {}", res);
        } else {
            if (foundHistoricResults != null && !foundHistoricResults.isEmpty()) {
                HistoricTaskInstance historicTask = foundHistoricResults.get(0);
                Set<String> keys = historicTask.getProcessVariables().keySet();
                StringBuilder sb = new StringBuilder();
                Iterator<String> iter = keys.iterator();
                while (iter.hasNext()) {
                    sb.append(iter.next());
                    if (iter.hasNext()) {
                        sb.append(";");
                    }
                }
                res = sb.toString();
                LOG.info("res: " + res);
            }
            LOG.info("Formed header from all the fields of a task: {}", res);
        }
        if (saFieldsCalc != null) {
            saFieldsCalc = StringUtils.substringAfter(saFieldsCalc, "\"");
            saFieldsCalc = StringUtils.substringBeforeLast(saFieldsCalc, "\"");
            String[] params = saFieldsCalc.split(";");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < params.length; i++) {
                String currParam = params[i];
                String cutHeader = StringUtils.substringBefore(currParam, "=");
                LOG.info("Adding header to the csv file from saFieldsCalc: {}", cutHeader);
                sb.append(cutHeader);
                if (i < params.length - 1) {
                    sb.append(";");
                }
            }
            res = res + ";" + sb.toString();
            LOG.info("Header with calculated fields: {}", res);
        }
        return res;
    }

    public Task getTaskByID(String nID_Task) {
        return oTaskService.createTaskQuery().taskId(nID_Task).singleResult();
    }

    private List<Task> getTasksByProcessInstanceId(String processInstanceID) throws RecordNotFoundException {
        List<Task> tasks = oTaskService.createTaskQuery().processInstanceId(processInstanceID).list();
        if (tasks == null || tasks.isEmpty()) {
            LOG.error(String.format("Tasks for Process Instance [id = '%s'] not found", processInstanceID));
            throw new RecordNotFoundException();
        }
        return tasks;
    }

    private String getPropertyValue(FormProperty property) {
        String sValue;
        String sType = property.getType().getName();
        LOG.info("getId:" + property.getId() + " getName: " + property.getName() + " getType: " + sType + " getValue: " + property.getValue());
        if ("enum".equalsIgnoreCase(sType)) {
            sValue = parseEnumProperty(property);
        } else {
            sValue = property.getValue();
        }
        LOG.info("(sValue={})", sValue);
        return sValue;
    }

    public Set<String> findExtraHeaders(Boolean bDetail, List<HistoricTaskInstance> foundResults, List<String> headers) {
        if (bDetail) {
            Set<String> headersExtra = findExtraHeadersForDetail(foundResults, headers);
            return headersExtra;
        } else {
            return new TreeSet<>();
        }
    }

    private String replaceReportFields(SimpleDateFormat sDateCreateDF, Task curTask, String currentRow) {
        String res = currentRow;
        for (TaskReportField field : TaskReportField.values()) {
            if (res.contains(field.getPattern())) {
                res = field.replaceValue(res, curTask, sDateCreateDF, oGeneralConfig);//sID_Order
                LOG.info("!!!!!!!!!!res: " + res);
            }
        }
        return res;
    }

    private String replaceReportFields(SimpleDateFormat sDateCreateDF, HistoricTaskInstance curTask, String currentRow) {
        LOG.info("<--------------------------------replaceReportFields_begin-------------------------------------------->");
        String res = currentRow;
        for (TaskReportField field : TaskReportField.values()) {
            if (res.contains(field.getPattern())) {
                res = field.replaceValue(res, curTask, sDateCreateDF, oGeneralConfig);
                LOG.info("!!!!!!!!!!res: " + res);
            }
        }
        LOG.info("<--------------------------------replaceReportFields_end-------------------------------------------->");
        return res;

    }

    private void loadCandidateGroupsFromTasks(ProcessDefinition oProcessDefinition, Set<String> asID_CandidateGroupToCheck) {
        //LOG.info("oProcessDefinition.getId()={}", oProcessDefinition.getId());
        BpmnModel oBpmnModel = oRepositoryService.getBpmnModel(oProcessDefinition.getId());
        for (FlowElement oFlowElement : oBpmnModel.getMainProcess().getFlowElements()) {
            if (oFlowElement instanceof UserTask) {
                UserTask oUserTask = (UserTask) oFlowElement;
                LOG.info("oUserTask.getId()={}", oUserTask.getId());
                List<String> asID_CandidateGroup = oUserTask.getCandidateGroups();
                if (asID_CandidateGroup != null && !asID_CandidateGroup.isEmpty()) {
                    asID_CandidateGroupToCheck.addAll(asID_CandidateGroup);
                    LOG.info("Added candidate groups asID_CandidateGroup={} from user task oUserTask.getId()={}", asID_CandidateGroup, oUserTask.getId());
                }
            }
        }
    }

    private Set<String> getGroupsOfProcessTask(ProcessDefinition oProcessDefinition) {
        Set<String> asID_Group_Return = new HashSet();
        //LOG.info("oProcessDefinition.getId()={}", oProcessDefinition.getId());
        BpmnModel oBpmnModel = oRepositoryService.getBpmnModel(oProcessDefinition.getId());
        for (FlowElement oFlowElement : oBpmnModel.getMainProcess().getFlowElements()) {
            if (oFlowElement instanceof UserTask) {
                UserTask oUserTask = (UserTask) oFlowElement;
                LOG.info("oUserTask.getId()={}", oUserTask.getId());
                List<String> asID_CandidateGroup = oUserTask.getCandidateGroups();
                if (asID_CandidateGroup != null && !asID_CandidateGroup.isEmpty()) {
                    asID_Group_Return.addAll(asID_CandidateGroup);
                    LOG.info("Added candidate groups asID_CandidateGroup={}", asID_CandidateGroup);
                }
                String sID_Assign = oUserTask.getAssignee();
                if (sID_Assign != null) {
                    asID_Group_Return.add(sID_Assign);
                    LOG.info("Added candidate groups sID_Assign={}", sID_Assign);
                }
            }
        }
        return asID_Group_Return;
    }

    /**
     * saFeilds paramter may contain name of headers or can be empty. Before
     * forming the result - we need to cut header names
     * <p>
     * // * @param saFields // * @param foundHistoricResults // * @return
     */
    public String processSaFields(String saFields, List<HistoricTaskInstance> foundHistoricResults) {
        String res = null;

        if (saFields != null) {
            LOG.info("saFields has custom header names");
            StringBuilder sb = new StringBuilder();
            String[] fields = saFields.split(";");
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].contains("=")) {
                    sb.append(StringUtils.substringAfter(fields[i], "="));
                } else {
                    sb.append(fields[i]);
                }
                if (i < fields.length - 1) {
                    sb.append(";");
                }
            }
            res = sb.toString();
        } else {
            if (foundHistoricResults != null && !foundHistoricResults.isEmpty()) {
                HistoricTaskInstance historicTask = foundHistoricResults.get(0);
                Set<String> keys = historicTask.getProcessVariables().keySet();
                StringBuilder sb = new StringBuilder();
                Iterator<String> iter = keys.iterator();
                while (iter.hasNext()) {
                    sb.append("${").append(iter.next()).append("}");
                    if (iter.hasNext()) {
                        sb.append(";");
                    }
                }
                res = sb.toString();
            }
            LOG.info("Formed header from all the fields of a task: {}", res);
        }
        return res;
    }

    public Long getIDProtectedFromIDOrder(String sID_order) {
        StringBuilder ID_Protected = new StringBuilder();
        int hyphenPosition = sID_order.lastIndexOf("-");
        if (hyphenPosition < 0) {
            for (int i = 0; i < sID_order.length(); i++) {
                buildID_Protected(sID_order, ID_Protected, i);
            }
        } else {
            for (int i = hyphenPosition + 1; i < sID_order.length(); i++) {
                buildID_Protected(sID_order, ID_Protected, i);
            }
        }
        return Long.parseLong(ID_Protected.toString());
    }

    private void buildID_Protected(String sID_order, StringBuilder ID_Protected, int i) {
        String ch = "" + sID_order.charAt(i);
        Scanner scanner = new Scanner(ch);
        if (scanner.hasNextInt()) {
            ID_Protected.append(ch);
        }
    }

    public Date getEndDate(DateTime date) {
        if (date == null) {
            return DateTime.now().toDate();
        }
        return date.plusDays(1).toDate();
    }

    public List<String> getTaskIdsByProcessInstanceId(String processInstanceID) {

        return findTaskIDsByActiveAndHistoryProcessInstanceID(Long.parseLong(processInstanceID));

    }

    /**
     * Проверить есть ли у процесса таски (и активные и закрытые)
     *
     * @param snID_Process ид процесса
     * @return результат проверки true - таски есть, false - нет
     */
    public boolean processHasTask(String snID_Process) {
        boolean bResult = false;
        if (!getTaskIdsByProcessInstanceId(snID_Process).isEmpty()) {
            bResult = true;
        }
        return bResult;
    }

    public void fillTheCSVMap(String sID_BP, Date dateAt, Date dateTo, List<Task> aTaskFound, SimpleDateFormat sDateCreateDF,
                              List<Map<String, Object>> csvLines, String pattern, String saFieldsCalc, String[] asHeader, String asField_Filter) {
        if (CollectionUtils.isEmpty(aTaskFound)) {

            LOG.info(String.format("No tasks found for business process %s for date period %s - %s", sID_BP, DATE_TIME_FORMAT.format(dateAt), DATE_TIME_FORMAT.format(dateTo)));
            return;
        }
        LOG.info(String.format("Found %s tasks for business process %s for date period %s - %s", aTaskFound.size(), sID_BP, DATE_TIME_FORMAT.format(dateAt), DATE_TIME_FORMAT.format(dateTo)));
        if (pattern != null) {
            LOG.info("List of fields to retrieve: {}", pattern);
        } else {
            LOG.info("Will retreive all fields from tasks");
        }

        ToolJS oToolJs = new ToolJS();
        for (Task oTask : aTaskFound) {

            if (asField_Filter != null) {
                try {
                    Map<String, Object> variablesToFilter = new HashMap<>();
                    variablesToFilter.putAll(oTask.getProcessVariables());
                    variablesToFilter.putAll(oTask.getTaskLocalVariables());
                    if (!(Boolean) (oToolJs.getObjectResultOfCondition(new HashMap<>(), variablesToFilter, asField_Filter))) {
                        LOG.info("filtered Task Id in fillTheCSVMap {curTask.getId()}", oTask.getId());
                        continue;
                    }

                } catch (ScriptException | NoSuchMethodException ex) {
                    LOG.info("Error during fillTheCSVMapHistoricTasks filtering {}", ex);
                }
            }
            String sRow = pattern;
            LOG.trace("Process task - {}", oTask);
            TaskFormData oTaskFormData = oFormService.getTaskFormData(oTask.getId());

            sRow = replaceFormProperties(sRow, oTaskFormData);
            LOG.info("!!!!!!!!!!!!!!!!!!!!!!fillTheCSVMap!_!sRows= " + sRow);
            if (saFieldsCalc != null) {
                sRow = addCalculatedFields(saFieldsCalc, oTask, sRow);
            }
            if (pattern != null) {
                sRow = replaceReportFields(sDateCreateDF, oTask, sRow);
                sRow = sRow.replaceAll("\\$\\{.*?\\}", "");
            }
            String[] asField = sRow.split(";");
            Map<String, Object> mCell = new HashMap<>();
            for (int i = 0; i < asField.length; i++) {
                try {
                    String sName = "Column_" + i;
                    if (asHeader.length > i) {
                        sName = asHeader[i];
                    }
                    mCell.put(sName, asField[i]);
                } catch (Exception oException) {
                    LOG.warn("oException.getMessage()={} (i={},mCell={},asHeader={},asField={})", oException.getMessage(), i, mCell, asHeader, asField);
                }
            }
            csvLines.add(mCell);
        }
    }

    public String[] createStringArray(Map<String, Object> csvLine, List<String> headers) {
        List<String> result = new LinkedList<>();
        for (String header : headers) {
            Object value = csvLine.get(header);
            result.add(value == null ? "" : value.toString());
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Получение списка бизнес процессов к которым у пользователя есть доступ
     *
     * @param sLogin               - Логин. пользователя
     * @param bDocOnly             Выводить только список БП документов
     * @param sProcessDefinitionId - выводить только из этого процесса
     * @return
     */
    public List<Map<String, String>> getBusinessProcessesOfLogin(String sLogin, Boolean bDocOnly, String sProcessDefinitionId) {

        List<ProcessDefinition> aProcessDefinition_Return = getProcessDefinitionOfLogin(sLogin, bDocOnly, sProcessDefinitionId);

        List<Map<String, String>> amPropertyBP = new LinkedList<>();

        for (ProcessDefinition oProcessDefinition : aProcessDefinition_Return) {
            Map<String, String> mPropertyBP = new HashMap<>();

            mPropertyBP.put("sID", oProcessDefinition.getKey());
            mPropertyBP.put("sName", oProcessDefinition.getName());

            LOG.info("Added record to response {}", mPropertyBP);
            amPropertyBP.add(mPropertyBP);
        }

        return amPropertyBP;
    }

    /**
     * Получение списка полей бизнес процессов, к которым у пользователя есть
     * доступ
     *
     * @param sLogin               - Логин пользователя
     * @param bDocOnly             - Выводить только список БП документов
     * @param sProcessDefinitionId - Ид БП, если передается возвращаются поля
     *                             только этого процесса
     * @return Лист полей, согласно запросу
     */
    public List<Map<String, String>> getBusinessProcessesFieldsOfLogin(String sLogin, Boolean bDocOnly, String sProcessDefinitionId) {
        LOG.info("getBusinessProcessesFieldsOfLogin started with sLogin={}, bDocOnly={}, sProcessDefinitionId={}",
                sLogin, bDocOnly, sProcessDefinitionId);
        List<ProcessDefinition> aProcessDefinition_Return = getProcessDefinitionOfLogin(sLogin, bDocOnly, sProcessDefinitionId);
        Map<String, Map<String, String>> amPropertyBP = new HashMap<>();

        for (ProcessDefinition oProcessDefinition : aProcessDefinition_Return) {
            StartFormData formData = oFormService.getStartFormData(oProcessDefinition.getId());

            for (FormProperty property : formData.getFormProperties()) {
                Map<String, String> mPropertyBP = new HashMap<>();

                mPropertyBP.put("sID", property.getId());
                mPropertyBP.put("sName", property.getName());
                mPropertyBP.put("sID_Type", property.getType() != null ? property.getType().getName() : null);

                amPropertyBP.put(mPropertyBP.get("sID"), mPropertyBP);
                LOG.debug("Added record to response {}", mPropertyBP);
            }

            Collection<FlowElement> elements = oRepositoryService.getBpmnModel(oProcessDefinition.getId())
                    .getMainProcess().getFlowElements();

            for (FlowElement flowElement : elements) {
                if (flowElement instanceof UserTask) {
                    LOG.info("Processing user task with ID {} name {} ", flowElement.getId(), flowElement.getName());
                    UserTask userTask = (UserTask) flowElement;

                    for (org.activiti.bpmn.model.FormProperty property : userTask.getFormProperties()) {
                        Map<String, String> mPropertyBP = new HashMap<>();
                        mPropertyBP.put("sID", property.getId());
                        mPropertyBP.put("sName", property.getName());
                        mPropertyBP.put("sID_Type", property.getType());

                        amPropertyBP.put(mPropertyBP.get("sID"), mPropertyBP);
                        LOG.debug("Added record to response from user task {}", mPropertyBP);
                    }
                }
            }
        }

        List<Map<String, String>> res = new LinkedList<>();
        res.addAll(amPropertyBP.values());

        return res;
    }

    private List<ProcessDefinition> getProcessDefinitionOfLogin(String sLogin, Boolean bDocOnly, String sProcessDefinitionId) {
        LOG.info("getProcessDefinitionOfLogin started with sLogin={}, bDocOnly={}, sProcessDefinitionId={}",
                sLogin, bDocOnly, sProcessDefinitionId);
        List<ProcessInstance> aAllProcessInstance = new ArrayList<>();
        //вернуть только документы
        if (bDocOnly) {

            List<ProcessInstance> aProcessInstanceHistory = oRuntimeService.createNativeProcessInstanceQuery().sql(
                    "Select proc.* from act_hi_procinst proc, act_hi_identitylink link where proc.id_ = link.proc_inst_id_"
                            + "                                                        and link.user_id_ = '" + sLogin + "'"
                            + "                                                        and proc.proc_def_id_ like '_doc_%'"
            ).list();

            List<ProcessInstance> aProcessInstanceActive = oRuntimeService.createNativeProcessInstanceQuery().sql(
                    "Select proc.* from act_ru_identitylink link, act_hi_taskinst task, act_hi_procinst proc where link.task_id_ = task.id_"
                            + "                                                        and task.proc_inst_id_ = proc.proc_inst_id_"
                            + "                                                        and link.group_id_ = '" + sLogin + "'"
                            + "                                                        and proc.proc_def_id_ like '_doc_%'"
            ).list();

            aAllProcessInstance.addAll(aProcessInstanceHistory);
            aAllProcessInstance.addAll(aProcessInstanceActive);

            //вернуть только заданный sProcessDefinitionId
        } else if (sProcessDefinitionId != null) {

            List<ProcessInstance> aProcessInstanceHistory = oRuntimeService.createNativeProcessInstanceQuery().sql(
                    "Select proc.* from act_hi_procinst proc, act_hi_identitylink link where proc.id_ = link.proc_inst_id_"
                            + "                                                        and link.user_id_ = '" + sLogin + "'"
                            + "                                                        and proc.proc_def_id_ like '" + sProcessDefinitionId + "%'"
            ).list();

            List<ProcessInstance> aProcessInstanceActive = oRuntimeService.createNativeProcessInstanceQuery().sql(
                    "Select proc.* from act_ru_identitylink link, act_hi_taskinst task, act_hi_procinst proc where link.task_id_ = task.id_"
                            + "                                                        and task.proc_inst_id_ = proc.proc_inst_id_"
                            + "                                                        and link.group_id_ = '" + sLogin + "'"
                            + "                                                        and proc.proc_def_id_ like '" + sProcessDefinitionId + "%'"
            ).list();

            aAllProcessInstance.addAll(aProcessInstanceHistory);
            aAllProcessInstance.addAll(aProcessInstanceActive);

            //вернуть все процессы для логина
        } else if (!bDocOnly && sProcessDefinitionId == null) {

            List<ProcessInstance> aProcessInstanceHistory = oRuntimeService.createNativeProcessInstanceQuery().sql(
                    "Select proc.* from act_hi_procinst proc, act_hi_identitylink link where proc.id_ = link.proc_inst_id_"
                            + "                                                        and link.user_id_ = '" + sLogin + "'"
            ).list();

            List<ProcessInstance> aProcessInstanceActive = oRuntimeService.createNativeProcessInstanceQuery().sql(
                    "Select proc.* from act_ru_identitylink link, act_hi_taskinst task, act_hi_procinst proc where link.task_id_ = task.id_"
                            + "                                                        and task.proc_inst_id_ = proc.proc_inst_id_"
                            + "                                                        and link.group_id_ = '" + sLogin + "'"
            ).list();

            aAllProcessInstance.addAll(aProcessInstanceHistory);
            aAllProcessInstance.addAll(aProcessInstanceActive);
        }
        //Сет в который записываются sProcessDefinitionId без версионности, чтобы убрать дубли одних и тех же процессов,
        //но с разными версиями
        Set<String> asProcessDefinitionIdWithoutVersion = new HashSet<>();
        //Лист без дублей
        List<ProcessInstance> aProcessInstanceWithoutDuplicates = new ArrayList<>();

        for (ProcessInstance oProcessInstance : aAllProcessInstance) {
            String sProcessDefinitionIdRoot = oProcessInstance.
                    getProcessDefinitionId().substring(0, oProcessInstance.getProcessDefinitionId().indexOf(":"));
            //если в сете уже лежит такой ProcessDefinitionId, то не кладем в итоговый лист
            if (!asProcessDefinitionIdWithoutVersion.contains(sProcessDefinitionIdRoot)) {

                aProcessInstanceWithoutDuplicates.add(oProcessInstance);
            }
            asProcessDefinitionIdWithoutVersion.add(sProcessDefinitionIdRoot);
        }

        List<ProcessDefinition> aProcessDefinition_Return = new ArrayList<>();

        for (ProcessInstance oProcessInstance : aProcessInstanceWithoutDuplicates) {

            ProcessDefinition oProcessDefinition = oRepositoryService.
                    getProcessDefinition(oProcessInstance.getProcessDefinitionId());
            aProcessDefinition_Return.add(oProcessDefinition);
        }
        LOG.info("aProcessDefinition_Return={}", aProcessDefinition_Return);
        return aProcessDefinition_Return;
    }

    /**
     * Получение списка бизнес процессов к которым у пользователя есть доступ
     *
     * @param sLogin - Логин пользователя
     * @return
     */
    @Deprecated //новый: getBusinessProcessesOfLogin
    public List<Map<String, String>> getBusinessProcessesForUser(String sLogin) {

        return getBusinessProcessesOfLogin(sLogin, false, null);
    }

    private List<ProcessInfoShortVO> getAvailabilityProcessDefinitionByLogin(String sLogin, List<ProcessDefinition> aProcessDefinition) throws NotFoundException, NotFoundException, NotFoundException {

        List<ProcessDefinition> aProcessDefinition_Return = new LinkedList<>();

        List<Group> aGroup;
        aGroup = oIdentityService.createGroupQuery().groupMember(sLogin).list();
        if (aGroup != null && !aGroup.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Group oGroup : aGroup) {
                sb.append(oGroup.getId());
                sb.append(",");
            }
            LOG.info("Found {}  groups for the user {}:{}", aGroup.size(), sLogin, sb.toString());
        }

        for (ProcessDefinition oProcessDefinition : aProcessDefinition) {
            LOG.info("process definition id: {}", oProcessDefinition.getId());

            Set<String> aCandidateCroupsToCheck = getGroupsByProcessDefinition(oProcessDefinition);
            LOG.info("aCandidateCroupsToCheck is {}", aCandidateCroupsToCheck);
            if (checkIncludeProcessDefinitionIntoGroupList(aGroup, aCandidateCroupsToCheck)) {
                aProcessDefinition_Return.add(oProcessDefinition);
            }
        }

        List<ProcessInfoShortVO> aProcessInfoShortVO = new LinkedList();

        for (ProcessDefinition processDef : aProcessDefinition_Return) {
            ProcessInfoShortVO oProcessInfoShortVO = new ProcessInfoShortVO();
            oProcessInfoShortVO.setsID(processDef.getKey());
            oProcessInfoShortVO.setsName(processDef.getName());
            aProcessInfoShortVO.add(oProcessInfoShortVO);
            /*
            Map<String, String> process = new HashMap<>();
            process.put("sID", processDef.getKey());
            process.put("sName", processDef.getName());
            LOG.info(String.format("Added record to response %s", process.toString()));
            result.add(process);*/
        }

        List<SubjectRightBPVO> aResSubjectRightBPVO = oSubjectRightBPService.getBPs_ForReferent_bysLogin(sLogin);
        LOG.info("aResSubjectRightBPVO in getSubjectRightBPs is {}", aResSubjectRightBPVO);

        if (aResSubjectRightBPVO != null) {
            for (SubjectRightBPVO oSubjectRightBPVO : aResSubjectRightBPVO) {
                //aProcessDefinition_Return.add(oSubjectRightBPVO.getoSubjectRightBP().getsID_BP() );
                ProcessInfoShortVO oProcessInfoShortVO = new ProcessInfoShortVO();
                oProcessInfoShortVO.setsID(oSubjectRightBPVO.getoSubjectRightBP().getsID_BP());
                oProcessInfoShortVO.setsName(oSubjectRightBPVO.getsName_BP());
                aProcessInfoShortVO.add(oProcessInfoShortVO);

            }
        }

        //return aProcessDefinition_Return;
        return aProcessInfoShortVO;

    }

    private Set<String> getGroupsByProcessDefinition(ProcessDefinition oProcessDefinition) {
        Set<String> aCandidateCroupsToCheck = new HashSet<>();
        loadCandidateGroupsFromTasks(oProcessDefinition, aCandidateCroupsToCheck);
        loadCandidateStarterGroup(oProcessDefinition, aCandidateCroupsToCheck);
        return aCandidateCroupsToCheck;
    }

    private boolean checkIncludeProcessDefinitionIntoGroupList(List<Group> aGroup, Set<String> asProcessGroupMask) {
        for (Group oGroup : aGroup) {
            for (String sProcessGroupMask : asProcessGroupMask) {

                if (sProcessGroupMask.contains("${")) {
                    sProcessGroupMask = sProcessGroupMask.replaceAll("\\$\\{?.*}", "(.*)");
                }

                if (oGroup.getId().matches(sProcessGroupMask)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Map<String, String> getTaskFormDataInternal(Long nID_Task) throws CommonServiceException {
        Map<String, String> result = new HashMap<>();
        Task task = oTaskService.createTaskQuery().taskId(nID_Task.toString()).singleResult();
        LOG.info("Found task with (ID={}, process instance ID={})", nID_Task, task.getProcessInstanceId());
        FormData taskFormData = oFormService.getTaskFormData(task.getId());
        Map<String, Object> variables = oRuntimeService.getVariables(task.getProcessInstanceId());
        if (taskFormData != null) {
            loadFormPropertiesToMap(taskFormData, variables, result);
        }
        return result;
    }

    public Map<String, Object> sendProccessToGRESInternal(Long nID_Task) throws CommonServiceException {
        Map<String, Object> res = new HashMap<>();

        Task task = oTaskService.createTaskQuery().taskId(nID_Task.toString()).singleResult();

        LOG.info("Found task with (ID={}, process inctanse ID={})", nID_Task, task.getProcessInstanceId());

        HistoricProcessInstance processInstance = oHistoryService.createHistoricProcessInstanceQuery().processInstanceId(
                task.getProcessInstanceId()).singleResult();

        ProcessDefinition processDefinition = oRepositoryService.createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId()).singleResult();

        FormData startFormData = oFormService.getStartFormData(processInstance.getProcessDefinitionId());
        FormData taskFormData = oFormService.getTaskFormData(task.getId());

        res.put("nID_Task", nID_Task.toString());
        res.put("nID_Proccess", task.getProcessInstanceId());
        res.put("sProcessName", processDefinition.getName());
        res.put("sProcessDefinitionKey", processDefinition.getKey());

        Map<String, Object> variables = oRuntimeService.getVariables(task.getProcessInstanceId());

        Map<String, String> startFormValues = new HashMap<>();
        Map<String, String> taskFormValues = new HashMap<>();
        if (startFormData != null) {
            loadFormPropertiesToMap(startFormData, variables, startFormValues);
        }
        if (taskFormData != null) {
            loadFormPropertiesToMap(taskFormData, variables, taskFormValues);
        }

        res.put("startFormData", startFormValues);
        res.put("taskFormData", taskFormValues);

        return res;
    }

    public String updateHistoryEvent_Service(HistoryEvent_Service_StatusType oHistoryEvent_Service_StatusType, String sID_Order,
                                             String saField, String sBody, String sToken, String sUserTaskName, String sSubjectInfo, Long nID_Subject
    ) throws Exception {

        Map<String, Object> mBody = new HashMap<>();
        Map<String, String> mParam = new HashMap<>();
        mParam.put("sID_Order", sID_Order);
        LOG.info("sID_Order", sID_Order);
        mBody.put("soData", saField);
        LOG.info("soData {}", saField);
        mBody.put("sBody", sBody);
        LOG.info("sBody", sBody);
        mParam.put("sSubjectInfo", sSubjectInfo);
        if (nID_Subject != null) {
            mParam.put("nID_Subject", nID_Subject + "");
        }
        if (sUserTaskName != null) {
            mParam.put("sUserTaskName", sUserTaskName);
        }

        mParam.put("nID_StatusType", oHistoryEvent_Service_StatusType.getnID() + "");
        LOG.info("nID_StatusType", oHistoryEvent_Service_StatusType.getnID() + "");
        mParam.put("sToken", sToken);
        LOG.info("mParam from ActionTaskService = {};", mParam);
        LOG.info("mBody from ActionTaskService = {};", mBody);
        return oHistoryEventService.updateHistoryEvent(mParam, mBody);
    }

    public List<Task> getTasksForChecking(String sLogin,
                                          Boolean bEmployeeUnassigned) {
        List<Task> tasks;
        if (bEmployeeUnassigned) {
            tasks = oTaskService.createTaskQuery().taskCandidateUser(sLogin).taskUnassigned().active().list();
        } else {
            tasks = oTaskService.createTaskQuery().taskCandidateOrAssigned(sLogin).active().list();
        }

        return tasks.stream()
                .filter(oTask
                        -> !(oTask.getProcessDefinitionId().startsWith("_doc")
                        || oTask.getProcessDefinitionId().startsWith("_task")))
                .collect(Collectors.toList());
    }

    /*public static void main(String[] args) {
        System.out.println(createTable_TaskProperties("[{'id':'bankIdfirstName','type':'string','value':'3119325858'}]"));
    }*/
    public static void replacePatterns(DelegateExecution execution, DelegateTask task, Logger LOG) {
        try {
            LOG.info("(task.getId()={})", task.getId());
            //LOG.info("execution.getId()=" + execution.getId());
            //LOG.info("task.getVariable(\"sBody\")=" + task.getVariable("sBody"));
            //LOG.info("execution.getVariable(\"sBody\")=" + execution.getVariable("sBody"));

            EngineServices oEngineServices = execution.getEngineServices();
            RuntimeService oRuntimeService = oEngineServices.getRuntimeService();
            TaskFormData oTaskFormData = oEngineServices
                    .getFormService()
                    .getTaskFormData(task.getId());

            LOG.info("Found taskformData={}", oTaskFormData);
            if (oTaskFormData == null) {
                return;
            }

            Collection<File> asPatterns = getFiles_PatternPrint();
            for (FormProperty oFormProperty : oTaskFormData.getFormProperties()) {
                String sFieldID = oFormProperty.getId();
                String sExpression = oFormProperty.getName();

                LOG.info("(sFieldID={})", sFieldID);
                //LOG.info("sExpression=" + sExpression);
                LOG.info("(sExpression.length()={})", sExpression != null ? sExpression.length() + "" : "");

                if (sExpression == null || sFieldID == null || !sFieldID.startsWith("sBody")) {
                    continue;
                }

                for (File oFile : asPatterns) {
                    String sName = "pattern/print/" + oFile.getName();
                    //LOG.info("sName=" + sName);

                    if (sExpression.contains("[" + sName + "]")) {
                        LOG.info("sExpression.contains! (sName={})", sName);

                        String sData = ToolFS.getFileString(oFile, null);
                        //LOG.info("sData=" + sData);
                        LOG.info("(sData.length()={})", sData != null ? sData.length() + "" : "null");
                        if (sData == null) {
                            continue;
                        }

                        sExpression = sExpression.replaceAll("\\Q[" + sName + "]\\E", sData);
                        //                        LOG.info("sExpression=" + sExpression);

                        //LOG.info("[replacePatterns](sFieldID=" + sFieldID + "):1-Ok!");
                        oRuntimeService.setVariable(task.getProcessInstanceId(), sFieldID, sExpression);
                        /*                        LOG.info("[replacePatterns](sFieldID=" + sFieldID + "):2-Ok:" + oRuntimeService
                                .getVariable(task.getProcessInstanceId(), sFieldID));*/
                        LOG.info("setVariable Ok! (sFieldID={})", sFieldID);
                    }
                    LOG.info("Ok! (sName={})", sName);
                }
                LOG.info("Ok! (sFieldID={})", sFieldID);
            }
        } catch (Exception oException) {
            LOG.error("FAIL:", oException);
        }
    }

    public static String setStringFromFieldExpression(Expression expression,
                                                      DelegateExecution execution, Object value) {
        if (expression != null && value != null) {
            expression.setValue(value, execution);
        }
        return null;
    }

    /**
     * получаем по задаче ид процесса
     *
     * @param nID_Task ИД-номер таски
     * @return processInstanceId
     */
    public String getProcessInstanceIDByTaskID(String nID_Task) {
        String processInstanceId;
        //сначала ищем в таск сервисе
        Task oTask = oTaskService.createTaskQuery()
                .taskId(nID_Task)
                .singleResult();
        if (oTask != null && oTask.getProcessInstanceId() != null) {
            processInstanceId = oTask.getProcessInstanceId();
        } else {
            //не нашли в таск сервисе ищем в истории
            HistoricTaskInstance historicTaskInstanceQuery = oHistoryService
                    .createHistoricTaskInstanceQuery().taskId(nID_Task)
                    .singleResult();
            processInstanceId = historicTaskInstanceQuery
                    .getProcessInstanceId();
            if (processInstanceId == null) {
                throw new ActivitiObjectNotFoundException(String.format(
                        "ProcessInstanceId for taskId '{%s}' not found.", nID_Task),
                        Attachment.class);
            }
        }
        LOG.debug("getProcessInstanceIDByTaskID processInstanceId={}", processInstanceId);
        return processInstanceId;
    }

    /**
     * Получение процесса по его ИД
     *
     * @param sProcessInstanceID
     * @return ProcessInstance
     */
    public HistoricProcessInstance getProcessInstancyByID(String sProcessInstanceID) {
        HistoricProcessInstance processInstance = oHistoryService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(sProcessInstanceID).includeProcessVariables()
                .singleResult();
        if (processInstance == null) {
            throw new ActivitiObjectNotFoundException(String.format(
                    "ProcessInstance for processInstanceId '{%s}' not found.",
                    sProcessInstanceID), Attachment.class);
        }
        return processInstance;
    }

    /**
     * Получение данных о процессе по Таске
     *
     * @param nID_Task - номер-ИД таски
     * @return DTO-объект ProcessDTOCover
     */
    public ProcessDTOCover getProcessInfo(Long nID_Process, Long nID_Task, String sID_Order) throws CRCInvalidException, RecordNotFoundException {
        LOG.info("start process getting Task Data by nID_Task = {}", nID_Task);
        if (nID_Task == null) {
            nID_Task = getTaskIDbyProcess(nID_Process, sID_Order, Boolean.FALSE);
        }

        HistoricTaskInstance oHistoricTaskInstance = oHistoryService.createHistoricTaskInstanceQuery()
                .taskId(nID_Task.toString()).singleResult();

        String sBP = oHistoricTaskInstance.getProcessDefinitionId();
        LOG.info("id-бизнес-процесса (БП) sBP={}", sBP);

        ProcessDefinition ProcessDefinition = oRepositoryService.createProcessDefinitionQuery()
                .processDefinitionId(sBP).singleResult();

        String sName = ProcessDefinition.getName();
        LOG.info("название услуги (БП) sName={}", sName);

        String sProcessInstanceId = oHistoricTaskInstance.getProcessInstanceId();
        Long nID = null;
        String sPlace = "";
        String sDateCreate = "";
        String sDateClose = "";

        HistoricProcessInstance historicProcessInstance = null;
        if (nID_Process != null) {
            nID = nID_Process;
        } else if (sProcessInstanceId == null || sProcessInstanceId.equals("")) {
            LOG.info("Can't get getProcessInstanceId from HistoricTaskInstance by TaskId");
        } else {
            nID = Long.valueOf(sProcessInstanceId);
        }

        if (nID != null) {
            LOG.info("id процесса (nID={})", nID.toString());
            historicProcessInstance = oHistoryService.createHistoricProcessInstanceQuery().
                    processInstanceId(sProcessInstanceId).
                    includeProcessVariables().singleResult();
            if (historicProcessInstance != null) {
                if (historicProcessInstance.getProcessVariables() != null && historicProcessInstance.getProcessVariables().containsKey("sPlace")) {
                    sPlace = (String) historicProcessInstance.getProcessVariables().get("sPlace");
                } else {
                    sPlace = "";
                }
                LOG.info("Found process instance with variables. sPlace {}", sPlace);
                sPlace = sPlace + " ";

                DateTimeFormatter oDateTimeFormatter = JsonDateTimeSerializer.DATETIME_FORMATTER;
                Date oDateCreate = historicProcessInstance.getStartTime();
                sDateCreate = oDateTimeFormatter.print(oDateCreate.getTime());
                LOG.info("дата создания процесса sDateCreate={}", sDateCreate);
                Date oDateClose = historicProcessInstance.getEndTime();
                sDateClose = oDateClose == null ? null : oDateTimeFormatter.print(oDateClose.getTime());
                LOG.info("дата окончания процесса sDateClose={}", sDateClose);
            } else {
                LOG.info("HistoricProcessInstance not found");
                ProcessInstanceHistoryLog oProcessInstanceHistoryLog = oHistoryService.createProcessInstanceHistoryLogQuery(sProcessInstanceId).singleResult();
                if (oProcessInstanceHistoryLog != null) {
                    DateTimeFormatter oDateTimeFormatter = JsonDateTimeSerializer.DATETIME_FORMATTER;
                    Date oDateCreate = oProcessInstanceHistoryLog.getStartTime();
                    sDateCreate = oDateTimeFormatter.print(oDateCreate.getTime());
                    LOG.info("дата создания процесса sDateCreate={}", sDateCreate);
                    Date oDateClose = oProcessInstanceHistoryLog.getEndTime();
                    sDateClose = oDateClose == null ? null : oDateTimeFormatter.print(oDateClose.getTime());
                    LOG.info("дата окончания процесса sDateClose={}", sDateClose);
                } else {
                    LOG.info("ProcessInstanceHistoryLog not found");
                }
            }
        }

        ProcessDTOCover oProcess = new ProcessDTOCover(sPlace + sName, sBP, nID, sDateCreate, sDateClose);
        LOG.info("Created ProcessDTOCover={}", oProcess.toString());

        return oProcess;
    }

    /**
     * Получение полей стартовой формы по ID таски
     *
     * @param nID_Task номер-ИД таски, для которой нужно найти процесс и вернуть
     *                 поля его стартовой формы.
     * @return
     * @throws RecordNotFoundException
     */
    public Map<String, Object> getStartFormData(Long nID_Task) throws RecordNotFoundException {
        Map<String, Object> mReturn = new HashMap();
        HistoricTaskInstance oHistoricTaskInstance = oHistoryService.createHistoricTaskInstanceQuery()
                .taskId(nID_Task.toString()).singleResult();
        LOG.info("(oHistoricTaskInstance={})", oHistoricTaskInstance);
        if (oHistoricTaskInstance != null) {
            String snID_Process = oHistoricTaskInstance.getProcessInstanceId();
            LOG.info("(snID_Process={})", snID_Process);
            List<HistoricDetail> aHistoricDetail = null;
            if (snID_Process != null) {
                aHistoricDetail = oHistoryService.createHistoricDetailQuery().formProperties()
                        .processInstanceId(snID_Process).list();
            }
            LOG.info("(aHistoricDetail={})", aHistoricDetail);
            if (aHistoricDetail == null) {
                throw new RecordNotFoundException("aHistoricDetail");
            }
            for (HistoricDetail oHistoricDetail : aHistoricDetail) {
                HistoricFormProperty oHistoricFormProperty = (HistoricFormProperty) oHistoricDetail;
                mReturn.put(oHistoricFormProperty.getPropertyId(), oHistoricFormProperty.getPropertyValue());
            }
        } else {
            HistoricProcessInstance oHistoricProcessInstance = oHistoryService.createHistoricProcessInstanceQuery().processInstanceId(nID_Task.toString()).singleResult();
            LOG.info("(oHistoricProcessInstance={})", oHistoricProcessInstance);
            List<Task> activeTasks = null;
            TaskQuery taskQuery = oTaskService.createTaskQuery();
            taskQuery.taskId(nID_Task.toString());
            activeTasks = taskQuery.list();//.active()
            LOG.info("(nID_Task={})", nID_Task);
            if (activeTasks.isEmpty()) {
                taskQuery = oTaskService.createTaskQuery();
                LOG.info("1)activeTasks.isEmpty()");
                taskQuery.processInstanceId(nID_Task.toString());
                activeTasks = taskQuery.list();//.active()
                if (activeTasks.isEmpty() && oHistoricProcessInstance != null) {
                    taskQuery = oTaskService.createTaskQuery();
                    LOG.info("2)activeTasks.isEmpty()(oHistoricProcessInstance.getId()={})", oHistoricProcessInstance.getId());
                    taskQuery.processInstanceId(oHistoricProcessInstance.getId());
                    activeTasks = taskQuery.list();//.active()
                }
            }
            for (Task currTask : activeTasks) {
                TaskFormData data = oFormService.getTaskFormData(currTask.getId());
                if (data != null) {
                    LOG.info("Found TaskFormData for task {}.", currTask.getId());
                    for (FormProperty property : data.getFormProperties()) {
                        mReturn.put(property.getId(), property.getValue());
                    }
                } else {
                    LOG.info("Not found TaskFormData for task {}. Skipping from processing.", currTask.getId());
                }
            }
        }
        return mReturn;
    }

    public Map<String, Object> getProcessVariableValue(String nProcessID, String variableName) throws RecordNotFoundException {
        Map<String, Object> res = new HashMap<>();

        HistoricVariableInstance historicVariableInstance = oHistoryService.createHistoricVariableInstanceQuery().processInstanceId(nProcessID).variableName(variableName).singleResult();

        LOG.info("Retreived HistoricVariableInstance for process {} with value {}", nProcessID, historicVariableInstance);
        if (historicVariableInstance != null) {
            res.put(historicVariableInstance.getVariableName(), historicVariableInstance.getValue());
        }

        return res;
    }

    public boolean deleteProcess(Long nID_Order, String sLogin, String sReason) throws Exception {
        String snID_Process = String.valueOf(ToolLuna.getValidatedOriginalNumber(nID_Order));
        return deleteProcess(snID_Process, sLogin, sReason);
    }

    public boolean deleteProcess(String snID_Process, String sLogin, String sReason) throws Exception {
        boolean success;
        //String nID_Process;

        //nID_Process = String.valueOf(ToolLuna.getValidatedOriginalNumber(nID_Order));
        //String sID_Order = oGeneralConfig.getOrderId_ByOrder(nID_Order);
        String sID_Order = oGeneralConfig.getOrderId_ByProcess(Long.valueOf(snID_Process));

        HistoryEvent_Service_StatusType oStatusType = HistoryEvent_Service_StatusType.REMOVED;
        String statusType_Name = oStatusType.getsName_UA();
        String sBody = statusType_Name;
        //        String sID_status = "Заявка была удалена";
        if (sLogin != null) {
            sBody += " (" + sLogin + ")";
        }
        if (sReason != null) {
            sBody += ": " + sReason;
        }
        Map<String, String> mParam = new HashMap<>();
        mParam.put("nID_StatusType", oStatusType.getnID() + "");
        mParam.put("sBody", sBody);
        LOG.info("Deleting process {}: {}", snID_Process, statusType_Name);
        oHistoryEventService.updateHistoryEvent(
                sID_Order, statusType_Name, false, oStatusType, mParam);
        try {
            oRuntimeService.deleteProcessInstance(snID_Process, sReason);
        } catch (ActivitiObjectNotFoundException e) {
            LOG.error("Could not find process {} to delete: {}", snID_Process, e);
        }
        success = true;
        return success;
    }

    public boolean deleteProcessSimple(String snID_Process, String sLogin, String sReason) throws Exception {
        boolean bOk = false;
        LOG.info("Deleting process snID_Process={}, sLogin={}, sReason={}", snID_Process, sLogin, sReason);
        try {
            oRuntimeService.deleteProcessInstance(snID_Process, sReason);
        } catch (ActivitiObjectNotFoundException e) {
            LOG.info("Could not find process {} to delete: {}", snID_Process, e);
            throw new RecordNotFoundException();
        }
        bOk = true;
        return bOk;
    }

    /**
     * Загрузка задач из Activiti
     *
     * @param sAssignee - ID авторизированого субъекта
     * @return
     */
    public List<TaskAssigneeI> getTasksByAssignee(String sAssignee) {
        List<Task> tasks = oTaskService.createTaskQuery().taskAssignee(sAssignee).list();
        List<TaskAssigneeI> facadeTasks = new ArrayList<>();
        TaskAssigneeCover adapter = new TaskAssigneeCover();
        for (Task task : tasks) {
            facadeTasks.add(adapter.apply(task));
        }
        return facadeTasks;
    }

    public List<TaskAssigneeI> getTasksByAssigneeGroup(String sGroup) {
        List<Task> tasks = oTaskService.createTaskQuery().taskCandidateGroup(sGroup).list();
        List<TaskAssigneeI> facadeTasks = new ArrayList<>();
        TaskAssigneeCover adapter = new TaskAssigneeCover();
        for (Task task : tasks) {
            facadeTasks.add(adapter.apply(task));
        }
        return facadeTasks;
    }

    /**
     * Поиск nID_Task по nID_Process (process instance id) независимо от того,
     * активный этот процесс либо уже находится в архиве
     *
     * @param nID_Process
     */
    public List<String> findTaskIDsByActiveAndHistoryProcessInstanceID(Long nID_Process) {
        List<String> result = new ArrayList<>();
        List<Task> aTask = null;
        List<HistoricTaskInstance> aHistoricTask = null;
        aTask = oTaskService.createTaskQuery().processInstanceId(nID_Process.toString()).list();
        if (aTask == null || aTask.isEmpty()) {
            LOG.info(String.format("Tasks for active Process Instance [id = '%s'] not found", nID_Process));
            aHistoricTask = oHistoryService.createHistoricTaskInstanceQuery().processInstanceId(nID_Process.toString()).list();
            if (aHistoricTask == null || aHistoricTask.isEmpty()) {
                LOG.warn(String.format("Tasks for Process Instance [id = '%s'] not found", nID_Process));
            }
            for (HistoricTaskInstance historicTask : aHistoricTask) {
                result.add(historicTask.getId());
                LOG.info(String.format("Historic Task [id = '%s'] is found", historicTask.getId()));
            }
            LOG.info("Tasks for historic process instance: " + result.toString());
        }
        if (result.isEmpty()) {
            for (Task task : aTask) {
                result.add(task.getId());
                LOG.info(String.format("Task [id = '%s'] is found", task.getId()));
            }
            LOG.info("Tasks for process instance: " + result.toString());
        }
        return result;
    }

    /**
     * Поиск nID_Task из активного или завершенного процесса
     *
     * @param nID_Process         - process instance ID
     * @param sID_Order
     * @param bIsFirstCreatedTask -- true - для поиска Task с более ранней датой
     *                            создания false - для поиска Task с более поздней датой создания
     */
    public Long getTaskIDbyProcess(Long nID_Process, String sID_Order, Boolean bIsFirstCreatedTask)
            throws CRCInvalidException, RecordNotFoundException {
        Long nID_Task;
        ArrayList<String> taskIDsList = new ArrayList<>();
        List<String> resultTaskIDs = null;
        if (sID_Order != null && !sID_Order.isEmpty() && !sID_Order.equals("")) {
            LOG.info("start process getting Task Data by sID_Order={}", sID_Order);
            Long ProtectedID = getIDProtectedFromIDOrder(sID_Order);
            String snID_Process = getOriginalProcessInstanceId(ProtectedID);
            nID_Process = Long.parseLong(snID_Process);
            resultTaskIDs = findTaskIDsByActiveAndHistoryProcessInstanceID(nID_Process);
        } else if (nID_Process != null) {
            LOG.info("start process getting Task Data by nID_Process={}", nID_Process);
            resultTaskIDs = findTaskIDsByActiveAndHistoryProcessInstanceID(nID_Process);
        } else {
            String massege = "All request param is NULL";
            LOG.info(massege);
            throw new RecordNotFoundException(massege);
        }
        for (String taskID : resultTaskIDs) {
            taskIDsList.add(taskID);
        }

        if (taskIDsList.isEmpty()) {
            throw new RuntimeException("There is no active task for process " + nID_Process);
        }

        String task = taskIDsList.get(0);

        if (taskIDsList.size() > 1) {
            LOG.info("Result tasks list size: " + taskIDsList.size());
            if (bIsFirstCreatedTask) {
                LOG.info("Searching Task with an earlier creation date");
            } else {
                LOG.info("Searching Task with an later creation date");
            }

            Date createDateTask, createDateTaskOpponent;

            createDateTask = getTaskDateTimeCreate(Long.parseLong(task));
            LOG.info(String.format("Task create date: ['%s']",
                    JsonDateTimeSerializer.DATETIME_FORMATTER.print(createDateTask.getTime())));

            String taskOpponent;
            for (String taskID : taskIDsList) {
                taskOpponent = taskID;
                LOG.info(String.format("Task [id = '%s'] is detect", taskID));

                createDateTaskOpponent = getTaskDateTimeCreate(Long.parseLong(taskID));

                if (bIsFirstCreatedTask) {
                    if (createDateTask.after(createDateTaskOpponent)) {
                        task = taskOpponent;
                        LOG.info(String.format("Set new result Task [id = '%s']", task));
                    }
                } else if (createDateTask.before(createDateTaskOpponent)) {
                    task = taskOpponent;
                    LOG.info(String.format("Set new result Task [id = '%s']", task));
                }
            }
        }
        nID_Task = Long.parseLong(task);
        LOG.info(String.format("Task [id = '%s'] is found", nID_Task));
        return nID_Task;
    }

    /**
     * Ищет таску среди активных и архивных и возвращает дату ее создания (поиск
     * сначала происходит среди активных Тасок, если не удается найти - ищет в
     * архивных)
     *
     * @param nID_Task - ИД таски
     * @return - результат метода Таски getCreateTime()
     * @throws RecordNotFoundException - в случая не возможности найти заданный
     *                                 ИД среди архивных тасок
     */
    public Date getTaskDateTimeCreate(Long nID_Task) throws RecordNotFoundException {
        Date result;
        String taskID = nID_Task.toString();
        try {
            result = oTaskService.createTaskQuery().taskId(taskID).singleResult().getCreateTime();
        } catch (NullPointerException e) {
            LOG.info(String.format("Must search Task [id = '%s'] in history!!!", taskID));
            try {
                result = oHistoryService.createHistoricTaskInstanceQuery().taskId(taskID).singleResult().getCreateTime();
            } catch (NullPointerException e1) {
                throw new RecordNotFoundException(String.format("Task [id = '%s'] not faund", taskID));
            }
        }
        LOG.info("Task id = "
                + nID_Task
                + " is created on: "
                + JsonDateTimeSerializer.DATETIME_FORMATTER.print(result.getTime()));
        return result;
    }

    /**
     * Ищет таску среди активных и архивных и возвращает ее имя или статус
     * (поиск сначала происходит среди активных Тасок, если не удается найти -
     * ищет в архивных)
     *
     * @param nID_Task - ИД таски
     * @return - результат метода Таски getName()
     * @throws RecordNotFoundException - в случая не возможности найти заданный
     *                                 ИД среди архивных тасок
     */
    public String getTaskName(Long nID_Task) throws RecordNotFoundException {
        String result;
        String taskID = nID_Task.toString();
        try {
            result = oTaskService.createTaskQuery().taskId(taskID).singleResult().getName();
        } catch (NullPointerException e) {
            LOG.info(String.format("Must search Task [id = '%s'] in history!!!", taskID));
            try {
                result = oHistoryService.createHistoricTaskInstanceQuery().taskId(taskID).singleResult().getName();
            } catch (NullPointerException e1) {
                throw new RecordNotFoundException(String.format("Task [id = '%s'] not faund", taskID));
            }
        }
        LOG.info("Task id = "
                + nID_Task
                + "; name is: "
                + result);
        return result;
    }

    /**
     * Ищет таску среди активных и архивных и возвращает ее имя или статус
     * (поиск сначала происходит среди активных Тасок, если не удается найти -
     * ищет в архивных)
     *
     * @param nID_Task - ИД таски
     * @return - результат метода Таски getName()
     * @throws RecordNotFoundException - в случая не возможности найти заданный
     *                                 ИД среди архивных тасок
     */
    public Map<String, String> getTaskData(Long nID_Task) throws RecordNotFoundException {
        SimpleDateFormat oDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Map<String, String> m = new HashMap();
        //String result;
        String snID_Task = nID_Task.toString();
        try {
            //result = oTaskService.createTaskQuery().taskId(snID_Task).singleResult().getName();
            //m.put("sDateEnd", oActionTaskService.getsIDUserTaskByTaskId(nID_Task));
            Task oTask = oTaskService.createTaskQuery().taskId(snID_Task).singleResult();
            m.put("sLoginAssigned", oTask.getAssignee());
            //m.put("sDateEnd", oDateFormat.format(oTask.getCreateTime()));
            /*return oHistoryService.createHistoricTaskInstanceQuery()
                .taskId(nID_Task.toString()).singleResult().getTaskDefinitionKey();
             */
            //taskInfo.put("createTime", oDateFormat.format(task.getCreateTime()));

        } catch (NullPointerException e) {
            LOG.info(String.format("Must search Task [id = '%s'] in history!!!", snID_Task));
            try {
                //oTask = oHistoryService.createHistoricTaskInstanceQuery().taskId(snID_Task).singleResult().getName();
                HistoricTaskInstance oTask = oHistoryService.createHistoricTaskInstanceQuery().taskId(snID_Task).singleResult();
                m.put("sLoginAssigned", oTask.getAssignee());
                m.put("sDateEnd", oDateFormat.format(oTask.getCreateTime()));
            } catch (NullPointerException e1) {
                throw new RecordNotFoundException(String.format("Task [id = '%s'] not faund", snID_Task));
            }
        }
        LOG.info("Task id = " + nID_Task + "; m = " + m);
        return m;
    }

    public List<Map<String, Object>> getFormPropertiesMapByTaskID(Long nID_Task) {
        List<FormProperty> a = oFormService.getTaskFormData(nID_Task.toString()).getFormProperties();
        List<Map<String, Object>> aReturn = new LinkedList();
        Map<String, Object> mReturn;
        //a.get(1).getType().getInformation()
        for (FormProperty oProperty : a) {
            mReturn = new HashMap();
            //String sValue = "";
            //String sValue = oProperty.getValue();
            mReturn.put("sValue", oProperty.getValue());
            String sType = oProperty.getType() != null ? oProperty.getType().getName() : "";
            mReturn.put("sType", sType);
            mReturn.put("sID", oProperty.getId());
            mReturn.put("sName", oProperty.getName());
            mReturn.put("bReadable", oProperty.isReadable());
            mReturn.put("bWritable", oProperty.isWritable());
            mReturn.put("bRequired", oProperty.isRequired());
            if ("enum".equalsIgnoreCase(sType)) {
                Object oEnums = oProperty.getType().getInformation("values");
                if (oEnums instanceof Map) {
                    Map<String, String> mEnum = (Map) oEnums;
                    mReturn.put("mEnum", mEnum);

                }

            }

            aReturn.add(mReturn);
        }
        return aReturn;
    }

    public List<FormProperty> getFormPropertiesByTaskID(Long nID_Task) {
        List<FormProperty> a = oFormService.getTaskFormData(nID_Task.toString()).getFormProperties();
        //a.get(1).getType().getInformation()
        return a;
    }

    /**
     * Получение массива полей propertyId и propertyValue из
     * HistoricFormProperty
     *
     * @param nID_Task - ID-номер таски, которая находится в архиве
     * @return
     * @throws RecordNotFoundException
     */
    public List<Map<String, String>> getHistoricFormPropertiesByTaskID(Long nID_Task) throws RecordNotFoundException {
        List<Map<String, String>> aReturn = new ArrayList<>();
        List<HistoricDetail> aHistoricDetail = oHistoryService.createHistoricDetailQuery().taskId(nID_Task.toString()).formProperties().list();
        LOG.info("(aHistoricDetail={})", aHistoricDetail);
        if (aHistoricDetail == null) {
            throw new RecordNotFoundException("aHistoricDetail");
        }
        for (HistoricDetail oHistoricDetail : aHistoricDetail) {
            Map<String, String> mReturn = new HashMap<>();
            HistoricFormProperty oHistoricFormProperty = (HistoricFormProperty) oHistoricDetail;
            mReturn.put("sID", oHistoricFormProperty.getPropertyId());
            mReturn.put("sValue", oHistoricFormProperty.getPropertyValue());
            aReturn.add(mReturn);
        }
//        LOG.info("(List oHistoricFormPropertyCover = {})", aReturn);
        return aReturn;
    }

    /**
     * Проверка наличия полей электронной очереди и парсинг их контекста
     *
     * @param aFormProperties
     * @return
     */
    public Map<String, Object> getQueueData(List<FormProperty> aFormProperties) {
        Map<String, Object> result = null;
        List<FormProperty> aFormPropertiesQueueDataType = new ArrayList<>();
        if (aFormProperties == null || aFormProperties.isEmpty()) {
            LOG.info("List<FormProperty> is NULL");

        } else {
            for (FormProperty oFormProperty : aFormProperties) {
                if (oFormProperty.getType() instanceof QueueDataFormType) {
                    aFormPropertiesQueueDataType.add(oFormProperty);
                }
            }
        }

        if (aFormPropertiesQueueDataType.isEmpty()) {
            LOG.info("The array does not contain elements of the QueueData");
        } else {
            result = new HashMap<>();
            for (FormProperty field : aFormPropertiesQueueDataType) {
                result.put(field.getType().getName(), parseQueueDataFromFormProperty(field));
            }
        }
        LOG.info("getQueueData result = {}", result);
        return result;
    }

    private Map<String, Object> parseQueueDataFromFormProperty(FormProperty oFormProperty) {
        Map<String, Object> mItemReturn = new HashMap<>();
        Map<String, Object> mPropertyReturn = new HashMap<>();
        String sValue = oFormProperty.getValue();
        LOG.info("sValue = {}", sValue);
        Map<String, Object> m = QueueDataFormType.parseQueueData(sValue);
        String sDate = (String) m.get(QueueDataFormType.sDate);
        LOG.info("(sDate={})" + sDate);
        String sID_Type = QueueDataFormType.get_sID_Type(m);
        LOG.info("(sID_Type={})", sID_Type);
        if (bFlowOut(sID_Type)) {
            //}else if("iGov".equals(sID_Type)){
            String snID_ServiceCustomPrivate = m.get("nID_ServiceCustomPrivate") + "";
            LOG.info("(nID_ServiceCustomPrivate={})", snID_ServiceCustomPrivate);
            String sTicket_Number = (String) m.get("ticket_number");
            LOG.info("(sTicket_Number={})", sTicket_Number);
            String sTicket_Code = (String) m.get("ticket_code");
            LOG.info("(sTicket_Code={})", sTicket_Code);
            //element.put("nID_FlowSlotTicket", sTicket_Number);
            mPropertyReturn.put("snID_ServiceCustomPrivate", snID_ServiceCustomPrivate);
            mPropertyReturn.put("sTicket_Number", sTicket_Number);
            mPropertyReturn.put("sTicket_Code", sTicket_Code);
        } else {
            Long nID_FlowSlotTicket = QueueDataFormType.get_nID_FlowSlotTicket(m);
            LOG.info("(nID_FlowSlotTicket={})", nID_FlowSlotTicket);
            mPropertyReturn.put("nID_FlowSlotTicket", nID_FlowSlotTicket);
        }
        mPropertyReturn.put("sDate", sDate);
        mItemReturn.put(oFormProperty.getId(), mPropertyReturn);
        return mItemReturn;
    }

    /**
     * Получение массива отождествленных групп по Task
     *
     * @param nID_Task - Task ID
     * @return - CandidateGroup from ProcessDefinition by Task
     */
    private Set<String> getCandidateGroupByTaskID(Long nID_Task) {
        Set<String> aCandidateGroup = new HashSet<>();
        ProcessDefinition processDefinition = getProcessDefinitionByTaskID(nID_Task.toString());
        loadCandidateGroupsFromTasks(processDefinition, aCandidateGroup);
        return aCandidateGroup;
    }

    /**
     * Возвращает список объектов Attachment, привязанных к таске
     *
     * @param nID_Task - ИД-номер таски
     * @return
     */
    public List<Attachment> getAttachmentsByTaskID(Long nID_Task) {
        LOG.info(String.format("Start load Attachment object by Task [id = '%s']", nID_Task));
        List<Attachment> attachments = oTaskService.getTaskAttachments(nID_Task.toString());
        if (attachments.isEmpty()) {
            LOG.info(String.format("No attachments in the Task [id = '%s']", nID_Task));
        } else {
            List<String> attachmetIDs = new ArrayList<>();
            int index = 0;
            for (Attachment attachment : attachments) {
                if (attachment.getDescription() == null || attachment.getDescription().equals("")) {
                    attachment.setDescription("Завантажений файл " + (++index));
                }
                attachmetIDs.add(attachment.getId());
            }
            LOG.info("Task attachments: " + attachmetIDs.toString());
        }
        return attachments;
    }

    /**
     * @param taskQuery
     * @param nStart
     * @param nSize
     * @param bFilterHasTicket
     * @param mapOfTickets
     * @return
     */
    public List<TaskInfo> getTasksWithTicketsFromQuery(Object taskQuery, int nStart, int nSize, boolean bFilterHasTicket, Map<String, FlowSlotTicket> mapOfTickets) {
        List<TaskInfo> tasks = (taskQuery instanceof TaskInfoQuery) ? ((TaskInfoQuery) taskQuery).listPage(nStart, nSize)
                : (List) ((NativeTaskQuery) taskQuery).listPage(nStart, nSize);

        List<Long> taskIds = new LinkedList<>();
        for (int i = 0; i < tasks.size(); i++) {
            TaskInfo currTask = tasks.get(i);
            if (currTask.getProcessInstanceId() != null) {
                taskIds.add(Long.valueOf(currTask.getProcessInstanceId()));
            }
        }
        LOG.info("Preparing to select flow slot tickets. taskIds:{}", taskIds.toString());
        List<FlowSlotTicket> tickets = new LinkedList<>();
        if (taskIds.isEmpty()) {
            return tasks;
        }
        try {
            tickets = oFlowSlotTicketDao.findAllByInValues("nID_Task_Activiti", taskIds);
        } catch (Exception e) {
            LOG.error("Error occured while getting tickets for tasks", e);
        }
        LOG.info("Found {} tickets for specified list of tasks IDs", tickets.size());
        if (tickets != null) {
            for (FlowSlotTicket ticket : tickets) {
                mapOfTickets.put(ticket.getnID_Task_Activiti().toString(), ticket);
            }
        }
        if (bFilterHasTicket) {
            LOG.info("Removing tasks which don't have flow slot tickets");
            Iterator<TaskInfo> iter = tasks.iterator();
            while (iter.hasNext()) {
                TaskInfo curr = iter.next();
                if (!mapOfTickets.keySet().contains(curr.getProcessInstanceId())) {
                    LOG.info("Removing tasks with ID {}", curr.getId());
                    iter.remove();
                }
            }
        }
        return tasks;
    }

    public List<TaskInfo> matchTasksWithTicketsFromQuery(final String sLogin, boolean bIncludeAlienTickets, String sFilterStatus,
                                                         List<TaskInfo> tasks) {

        final List<Long> taskIds = new LinkedList<>();
        for (int i = 0; i < tasks.size(); i++) {
            TaskInfo currTask = tasks.get(i);
            if (currTask.getProcessInstanceId() != null) {
                taskIds.add(Long.valueOf(currTask.getProcessInstanceId()));
            }
        }
        LOG.info("Preparing to select flow slot tickets. taskIds:{}", taskIds.toString());
        if (taskIds.isEmpty()) {
            return tasks;
        }
        SerializableResponseEntity<ArrayList<FlowSlotTicket>> entities = oCachedInvocationBean
                .invokeUsingCache(new CachedInvocationBean.Callback<SerializableResponseEntity<ArrayList<FlowSlotTicket>>>(
                        GET_ALL_TICKETS_CACHE, sLogin, bIncludeAlienTickets, sFilterStatus) {
                    @Override
                    public SerializableResponseEntity<ArrayList<FlowSlotTicket>> execute() {
                        LOG.info("Loading tickets from cache for user {}", sLogin);

                        ArrayList<FlowSlotTicket> res = (ArrayList<FlowSlotTicket>) oFlowSlotTicketDao.findAllByInValues("nID_Task_Activiti", taskIds);

                        return new SerializableResponseEntity<>(new ResponseEntity<>(res, null, HttpStatus.OK));
                    }
                });
        ArrayList<FlowSlotTicket> tickets = entities.getBody();
        LOG.info("Found {} tickets for specified list of tasks IDs", tickets.size());
        Map<String, FlowSlotTicket> mapOfTickets = new HashMap<>();
        if (tickets != null) {
            for (FlowSlotTicket ticket : tickets) {
                mapOfTickets.put(ticket.getnID_Task_Activiti().toString(), ticket);
            }
        }
        LOG.info("Removing tasks which don't have flow slot tickets");
        LinkedList<TaskInfo> res = new LinkedList<>();
        Iterator<TaskInfo> iter = tasks.iterator();
        while (iter.hasNext()) {
            TaskInfo curr = iter.next();
            if (mapOfTickets.keySet().contains(curr.getProcessInstanceId())) {
                LOG.info("Adding tasks with ID {} to the response", curr.getId());
                res.add(curr);
            }
        }
        return res;
    }

    public long getCountOfTasksForGroups(List<String> groupsIds) {
        StringBuilder groupIdsSB = new StringBuilder();
        for (int i = 0; i < groupsIds.size(); i++) {
            groupIdsSB.append("'");
            groupIdsSB.append(groupsIds.get(i));
            groupIdsSB.append("'");
            if (i < groupsIds.size() - 1) {
                groupIdsSB.append(",");
            }
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT count(task.*) FROM ACT_RU_TASK task, ACT_RU_IDENTITYLINK link WHERE task.ID_ = link.TASK_ID_ AND link.GROUP_ID_ IN(");
        sql.append(groupIdsSB.toString());
        sql.append(") ");

        LOG.info("sql query {}", sql);

        return oTaskService.createNativeTaskQuery().sql(sql.toString()).count();
    }

    public void populateResultSortedByTasksOrder(boolean bFilterHasTicket,
                                                 List<?> tasks, Map<String, FlowSlotTicket> mapOfTickets,
                                                 List<Map<String, Object>> data) {

        LOG.info("populateResultSortedByTasksOrder. number of tasks:{} number of tickets:{} ", tasks.size(), mapOfTickets.size());
        for (int i = 0; i < tasks.size(); i++) {
            try {
                TaskInfo task = (TaskInfo) tasks.get(i);
                Map<String, Object> taskInfo = populateTaskInfo(task, mapOfTickets.get(task.getProcessInstanceId()));
                data.add(taskInfo);
            } catch (Exception e) {
                LOG.error("error: Error while populatiing task", e);
            }
        }
    }

    public void populateResultSortedByTicketDate(boolean bFilterHasTicket, List<?> tasks,
                                                 Map<String, FlowSlotTicket> mapOfTickets, List<Map<String, Object>> data) {
        LOG.info("Sorting result by flow slot ticket create date. Number of tasks:{} number of tickets:{}", tasks.size(), mapOfTickets.size());

        List<FlowSlotTicket> tickets = new LinkedList<>();
        tickets.addAll(mapOfTickets.values());
        Collections.sort(tickets, FLOW_SLOT_TICKET_ORDER_CREATE_COMPARATOR);
        LOG.info("Sorted tickets by order create date");

        Map<String, TaskInfo> tasksMap = new HashMap<>();
        for (int i = 0; i < tasks.size(); i++) {
            TaskInfo task = (TaskInfo) tasks.get(i);
            tasksMap.put(((TaskInfo) tasks.get(i)).getProcessInstanceId(), task);
        }

        for (int i = 0; i < tickets.size(); i++) {
            try {
                FlowSlotTicket ticket = tickets.get(i);
                TaskInfo task = tasksMap.get(ticket.getnID_Task_Activiti());
                Map<String, Object> taskInfo = populateTaskInfo(task, ticket);
                data.add(taskInfo);
            } catch (Exception e) {
                LOG.error("error: ", e);
            }
        }
    }

    public List<TaskInfo> returnTasksFromCache(final String sLogin, final String sFilterStatus, final boolean bIncludeAlienAssignedTasks,
                                               final List<String> groupsIds, String soaFilterField) {
        SerializableResponseEntity<ArrayList<TaskInfo>> entity = oCachedInvocationBean
                .invokeUsingCache(new CachedInvocationBean.Callback<SerializableResponseEntity<ArrayList<TaskInfo>>>(
                        GET_ALL_TASK_FOR_USER_CACHE, sLogin, sFilterStatus, bIncludeAlienAssignedTasks) {
                    @Override
                    public SerializableResponseEntity<ArrayList<TaskInfo>> execute() {
                        LOG.info("Loading tasks from cache for user {} with filterStatus {} and bIncludeAlienAssignedTasks {}", sLogin, sFilterStatus, bIncludeAlienAssignedTasks);
                        Object taskQuery = createQuery(sLogin, bIncludeAlienAssignedTasks, null, sFilterStatus, groupsIds, soaFilterField);

                        ArrayList<TaskInfo> res = (ArrayList<TaskInfo>) ((taskQuery instanceof TaskInfoQuery) ? ((TaskInfoQuery) taskQuery).list()
                                : (List) ((NativeTaskQuery) taskQuery).list());

                        LOG.info("Loaded {} tasks", res.size());
                        return new SerializableResponseEntity<>(new ResponseEntity<>(res, null, HttpStatus.OK));
                    }
                });
        LOG.info("Entity {}", entity.toString());
        return entity.getBody();
    }

    public String getTypeOfTask(String sLogin, String sID_Task) {
        long count = 0;
        try {
            count = oTaskService.createTaskQuery().taskCandidateOrAssigned(sLogin).processDefinitionKeyLikeIgnoreCase("_doc_%").taskId(sID_Task).count();
            if (count > 0) {
                return THE_STATUS_OF_TASK_IS_DOCUMENTS;
            }
        } catch (Exception e) {
            //
        }
        try {
            count = oTaskService.createTaskQuery().taskCandidateUser(sLogin).taskId(sID_Task).count();
            if (count > 0) {
                return THE_STATUS_OF_TASK_IS_OPENED_UNASSIGNED;
            }
        } catch (Exception e) {
            //
        }
        try {
            count = oTaskService.createTaskQuery().taskAssignee(sLogin).taskId(sID_Task).count();
            if (count > 0) {
                return THE_STATUS_OF_TASK_IS_OPENED_ASSIGNED;
            }
        } catch (Exception e) {
            //
        }
        try {
            count = oHistoryService.createHistoricTaskInstanceQuery().taskInvolvedUser(sLogin).taskId(sID_Task).finished().count();
            if (count > 0) {
                return THE_STATUS_OF_TASK_IS_CLOSED;
            }
        } catch (Exception e) {
            //
        }
        return "";
    }

    public Object createQuery(String sLogin,
                              boolean bIncludeAlienAssignedTasks, String sOrderBy, String sFilterStatus,
                              List<String> groupsIds, String soaFilterField) {

        if (!StringUtils.isEmpty(soaFilterField)) {
        }
        Object taskQuery;
        if (THE_STATUS_OF_TASK_IS_CLOSED.equalsIgnoreCase(sFilterStatus)) {
            taskQuery = oHistoryService.createHistoricTaskInstanceQuery().taskInvolvedUser(sLogin).finished();
            if ("taskCreateTime".equalsIgnoreCase(sOrderBy)) {
                ((TaskInfoQuery) taskQuery).orderByTaskCreateTime();
            } else {
                ((TaskInfoQuery) taskQuery).orderByTaskId();
            }

            if (!StringUtils.isEmpty(soaFilterField)) {
                JSONArray oJSONArray = new JSONArray(soaFilterField);
                Map<String, String> mFilterField = new HashMap<>();
                for (int i = 0; i < oJSONArray.length(); i++) {
                    JSONObject oJSON = (JSONObject) oJSONArray.get(i);
                    if (oJSON.has("sID") && oJSON.has("sValue")) {
                        mFilterField.put(oJSON.getString("sID"), oJSON.getString("sValue"));
                        ((TaskInfoQuery) taskQuery).processVariableValueEqualsIgnoreCase(oJSON.getString("sID"), oJSON.getString("sValue"));
//
                    } else {
                        LOG.info("{} json element doesn't have either sID or sValue fields", i);
                    }
                }
                LOG.info("Converted filter fields to the map mFilterField={}", mFilterField);
            }
            ((TaskInfoQuery) taskQuery).asc();
        } else if (bIncludeAlienAssignedTasks) {
            StringBuilder groupIdsSB = new StringBuilder();
            for (int i = 0; i < groupsIds.size(); i++) {
                groupIdsSB.append("'");
                groupIdsSB.append(groupsIds.get(i));
                groupIdsSB.append("'");
                if (i < groupsIds.size() - 1) {
                    groupIdsSB.append(",");
                }
            }

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT task.* FROM ACT_RU_TASK task, ACT_RU_IDENTITYLINK link WHERE task.ID_ = link.TASK_ID_ AND link.GROUP_ID_ IN(");
            sql.append(groupIdsSB.toString());
            sql.append(") ");

            if ("taskCreateTime".equalsIgnoreCase(sOrderBy)) {
                sql.append(" order by task.CREATE_TIME_ asc");
            } else {
                sql.append(" order by task.ID_ asc");
            }
            LOG.info("Query to execute {}", sql.toString());
            taskQuery = oTaskService.createNativeTaskQuery().sql(sql.toString());
        } else {
            taskQuery = oTaskService.createTaskQuery();
            long startTime = System.currentTimeMillis();
            if (THE_STATUS_OF_TASK_IS_OPENED_UNASSIGNED.equalsIgnoreCase(sFilterStatus)) {
                ((TaskQuery) taskQuery).taskCandidateUser(sLogin);
            } else if (THE_STATUS_OF_TASK_IS_OPENED_ASSIGNED.equalsIgnoreCase(sFilterStatus)) {
                taskQuery = ((TaskQuery) taskQuery).taskAssignee(sLogin);
            } else if (THE_STATUS_OF_TASK_IS_OPENED.equalsIgnoreCase(sFilterStatus)) {
                taskQuery = ((TaskQuery) taskQuery).taskCandidateOrAssigned(sLogin);
                LOG.info("Opened JSONValue element in filter {}", JSONValue.toJSONString(taskQuery));
            } else if (THE_STATUS_OF_TASK_IS_DOCUMENTS.equalsIgnoreCase(sFilterStatus)) {
                taskQuery = ((TaskQuery) taskQuery).taskCandidateOrAssigned(sLogin).processDefinitionKeyLikeIgnoreCase("_doc_%");
            }
            LOG.info("time: " + sFilterStatus + ": " + (System.currentTimeMillis() - startTime));
            if ("taskCreateTime".equalsIgnoreCase(sOrderBy)) {
                ((TaskQuery) taskQuery).orderByTaskCreateTime();
            } else {
                ((TaskQuery) taskQuery).orderByTaskId();
            }

            if (!StringUtils.isEmpty(soaFilterField)) {
                JSONArray oJSONArray = new JSONArray(soaFilterField);
                Map<String, String> mFilterField = new HashMap<>();
                for (int i = 0; i < oJSONArray.length(); i++) {
                    JSONObject oJSON = (JSONObject) oJSONArray.get(i);
                    if (oJSON.has("sID") && oJSON.has("sValue")) {
                        mFilterField.put(oJSON.getString("sID"), oJSON.getString("sValue"));
                        ((TaskQuery) taskQuery)
                                .processVariableValueEqualsIgnoreCase(oJSON.getString("sID"), oJSON.getString("sValue"));
                        LOG.info("{} json element doesn't have either sID or sValue fields", i);
                    }
                }
                LOG.info("Converted filter fields to the map mFilterField={}", mFilterField);
            }
            ((TaskQuery) taskQuery).asc();
        }

        return taskQuery;
    }

    public Map<String, Object> populateTaskInfo(TaskInfo task, FlowSlotTicket flowSlotTicket) {

        String sPlace = "";

        //Выполняем поиск sPlace только, если процесс начинается на system
        if (task.getProcessDefinitionId().startsWith("system")) {
            HistoricProcessInstance processInstance = oHistoryService.createHistoricProcessInstanceQuery().
                    processInstanceId(task.getProcessInstanceId()).
                    includeProcessVariables().singleResult();

            sPlace = processInstance.getProcessVariables().containsKey("sPlace") ? (String) processInstance.getProcessVariables().get("sPlace") + " " : "";
            LOG.info("Found process instance with variables. sPlace {} taskId {} processInstanceId {}", sPlace, task.getId(), task.getProcessInstanceId());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        Map<String, Object> taskInfo = new HashMap<>();

        taskInfo.put("id", task.getId());
        taskInfo.put("url", oGeneralConfig.getSelfHost() + "/wf/service/runtime/tasks/" + task.getId());
        taskInfo.put("owner", task.getOwner());
        taskInfo.put("assignee", task.getAssignee());
        taskInfo.put("delegationState", (task instanceof Task) ? ((Task) task).getDelegationState() : null);
        taskInfo.put("name", sPlace + task.getName());
        taskInfo.put("description", task.getDescription());
        taskInfo.put("createTime", sdf.format(task.getCreateTime()));
        taskInfo.put("dueDate", task.getDueDate() != null ? sdf.format(task.getDueDate()) : null);
        taskInfo.put("priority", task.getPriority());
        taskInfo.put("suspended", (task instanceof Task) ? ((Task) task).isSuspended() : null);
        taskInfo.put("taskDefinitionKey", task.getTaskDefinitionKey());
        taskInfo.put("tenantId", task.getTenantId());
        taskInfo.put("category", task.getCategory());
        taskInfo.put("formKey", task.getFormKey());
        taskInfo.put("parentTaskId", task.getParentTaskId());
        taskInfo.put("parentTaskUrl", "");
        taskInfo.put("executionId", task.getExecutionId());
        taskInfo.put("executionUrl", oGeneralConfig.getSelfHost() + "/wf/service/runtime/executions/" + task.getExecutionId());
        taskInfo.put("processInstanceId", task.getProcessInstanceId());
        taskInfo.put("processInstanceUrl", oGeneralConfig.getSelfHost() + "/wf/service/runtime/process-instances/" + task.getProcessInstanceId());
        taskInfo.put("processDefinitionId", task.getProcessDefinitionId());
        taskInfo.put("processDefinitionUrl", oGeneralConfig.getSelfHost() + "/wf/service/repository/process-definitions/" + task.getProcessDefinitionId());
        taskInfo.put("variables", new LinkedList());

        if (flowSlotTicket != null) {
            LOG.info("Populating flow slot ticket");
            DateTimeFormatter dtf = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");
            Map<String, Object> flowSlotTicketData = new HashMap<>();
            flowSlotTicketData.put("nID", flowSlotTicket.getId());
            flowSlotTicketData.put("nID_Subject", flowSlotTicket.getnID_Subject());
            flowSlotTicketData.put("sDateStart", flowSlotTicket.getsDateStart() != null ? dtf.print(flowSlotTicket.getsDateStart()) : null);
            flowSlotTicketData.put("sDateFinish", flowSlotTicket.getsDateFinish() != null ? dtf.print(flowSlotTicket.getsDateFinish()) : null);
            taskInfo.put("flowSlotTicket", flowSlotTicketData);
        }

        return taskInfo;
    }

    /**
     * Get sID_UserTask by nID_Task
     *
     * @param nID_Task
     * @return sID_UserTask
     */
    public String getsIDUserTaskByTaskId(Long nID_Task) {

        HistoricTaskInstance oHistoricTaskInctance = oHistoryService.createHistoricTaskInstanceQuery()
                .taskId(nID_Task.toString()).singleResult();
        if (oHistoricTaskInctance != null) {
            return oHistoricTaskInctance.getTaskDefinitionKey();
        } else {
            return null;
        }

    }

    /**
     * Получить список идентификаторов отождествленных групп по таске
     *
     * @param nID_Task - идентификатор таски
     * @see IdentityLink#getGroupId()
     * @see HistoricIdentityLink#getGroupId()
     */
    public Set<String> getGroupIDsByTaskID(Long nID_Task) {
        LOG.info(String.format("Start extraction Group IDs for Task [id=%s]", nID_Task));
        Set<String> result = new HashSet<>();
        try {
            List<IdentityLink> identityLinks = oTaskService.getIdentityLinksForTask(nID_Task.toString());
            for (IdentityLink link : identityLinks) {
                LOG.info(String.format("Extraction Group ID from IdentityLink %s", link.toString()));
                if (link.getGroupId() == null || link.getGroupId().isEmpty()) {
                    LOG.info(String.format("Not found Group in IdentityLink %s", link.toString()));
                } else {
                    result.add(link.getGroupId());
                    LOG.info(String.format("Add Group id=%s for active Task id=%s from IdentityLink %s",
                            link.getGroupId(), nID_Task, link.toString()));
                }
            }
        } catch (NullPointerException e) {
            try {
                List<HistoricIdentityLink> historicIdentityLinks = oHistoryService.getHistoricIdentityLinksForTask(nID_Task.toString());
                for (HistoricIdentityLink link : historicIdentityLinks) {
                    LOG.info(String.format("Extraction Group ID from HistoricIdentityLink %s", link.toString()));
                    if (link.getGroupId() == null || link.getGroupId().isEmpty()) {
                        LOG.info(String.format("Not found Group in HistoricIdentityLink %s", link.toString()));
                    } else {
                        result.add(link.getGroupId());
                        LOG.info(String.format("Add Group id=%s for historic Task id=%s from HistoricIdentityLink %s",
                                link.getGroupId(), nID_Task, link.toString()));
                    }
                }
            } catch (NullPointerException eh) {
                LOG.info(String.format("No found Group id for Task id=%s", nID_Task));
            }
        }

        return result;
    }

    /**
     * Проверяет вхождение пользователя в одну из груп, на которую
     * распространяется тиска
     *
     * @param sLogin   - логгин пользователя
     * @param nID_Task - ИД-номер таски
     * @return true - если пользователь входит в одну из групп; false - если
     * совпадений не найдено.
     */
    public boolean checkAvailabilityTaskGroupsForUser(String sLogin, Long nID_Task) throws NotFoundException {

        ProcessDefinition BP_Task = getProcessDefinitionByTaskID(nID_Task.toString());
        List<ProcessDefinition> aBP_Task = new LinkedList<>();
        aBP_Task.add(BP_Task);

        List<ProcessInfoShortVO> aProcessInfoShortVO = getAvailabilityProcessDefinitionByLogin(sLogin, aBP_Task);

        return CollectionUtils.isNotEmpty(aProcessInfoShortVO);
    }

    public void deleteHistoricProcessInstance(String snID_Process_Activiti) {
        oHistoryService.deleteHistoricProcessInstance(snID_Process_Activiti);
    }

    /**
     * Находим все таски по логину и фильтру.
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return возвращает лист тасок
     */
    public List<TaskInfo> searchTasks(TaskFilterVO oTaskFilterVO) {

        List<TaskInfo> aoResultTasks = new LinkedList<>();
        String sFilterStatus = oTaskFilterVO.getsFilterStatus();
        //вернуть последнюю юзертаску закрытого процесса-документа
        if (sFilterStatus.equals(THE_STATUS_OF_TASK_IS_DOCUMENT_CLOSED)) {
            aoResultTasks.addAll(getDocumentClosed(oTaskFilterVO));

            //выборка из документстепрайт где bWrite=тру или фолс и нет даты подписи
        } else if ((sFilterStatus.equals(THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_UNASSIGNED_UNPROCESSED))) {
            aoResultTasks.addAll(getOpenedUnassignedUnprocessedDocument(oTaskFilterVO));

            //выборка из документстепрайт где  sDate != null && bNeedECP == true && sDateECP == null
        } else if (sFilterStatus.equals(THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_UNASSIGNED_WITHOUTECP)) {
            aoResultTasks.addAll(getOpenedUnassignedWithoutECPDocument(oTaskFilterVO));

            //Выборка из документстепрайт только тех кто есть на дефолтном степе
        } else if (sFilterStatus.equals(THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_UNASSIGNED_PROCESSED)) {
            aoResultTasks.addAll(getOpenedUnassignedProcessedDocument(oTaskFilterVO));

        } else if (sFilterStatus.equals(THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_CREATOR)) {
            aoResultTasks.addAll(getOpenedCreatorDocument(oTaskFilterVO));

        } else if (sFilterStatus.equals(THE_STATUS_OF_TASK_IS_OPENED_ASSIGNED)) {
            aoResultTasks.addAll(getOpenedAssignedTask(oTaskFilterVO));

        } else if (sFilterStatus.equals(THE_STATUS_OF_TASK_IS_OPENED_UNASSIGNED)) {
            aoResultTasks.addAll(getOpenedUnassignedTask(oTaskFilterVO));

        } else if (sFilterStatus.equals(THE_STATUS_OF_TASK_IS_EXECUTION)) {
            aoResultTasks.addAll(getExecutionTask(oTaskFilterVO));

        } else if (sFilterStatus.equals(THE_STATUS_OF_TASK_IS_EXECUTION_FINISHED)) {
            aoResultTasks.addAll(getExecutionFinishedTask(oTaskFilterVO));

        } else if (sFilterStatus.equals(THE_STATUS_OF_TASK_IS_CONTROL)) {
            aoResultTasks.addAll(getControlTask(oTaskFilterVO));

        } else if (sFilterStatus.equals(THE_STATUS_OF_TASK_IS_CONTROL_FINISHED)) {
            aoResultTasks.addAll(getControlFinishedTask(oTaskFilterVO));

        } else if (sFilterStatus.equals(THE_STATUS_OF_TASK_IS_OPENED)) {
            aoResultTasks.addAll(getOpenedTask(oTaskFilterVO));

        } else if (sFilterStatus.equals(THE_STATUS_OF_TASK_IS_CLOSED)) {
            aoResultTasks.addAll(getClosedTask(oTaskFilterVO));

        } else if (sFilterStatus.equals(THE_STATUS_OF_TASK_IS_DOCUMENT_ALL)) {
            aoResultTasks.addAll(getAllDocument(oTaskFilterVO));

        } else if (sFilterStatus.equals(THE_STATUS_OF_TASK_IS_TASK_ALL)) {
            aoResultTasks.addAll(getAllTaskIssue(oTaskFilterVO));
        }

        return aoResultTasks;
    }

    /**
     * Получить все таски-задачи.
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return все закрытые таски для процессов в которых учавствовал sLogin
     */
    private Collection<? extends TaskInfo> getAllTaskIssue(TaskFilterVO oTaskFilterVO) {
        LOG.debug("getAllTaskIssue start with oTaskFilterVO={}", oTaskFilterVO);
        List<TaskInfo> aoAllTaskIssue = new ArrayList<>();
        //получили запрос, который возвращает все закрытые таски
        SqlQueryVO oSqlQueryVO = getAllTaskSql(oTaskFilterVO);
        //добавили ограничение именно для данной вкладки (не документы)
        String sWhere = oSqlQueryVO.getsWhere() + "      AND task.\"proc_def_id_\" NOT LIKE '_doc%'\n ";
        oSqlQueryVO.setsWhere(sWhere);
        //добавляем критерии поиска, если имеются
        oSqlQueryVO = addTaskFilterCriteria(oSqlQueryVO, oTaskFilterVO);

        String sQuery = oSqlQueryVO.getsFrom() + oSqlQueryVO.getsWhere();
        LOG.debug("getClosedTask sQuery={}", sQuery);
        aoAllTaskIssue.addAll(
            filterHistoryTasksByEndTime(
                oHistoryService.createNativeHistoricTaskInstanceQuery().sql(sQuery).list()
            )
        );
        return aoAllTaskIssue;
    }

    /**
     * Получить все таски-документы.
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return все закрытые таски для процессов в которых учавствовал sLogin
     */
    private Collection<? extends TaskInfo> getAllDocument(TaskFilterVO oTaskFilterVO) {
        LOG.debug("getAllDocument start with oTaskFilterVO={}", oTaskFilterVO);
        List<TaskInfo> aoAllDocuments = new ArrayList<>();
        //получили запрос, который возвращает все закрытые таски
        SqlQueryVO oSqlQueryVO = getAllTaskSql(oTaskFilterVO);
        //добавили ограничение именно для данной вкладки (не документы)
        String sWhere = oSqlQueryVO.getsWhere() + "      AND task.\"proc_def_id_\" LIKE '_doc%'\n";
        oSqlQueryVO.setsWhere(sWhere);
        //добавляем критерии поиска, если имеются
        oSqlQueryVO = addTaskFilterCriteria(oSqlQueryVO, oTaskFilterVO);

        String sQuery = oSqlQueryVO.getsFrom() + oSqlQueryVO.getsWhere();
        LOG.debug("getClosedTask sQuery={}", sQuery);
        aoAllDocuments.addAll(
            filterHistoryTasksByEndTime(
                oHistoryService.createNativeHistoricTaskInstanceQuery().sql(sQuery).list()
            )
        );
        return aoAllDocuments;
    }

    public Map<String, Object> getHistoryVariableByHistoryProcessInstanceId(String sProcessInstanceId) {
        LOG.info("getHistoryVariableByHistoryProcessInstanceId started with "
                + "sProcessInstanceId={}", sProcessInstanceId);
        Map<String, Object> mHistoryVariables = new HashMap<>();
        try {
            List<HistoricVariableInstance> aHistoricVariableInstance = oHistoryService
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(sProcessInstanceId)
                    .list();
            if (!aHistoricVariableInstance.isEmpty() && aHistoricVariableInstance != null) {
                aHistoricVariableInstance.forEach(oHistoricVariableInstance -> {
                    String sVariableName = oHistoricVariableInstance.getVariableName();
                    Object oVariableValue = oHistoricVariableInstance.getValue();
                    mHistoryVariables.put(sVariableName, oVariableValue);
                });
            } else {
                LOG.warn("Cant find HistoricVariable.");
            }
        } catch (Exception oException) {
            LOG.error("Error while fetching historic variable instances. Error msg={}", oException.getMessage());
        }

        return mHistoryVariables;
    }

    /**
     * Получить закрытые таски. В
     * act_hi_identitylink узнали процессы в которых учавствует логин, по
     * процессам нашли все закрытые таски.
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return все закрытые таски для процессов в которых учавствовал sLogin
     */
    private List<TaskInfo> getDocumentClosed(TaskFilterVO oTaskFilterVO) {
        LOG.debug("getDocumentClosedTask start with oTaskFilterVO={}", oTaskFilterVO);
        List<TaskInfo> aoDocumentClosedTask = new ArrayList<>();
        //получили запрос, который возвращает все закрытые таски
        SqlQueryVO oSqlQueryVO = getAllTaskSql(oTaskFilterVO);
        //добавили ограничение именно для данной вкладки (только документы)
        String sWhere = oSqlQueryVO.getsWhere() + "      AND task.\"proc_def_id_\" LIKE '_doc%'\n " +
            "      AND process.\"end_time_\" IS NOT NULL AND task.\"end_time_\" IS NOT NULL";
        oSqlQueryVO.setsWhere(sWhere);
        //добавляем критерии поиска, если имеются
        oSqlQueryVO = addTaskFilterCriteria(oSqlQueryVO, oTaskFilterVO);

        String sQuery = oSqlQueryVO.getsFrom() + oSqlQueryVO.getsWhere();
        LOG.debug("getDocumentClosed sQuery={}", sQuery);
        //кладем только последнюю таску процесса
        aoDocumentClosedTask.addAll(
                filterHistoryTasksByEndTime(
                        oHistoryService.createNativeHistoricTaskInstanceQuery().sql(sQuery).list()
                )
        );
        if (oTaskFilterVO.getbSearchExternalTasks()) {
            aoDocumentClosedTask.addAll(getExternalTask(
                    oTaskFilterVO.getsLogin(), DOCUMENT, THE_STATUS_OF_TASK_IS_DOCUMENT_CLOSED, false, null, false));
        }

        return aoDocumentClosedTask;
    }

    /**
     * Получить таски для удаленных процессов в которых учавствовал sLogin. В act_hi_identitylink узнали процессы в
     * которых учавствует логин, по процессам нашли все закрытые таски. "public"."act_hi_procinst"."delete_reason_" =
     * "deleted"
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return все таски для для удаленных процессов в которых учавствовал sLogin
     */
    public List<TaskInfo> getDocumentDeleted(TaskFilterVO oTaskFilterVO) {
        LOG.info("getDocumentDeleted start with oTaskFilterVO={}", oTaskFilterVO);
        List<TaskInfo> aoDocumentDeletedTask = new ArrayList<>();
        String sFrom = "SELECT DISTINCT task.*\n" +
                "FROM \"public\".\"act_hi_taskinst\" task, \"public\".\"act_hi_identitylink\" link,\n" +
                "  \"public\".\"act_hi_procinst\" process";
        String sWhere = " WHERE task.\"proc_inst_id_\" = process.\"proc_inst_id_\"\n" +
                "      AND (task.\"id_\" = link.\"task_id_\" OR process.\"proc_inst_id_\" = link.\"proc_inst_id_\")\n" +
                "      AND (link.\"group_id_\" = '" + oTaskFilterVO.getsLogin() + "' OR link.\"user_id_\" = '" + oTaskFilterVO.getsLogin() + "')\n" +
                "      AND process.\"end_time_\" IS NOT NULL\n" +
                "      AND task.\"end_time_\" IS NOT NULL\n" +
                "      AND process.\"delete_reason_\" = 'deleted'\n";
        String sQuery = sFrom + sWhere;
        //кладем только последнюю таску процесса
        aoDocumentDeletedTask.addAll(
                filterHistoryTasksByEndTime(
                        oHistoryService.createNativeHistoricTaskInstanceQuery().sql(sQuery).list()
                )
        );
        if (oTaskFilterVO.getbSearchExternalTasks()) {
            aoDocumentDeletedTask.addAll(
                    getExternalTask(oTaskFilterVO.getsLogin(), DOCUMENT, THE_STATUS_OF_TASK_IS_DOCUMENT_CLOSED, true, null, false));
        }

        return aoDocumentDeletedTask;
    }

    /**
     * Неотработанные экстренные документы. Выборка из документстепрайт где bWrite=тру или
     * фолс, нет даты подписи и bUrgent = true.
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return все не отработанные экстренные документы
     */
    public List<TaskInfo> getUrgentOpenedUnassignedUnprocessedDocument(TaskFilterVO oTaskFilterVO) {

        List<TaskInfo> aoUrgentUnassignedUnprocessedTask = new ArrayList<>();

        String sQuery = "select * from \"public\".\"act_ru_task\" task where task.\"proc_inst_id_\" \n"
                + "in (select step.\"snID_Process_Activiti\" \n"
                + "from \"public\".\"DocumentStep\" step, \"public\".\"DocumentStepSubjectRight\" docright,\n"
                + "\"public\".\"act_hi_varinst\" varhi, \"public\".\"act_ru_variable\" var\n"
                + "where docright.\"sKey_GroupPostfix\" = '" + oTaskFilterVO.getsLogin() + "'\n"
                + "and docright.\"bUrgent\" = 'true'\n"
                + "and docright.\"sDate\" is null and docright.\"bWrite\" is not null \n"
                + "and docright.\"nID_DocumentStep\" = step.\"nID\" and step.\"snID_Process_Activiti\" = varhi.\"proc_inst_id_\"\n"
                + "and step.\"snID_Process_Activiti\" = var.\"proc_inst_id_\"\n";
        //для выполнения запроса для определенной подпапки
        if (oTaskFilterVO.getoDocumentStepType() != null) {
            sQuery = sQuery + " AND step.\"nID_DocumentStepType\" = '" + oTaskFilterVO.getoDocumentStepType().getId() + "'";
        }
        sQuery = sQuery + " and varhi.\"name_\" = 'sKey_Step_Document' and  varhi.\"text_\" = step.\"sKey_Step\"\n"
                + "and var.\"name_\" = 'sLoginAuthor' and  var.\"text_\" != '" + oTaskFilterVO.getsLogin() + "')\n"
                + "and task.\"proc_def_id_\" like '_doc%'";

        aoUrgentUnassignedUnprocessedTask.addAll(oTaskService.createNativeTaskQuery().sql(sQuery).list());
        
        List<Task> aoTaskToRemove = oTaskService.createTaskQuery().taskAssignee(oTaskFilterVO.getsLogin()).list();
        //убираем из необработанных те, которые находятся в черновиках
        Set<String> snID_TaskToRemove = aoTaskToRemove.stream()
                .map(Task::getId)
                .collect(Collectors.toSet());

        aoUrgentUnassignedUnprocessedTask = aoUrgentUnassignedUnprocessedTask.stream()
                .filter(oTask -> !snID_TaskToRemove.contains(oTask.getId()))
                .collect(Collectors.toList());
        
        aoUrgentUnassignedUnprocessedTask.addAll(
                getExternalTask(oTaskFilterVO.getsLogin(), DOCUMENT, THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_UNASSIGNED_UNPROCESSED,
                        false, oTaskFilterVO.getoDocumentStepType(), true)
        );

        return aoUrgentUnassignedUnprocessedTask;
    }

    /**
     * Неотработанные документы. Выборка из документстепрайт где bWrite=тру или
     * фолс и нет даты подписи.
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return все не отработанные документы
     */
    private List<TaskInfo> getOpenedUnassignedUnprocessedDocument(TaskFilterVO oTaskFilterVO) {
        LOG.debug("getOpenedUnassignedProcessedDocument start with oTaskFilterVO={}", oTaskFilterVO);
        List<TaskInfo> aoUnassignedUnprocessedTask = new ArrayList<>();

        String sFrom = "select distinct task.* from \"public\".\"act_ru_task\" task";
        String sWhere = " where task.\"proc_inst_id_\""
                + " in (select step.\"snID_Process_Activiti\" \n"
                + " from \"public\".\"DocumentStep\" step, \"public\".\"DocumentStepSubjectRight\" docright,\n"
                + " \"public\".\"act_hi_varinst\" varhi, \"public\".\"act_ru_variable\" var\n"
                + " where docright.\"sKey_GroupPostfix\" = '" + oTaskFilterVO.getsLogin() + "'\n"
                + " and docright.\"sDate\" is null and docright.\"bWrite\" is not null \n"
                + " and docright.\"nID_DocumentStep\" = step.\"nID\" and step.\"snID_Process_Activiti\" = varhi.\"proc_inst_id_\"\n"
                + " and step.\"snID_Process_Activiti\" = var.\"proc_inst_id_\"\n";
        //для выполнения запроса для определенной подпапки
        if (oTaskFilterVO.getoDocumentStepType() != null) {
            sWhere = sWhere + " AND step.\"nID_DocumentStepType\" = '" + oTaskFilterVO.getoDocumentStepType().getId() + "'";
        }
        sWhere = sWhere + " and varhi.\"name_\" = 'sKey_Step_Document' and  varhi.\"text_\" = step.\"sKey_Step\"\n"
                + " and var.\"name_\" = 'sLoginAuthor' and  var.\"text_\" != '" + oTaskFilterVO.getsLogin() + "')\n"
                + " and task.\"proc_def_id_\" like '_doc%'";
        SqlQueryVO oSqlQueryVO = new SqlQueryVO();
        oSqlQueryVO.setsFrom(sFrom);
        oSqlQueryVO.setsWhere(sWhere);
        //добавляем критерии поиска, если имеются
        oSqlQueryVO = addTaskFilterCriteria(oSqlQueryVO, oTaskFilterVO);

        String sQuery = oSqlQueryVO.getsFrom() + oSqlQueryVO.getsWhere();
        LOG.debug("getOpenedUnassignedProcessedDocument sQuery={}", sQuery);
        aoUnassignedUnprocessedTask.addAll(oTaskService.createNativeTaskQuery().sql(sQuery).list());


        List<Task> aoTaskToRemove = oTaskService.createTaskQuery().taskAssignee(oTaskFilterVO.getsLogin()).list();
        //убираем из необработанных те, которые находятся в черновиках

        Set<String> snID_TaskToRemove = aoTaskToRemove.stream()
                .map(Task::getId)
                .collect(Collectors.toSet());

        aoUnassignedUnprocessedTask = aoUnassignedUnprocessedTask.stream()
                .filter(oTask -> !snID_TaskToRemove.contains(oTask.getId()))
                .collect(Collectors.toList());

        if (oTaskFilterVO.getbSearchExternalTasks()) {
            aoUnassignedUnprocessedTask.addAll(
                    getExternalTask(oTaskFilterVO.getsLogin(), DOCUMENT, THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_UNASSIGNED_UNPROCESSED,
                            false, oTaskFilterVO.getoDocumentStepType(), false)
            );
        }

        return aoUnassignedUnprocessedTask;
    }

    /**
     * Отработанные документы. Выборка из документстепрайт где bWrite=нал или
     * есть дата подписи bDate
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return все отработанные документы
     */
    private List<TaskInfo> getOpenedUnassignedProcessedDocument(TaskFilterVO oTaskFilterVO) {
        LOG.debug("getOpenedUnassignedProcessedDocument start with oTaskFilterVO={}", oTaskFilterVO);
        List<TaskInfo> aoProcessedDocumentTask = new ArrayList<>();

        String sFrom = "select task.* from \"public\".\"act_ru_task\" task, \"public\".\"DocumentStep\" step,\n"
                + "\"public\".\"DocumentStepSubjectRight\" docright, \"public\".\"act_ru_variable\" var\n";
        String sWhere = " where docright.\"sKey_GroupPostfix\" = '" + oTaskFilterVO.getsLogin() + "'\n"
                + " and docright.\"nID_DocumentStep\" = step.\"nID\" and step.\"snID_Process_Activiti\" = task.\"proc_inst_id_\"\n"
                + " and step.\"sKey_Step\" = '_' and task.\"proc_inst_id_\" = var.\"proc_inst_id_\"\n"
                + " and var.\"name_\" = 'sLoginAuthor' and var.\"text_\" != '" + oTaskFilterVO.getsLogin() + "'\n"
                + " and task.\"proc_def_id_\" like '_doc%'\n";
        SqlQueryVO oSqlQueryVO = new SqlQueryVO();
        oSqlQueryVO.setsFrom(sFrom);
        oSqlQueryVO.setsWhere(sWhere);
        //добавляем критерии поиска, если имеются
        oSqlQueryVO = addTaskFilterCriteria(oSqlQueryVO, oTaskFilterVO);

        //убираем нерассмотренные документы
        sWhere = oSqlQueryVO.getsWhere() + " and task.\"id_\" not in (\n"
                + " select task.\"id_\" from \"public\".\"act_ru_task\" task where task.\"proc_inst_id_\" \n"
                + " in (select step.\"snID_Process_Activiti\" \n"
                + " from \"public\".\"DocumentStep\" step, \"public\".\"DocumentStepSubjectRight\" docright,\n"
                + "\"public\".\"act_hi_varinst\" variable\n"
                + " where docright.\"sKey_GroupPostfix\" = '" + oTaskFilterVO.getsLogin() + "'\n"
                + " and docright.\"sDate\" is null and docright.\"bWrite\" is not null \n"
                + " and docright.\"nID_DocumentStep\" = step.\"nID\" and step.\"snID_Process_Activiti\" = variable.\"proc_inst_id_\"\n"
                + " and variable.\"name_\" = 'sKey_Step_Document' and  variable.\"text_\" = step.\"sKey_Step\")\n"
                + " and task.\"proc_def_id_\" like '_doc%')";

        String sQuery = oSqlQueryVO.getsFrom() + sWhere;
        LOG.debug("getOpenedUnassignedProcessedDocument sQuery={}", sQuery);
        aoProcessedDocumentTask.addAll(oTaskService.createNativeTaskQuery().sql(sQuery).list());

        if (oTaskFilterVO.getbSearchExternalTasks()) {
            aoProcessedDocumentTask.addAll(
                    getExternalTask(oTaskFilterVO.getsLogin(), DOCUMENT, THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_UNASSIGNED_PROCESSED, false, null, false));
        }

        return aoProcessedDocumentTask;
    }

    /**
     * Не закрытые документы для которых sLogin - автор.
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return активные таски по незавершенным процессам (документам) для которых sLogin - автор
     */
    private List<TaskInfo> getOpenedCreatorDocument(TaskFilterVO oTaskFilterVO) {
        LOG.debug("OpenedCreatorDocument start with oTaskFilterVO={}", oTaskFilterVO);
        List<TaskInfo> aoOpenedCreatorDocumentTask = new ArrayList<>();
        //если нет логина автора, то автором считает того от кого уходит запрос
        if (oTaskFilterVO.getsLoginAuthor() == null) {
            oTaskFilterVO.setsLoginAuthor(oTaskFilterVO.getsLogin());
        }

        String sFrom = "select distinct task.* from \"public\".\"act_ru_task\" task, \"public\".\"act_ru_variable\" var\n";
        String sWhere = " where task.\"proc_inst_id_\" = var.\"proc_inst_id_\" and task.\"proc_def_id_\" like '_doc%'\n"
                + " and var.\"name_\" = 'sLoginAuthor' and var.\"text_\" = '" + oTaskFilterVO.getsLoginAuthor() + "'";
        SqlQueryVO oSqlQueryVO = new SqlQueryVO();
        oSqlQueryVO.setsFrom(sFrom);
        oSqlQueryVO.setsWhere(sWhere);
        //добавляем критерии поиска, если имеются
        oSqlQueryVO = addTaskFilterCriteria(oSqlQueryVO, oTaskFilterVO);

        String sQuery = oSqlQueryVO.getsFrom() + oSqlQueryVO.getsWhere();
        LOG.debug("getOpenedCreatorDocument sQuery={}", sQuery);
        aoOpenedCreatorDocumentTask.addAll(oTaskService.createNativeTaskQuery().sql(sQuery).list());

        return aoOpenedCreatorDocumentTask;
    }


    /**
     * Не закрытые срочные документы для которых sLogin - автор.
     *
     * @param sLogin логин
     * @return активные экстренные таски по незавершенным процессам (документам) для которых sLogin - автор
     */
    public List<Task> getUrgentOpenedCreatorDocument(String sLogin) {
        String sFrom = "select distinct task.* from \"public\".\"act_ru_task\" task, \"public\".\"act_ru_variable\" var\n";
        String sWhere = " where task.\"proc_inst_id_\" = var.\"proc_inst_id_\" and task.\"proc_def_id_\" like '_doc%'\n"
                + " and var.\"name_\" = 'sLoginAuthor' and var.\"text_\" = '" + sLogin + "'"
                + " and task.\"id_\" in (\n"
                + " select task.\"id_\" from \"public\".\"act_ru_task\" task where task.\"proc_inst_id_\" \n"
                + " in (select step.\"snID_Process_Activiti\" \n"
                + " from \"public\".\"DocumentStep\" step, \"public\".\"DocumentStepSubjectRight\" docright,\n"
                + "\"public\".\"act_hi_varinst\" variable\n"
                + " where docright.\"sKey_GroupPostfix\" = '" + sLogin + "'\n"
                + " and docright.\"nID_DocumentStep\" = step.\"nID\" and step.\"snID_Process_Activiti\" = variable.\"proc_inst_id_\"\n"
                + " and docright.\"bUrgent\" = 'true'\n"
                + " and variable.\"name_\" = 'sKey_Step_Document' and  variable.\"text_\" = step.\"sKey_Step\")\n"
                + " and task.\"proc_def_id_\" like '_doc%')";
        String sQuery = sFrom + sWhere;
        List<Task> aTaskList = oTaskService.createNativeTaskQuery().sql(sQuery).list();

        return aTaskList;
    }


    /**
     * Документы ожидающие подпись ЭЦП. Выборка из документстепрайт где sDate !=
     * null && bNeedECP != null && bNeedECP != false && sDateECP == nul
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return документы ожидающие подпись ЭЦП
     */
    private List<TaskInfo> getOpenedUnassignedWithoutECPDocument(TaskFilterVO oTaskFilterVO) {
        LOG.debug("OpenedUnassignedWithoutECPDocument start with oTaskFilterVO={}", oTaskFilterVO);
        List<TaskInfo> aoTask = new ArrayList<>();

        String sFrom = "select * from \"public\".\"act_ru_task\" task";
        String sWhere = " where task.\"proc_inst_id_\"\n"
                + " in (select step.\"snID_Process_Activiti\" \n"
                + " from \"public\".\"DocumentStep\" step, \"public\".\"DocumentStepSubjectRight\" docright,\n"
                + "\"public\".\"act_hi_varinst\" varhi, \"public\".\"act_ru_variable\" var\n"
                + " where docright.\"sKey_GroupPostfix\" = '" + oTaskFilterVO.getsLogin() + "'\n"
                + " and docright.\"sDate\" is not null and docright.\"sDateECP\" is null and docright.\"bNeedECP\" = 'true'\n"
                + " and docright.\"nID_DocumentStep\" = step.\"nID\" and step.\"snID_Process_Activiti\" = varhi.\"proc_inst_id_\"\n"
                + " and step.\"snID_Process_Activiti\" = var.\"proc_inst_id_\" and step.\"sKey_Step\" != '_'\n"
                + " and var.\"name_\" = 'sLoginAuthor' and  var.\"text_\" != '" + oTaskFilterVO.getsLogin() + "')\n"
                + " and task.\"proc_def_id_\" like '_doc%'";
        SqlQueryVO oSqlQueryVO = new SqlQueryVO();
        oSqlQueryVO.setsFrom(sFrom);
        oSqlQueryVO.setsWhere(sWhere);
        //добавляем критерии поиска, если имеются
        oSqlQueryVO = addTaskFilterCriteria(oSqlQueryVO, oTaskFilterVO);

        String sQuery = oSqlQueryVO.getsFrom() + oSqlQueryVO.getsWhere();
        LOG.debug("getOpenedUnassignedWithoutECPDocument sQuery={}", sQuery);
        aoTask.addAll(oTaskService.createNativeTaskQuery().sql(sQuery).list());

        if (oTaskFilterVO.getbSearchExternalTasks()) {
            aoTask.addAll(getExternalTask(oTaskFilterVO.getsLogin(), DOCUMENT,
                    THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_UNASSIGNED_WITHOUTECP, false, null, false));
        }

        return aoTask;
    }

    /**
     * Получение внешних активных тасок. Внешними считаются те, для которых есть запись в таблице ProcessLink
     *
     * @param sID_Group_Activiti персонализированная группа
     * @param sType              тип закладки, например Task или Document
     * @param sSubType           тип подзакладки, например: Мои документы, Просмотренные
     * @param bIncludeDeleted    включая удаленные (sStatus='deleted')
     * @param oDocumentStepType  тип степа
     * @param bUrgent            только срочные
     * @return список активных внешних тасок
     */
    public List<TaskInfo> getExternalTask(String sID_Group_Activiti, String sType, String sSubType,
                                          boolean bIncludeDeleted, DocumentStepType oDocumentStepType, boolean bUrgent) {
        LOG.info("getExternalTask start with sID_Group_Activiti={}, sType={}, sSubType={}",
                sID_Group_Activiti, sType, sSubType);
        List<ProcessLink> aoProcessLink = oProcessLinkDao.getProcessLinks(sID_Group_Activiti, sType, sSubType);
        LOG.info("aoProcessLink.size={}", aoProcessLink.size());
        //для отдачи внешней таски в правильную подпапку нерассмотренных
        if (oDocumentStepType != null) {
            aoProcessLink = aoProcessLink.stream()
                    .filter(oProcessLink -> oDocumentStepType.equals(oProcessLink.getoDocumentStepType()))
                    .collect(Collectors.toList());
        }
        List<ProcessLink> aoProcessLink_Deleted = null;
        if (bIncludeDeleted) {
            aoProcessLink_Deleted = oProcessLinkDao.getDeletedProcessLinks(sID_Group_Activiti);
        }
        //отфильтровать экстренные документы
        if (bUrgent) {
            aoProcessLink = aoProcessLink.stream()
                    .filter(oProcessLink -> oProcessLink.getbUrgent() != null && oProcessLink.getbUrgent())
                    .collect(Collectors.toList());
        }

        List<TaskDataVO> aoTaskDataVO = new ArrayList<>();
        List<TaskInfo> aoTaskDataVO_Result = new ArrayList<>();

        for (ProcessLink oProcessLink : aoProcessLink) {

            TaskDataVO oTaskDataVO = new TaskDataVO();
            if (bIncludeDeleted) {
                for (ProcessLink oDeletedProcessLink : aoProcessLink_Deleted) {
                    if (oProcessLink.getSnID_Task().equals(oDeletedProcessLink.getSnID_Task())) {
                        oTaskDataVO.setsStatus("deleted");
                    }
                }
            }
            oTaskDataVO.setId(oProcessLink.getSnID_Task());
            oTaskDataVO.setProcessInstanceId(oProcessLink.getSnID_Process_Activiti());
            oTaskDataVO.setCreateTime(oProcessLink.getsTaskDateCreate().toDate()); //дата для сортировки тасок
            oTaskDataVO.setsCreateTime(oProcessLink.getsTaskDateCreate().toString("yyyy-MM-dd'T'HH:mm:ss.SSSZ")); //для клиента
            oTaskDataVO.setName(oProcessLink.getsTaskName());
            oTaskDataVO.setnOrder(ToolLuna.getProtectedNumber(Long.parseLong(oProcessLink.getSnID_Process_Activiti())));
            oTaskDataVO.setsID_Order(oProcessLink.getoServer().getId()
                    + "-" + ToolLuna.getProtectedNumber(Long.parseLong(oProcessLink.getSnID_Process_Activiti())));
            oTaskDataVO.setoDocumentStepType(oProcessLink.getoDocumentStepType());
            oTaskDataVO.setsProcessName(oProcessLink.getsProcessName());
            oTaskDataVO.setbUrgent(oProcessLink.getbUrgent());
            aoTaskDataVO.add(oTaskDataVO);
        }

        aoTaskDataVO_Result.addAll(aoTaskDataVO);

        return aoTaskDataVO_Result;
    }

    /**
     * Валидация таски. Для кейса, когда документ одновременно обрабатывают несколько человек. После того, как один
     * сотрудник отработал документ - другой сможет его обработать только с другим ид таски.
     *
     * @param snID_Process_Activiti ид процесса
     * @param snID_Task_Activiti    ид таски
     * @param sID_Group_Activiti    персонализированная группа
     */
    public void validateTask(String snID_Process_Activiti, String snID_Task_Activiti, String sID_Group_Activiti) {
        LOG.info("Task validation started with snID_Process_Activiti={}, snID_Task_Activiti={}, sID_Group_Activiti={}",
                snID_Process_Activiti, snID_Task_Activiti, sID_Group_Activiti);
        //валидация ид таски
        if (snID_Process_Activiti != null && snID_Task_Activiti != null) {
            List<Task> aTask = oTaskService.createTaskQuery().processInstanceId(snID_Process_Activiti).active().list();
            LOG.info("Active task size={}", aTask.size());
            for (Task oTask : aTask) {
                if (!oTask.getId().equals(snID_Task_Activiti)) {
                    throw new DocumentAccessException(DocumentAccessException.DOCUMENT_MODIFIED);
                }
            }
            LOG.info("Task validation complete");
        }
    }

    /**
     * Валидация степа. Проверяем сходство активного и переданого степа.
     *
     * @param snID_Process_Activiti ид процесса
     * @param sKey_Step             степ на котором находится документ
     */
    public void validateDocumentStep(String snID_Process_Activiti, String sKey_Step) {
        LOG.info("Task validation started with snID_Process_Activiti={}, sKey_Step={}",
                snID_Process_Activiti, sKey_Step);
        //валидация степа
        String sKey_Step_Active = oDocumentStepService.getActiveStepName(snID_Process_Activiti);
        //для дефолтного степа валидация не выполняется
        if (!sKey_Step.equals(sKey_Step_Active) && !sKey_Step.equals("_")) {
            throw new DocumentAccessException(DocumentAccessException.DOCUMENT_MODIFIED);
        }
    }

    /**
     * Поиск активной таски и вкладки, в которой она должна находиться. (Кейс для черновиков проигнорирован)
     *
     * @param snID_Task          ид таски
     * @param sID_Group_Activiti персонализированная группа
     * @param sID_Order          cидордер
     * @return ид активной таски, sID_Order и название вкладки в которой должна находится таска
     */
    public Map<String, Object> findTaskTab(String snID_Task, String sID_Group_Activiti, String sID_Order) {
        LOG.info("findTaskAndDocumentTab started with snID_Task={}, sID_Group={}, sID_Order={}",
                snID_Task, sID_Group_Activiti, sID_Order);
        Map<String, Object> mResultMap = new HashMap<>();
        String snID_Task_Active = null;
        String sDocumentStatus = null;
        String snID_Process = null;

        if (snID_Task != null) {
            HistoricTaskInstance oHistoricTaskInstance = oHistoryService.createHistoricTaskInstanceQuery()
                    .taskId(snID_Task)
                    .singleResult();

            snID_Process = oHistoricTaskInstance.getProcessInstanceId();
        }
        if (sID_Order != null) {
            long nID_Process_Protected = Long.parseLong(sID_Order.split("-")[1]);
            try {
                snID_Process = getOriginalProcessInstanceId(nID_Process_Protected);
            } catch (CRCInvalidException oException) {
                LOG.warn("Can't get original snID_Process", oException);
                throw new DocumentAccessException(DocumentAccessException.ACCESS_DENIED);
            }
        }
        if (processHasTask(snID_Process)) {
            //кладем в результирующую мапу
            mResultMap.put("sID_Order", oGeneralConfig.getOrderId_ByProcess(Long.valueOf(snID_Process)));

            HistoricProcessInstance oHistoricProcessInstance = oHistoryService.createHistoricProcessInstanceQuery()
                    .processInstanceId(snID_Process)
                    .singleResult();
            Date dtProcess_EndTime = oHistoricProcessInstance.getEndTime();
            //считаем что процесс закрытый
            if (dtProcess_EndTime != null) {
                sDocumentStatus = oHistoricProcessInstance.getProcessDefinitionId().startsWith("_doc_")
                        ? THE_STATUS_OF_TASK_IS_DOCUMENT_CLOSED
                        : THE_STATUS_OF_TASK_IS_CLOSED;

                List<HistoricTaskInstance> aoTaskList = oHistoryService.createHistoricTaskInstanceQuery()
                        .processInstanceId(snID_Process)
                        .finished()
                        .list();
                LOG.info("HistoricTaskInstance size before filtering={}", aoTaskList.size());
                aoTaskList = filterHistoryTasksByEndTime(aoTaskList);
                LOG.info("HistoricTaskInstance size after filtering={}", aoTaskList.size());
                //последняя закрытая юзертаска
                if (!aoTaskList.isEmpty()) {
                    snID_Task_Active = aoTaskList.get(0).getId();
                    checkTaskPermission(snID_Task_Active, snID_Process, sID_Group_Activiti);
                }
                mResultMap.put("snID_Task_Active", snID_Task_Active);
                mResultMap.put("sDocumentStatus", sDocumentStatus);

            } else if (oHistoricProcessInstance.getProcessDefinitionId().startsWith("_doc_")) {
                List<Task> aoTask_Active = oTaskService.createTaskQuery().processInstanceId(snID_Process).active().list();
                snID_Task_Active = aoTask_Active.get(0).getId();
                //находим автора документа
                String sLoginAuthor = String.valueOf(oRuntimeService.getVariable(snID_Process, "sLoginAuthor"));
                if (sLoginAuthor != null && sLoginAuthor.equals(sID_Group_Activiti)) {
                    sDocumentStatus = THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_CREATOR;
                }
                //если логин не автор ищем по остальным вкладкам
                if (sDocumentStatus == null) {
                    String sKey_Step_Active = oDocumentStepService.getActiveStepName(snID_Process);
                    DocumentStep oDocumentStep = oDocumentStepDao.getDocumentStepByID_ProcessAndName(snID_Process, sKey_Step_Active);
                    List<DocumentStepSubjectRight> aDocumentStepSubjectRight = oDocumentStep.aDocumentStepSubjectRight();
                    LOG.info("aDocumentStepSubjectRight size before filtering is {}", aDocumentStepSubjectRight.size());
                    aDocumentStepSubjectRight = aDocumentStepSubjectRight.stream()
                            .filter(oDocumentStepSubject -> oDocumentStepSubject.getsKey_GroupPostfix().equals(sID_Group_Activiti))
                            .collect(Collectors.toList());
                    LOG.info("aDocumentStepSubjectRight size after filtering is {}", aDocumentStepSubjectRight.size());
                    //если под одним логином работают несколько человек, одновременно обрабатывают один док, после того как
                    //первый подписал, сменился активный степ - права для просмотра смотрим на дефолтном
                    if (aDocumentStepSubjectRight.isEmpty()) {
                        oDocumentStep = oDocumentStepDao.getDocumentStepByID_ProcessAndName(snID_Process, "_");
                        aDocumentStepSubjectRight.addAll(oDocumentStep.aDocumentStepSubjectRight());
                        LOG.info("aDocumentStepSubjectRight size before filtering 2 is {}", aDocumentStepSubjectRight.size());
                        aDocumentStepSubjectRight = aDocumentStepSubjectRight.stream()
                                .filter(oDocumentStepSubject -> oDocumentStepSubject.getsKey_GroupPostfix().equals(sID_Group_Activiti))
                                .collect(Collectors.toList());
                        LOG.info("aDocumentStepSubjectRight size after filtering 2 is {}", aDocumentStepSubjectRight.size());
                    }
                    for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight) {
                        LOG.info("oDocumentStepSubjectRight={}", oDocumentStepSubjectRight);
                        Boolean bNeedECP = oDocumentStepSubjectRight.getbNeedECP();
                        DateTime sDateECP = oDocumentStepSubjectRight.getsDateECP();
                        DateTime sDate = oDocumentStepSubjectRight.getsDate();
                        Boolean bWrite = oDocumentStepSubjectRight.getbWrite();

                        if (bNeedECP == true && sDateECP == null && sDate != null) {
                            sDocumentStatus = THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_UNASSIGNED_WITHOUTECP;
                            break;
                        }
                        if (bWrite != null && sDate == null) {
                            sDocumentStatus = THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_UNASSIGNED_UNPROCESSED;
                            if (oDocumentStep.getoDocumentStepType().getbFolder() == true) {
                                mResultMap.put("sSubTab", oDocumentStep.getoDocumentStepType().getName());
                            }
                            break;
                        }
                        if (bWrite == null || sDate != null) {
                            sDocumentStatus = THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_UNASSIGNED_PROCESSED;
                            break;
                        }
                        LOG.info("finded missing case! oDocumentStepSubjectRight={}", oDocumentStepSubjectRight);
                    }
                }
                mResultMap.put("snID_Task_Active", snID_Task_Active);
                mResultMap.put("sDocumentStatus", sDocumentStatus);

            } else {
                List<Task> aoTask_Active = oTaskService.createTaskQuery().processInstanceId(snID_Process).active().list();
                Task oTask = aoTask_Active.get(0);

                if (oTask.getAssignee() != null) {
                    LOG.info("Task is THE_STATUS_OF_TASK_IS_OPENED_ASSIGNED");
                    sDocumentStatus = THE_STATUS_OF_TASK_IS_OPENED_ASSIGNED;

                } else {
                    LOG.info("Task has no assigne");
                    ProcessSubject oProcessSubject = oProcessSubjectDao.findBy("snID_Task_Activiti", oTask.getId()).orNull();
                    if (oProcessSubject != null) {

                        LOG.info("oProcessSubject process is {}", oProcessSubject.getSnID_Process_Activiti());

                        List<ProcessSubject> aProcessSubject = oProcessSubjectDao
                                .findAllBy("nID_ProcessSubjectTask", oProcessSubject.getnID_ProcessSubjectTask());
                        List<ProcessSubject> aProcessSubject_Result = new ArrayList<>();

                        LOG.info("aProcessSubject size {}", aProcessSubject.size());

                        for (ProcessSubject oProcessSubject_Curr : aProcessSubject) {
                            if (oProcessSubject_Curr.getsLogin().equals(sID_Group_Activiti) &&
                                    oProcessSubject_Curr.getSnID_Process_Activiti().equals(oProcessSubject.getSnID_Process_Activiti())) {
                                aProcessSubject_Result.add(oProcessSubject_Curr);
                                LOG.info("aProcessSubject_Result add login {}", sID_Group_Activiti);
                            }
                        }

                        if (aProcessSubject_Result.size() > 1) {
                            for (ProcessSubject oProcessSubject_Curr : aProcessSubject_Result) {
                               
                                
                                ProcessSubjectStatus oProcessSubjectStatus = oProcessSubject_Curr.getoProcessSubjectStatus();
                                Boolean bFinished = false;
                                if(oProcessSubjectStatus.getsID().equals("executed")||oProcessSubjectStatus.getsID().equals("notExecuted")
                                        ||oProcessSubjectStatus.getsID().equals("unactual"))
                                {
                                    bFinished = true;
                                }
                                    
                                if (oProcessSubject_Curr.getsLoginRole().equals("Executor")) {
                                    mResultMap.put("snID_Task_Active", oProcessSubject_Curr.getSnID_Task_Activiti());
                                    sDocumentStatus =  bFinished ?  THE_STATUS_OF_TASK_IS_EXECUTION_FINISHED : THE_STATUS_OF_TASK_IS_EXECUTION;
                                    break;
                                }
                            }

                        } else {
                            if (!aProcessSubject_Result.isEmpty()) {
                                
                                ProcessSubjectStatus oProcessSubjectStatus = aProcessSubject_Result.get(0).getoProcessSubjectStatus();
                                Boolean bFinished = false;
                                if(oProcessSubjectStatus.getsID().equals("executed")||oProcessSubjectStatus.getsID().equals("notExecuted")
                                        ||oProcessSubjectStatus.getsID().equals("unactual"))
                                {
                                    bFinished = true;
                                }
                                
                                if (aProcessSubject_Result.get(0).getsLoginRole().equals("Executor")) {
                                    sDocumentStatus =  bFinished ? THE_STATUS_OF_TASK_IS_EXECUTION_FINISHED : THE_STATUS_OF_TASK_IS_EXECUTION;
                                } else {
                                    sDocumentStatus =  bFinished ? THE_STATUS_OF_TASK_IS_CONTROL_FINISHED : THE_STATUS_OF_TASK_IS_CONTROL;
                                }

                                mResultMap.put("snID_Task_Active", aProcessSubject_Result.get(0).getSnID_Task_Activiti());
                            }
                        }

                    } else {
                        LOG.info("oProcessSubject is null");
                        sDocumentStatus = THE_STATUS_OF_TASK_IS_OPENED_UNASSIGNED;
                        mResultMap.put("snID_Task_Active", oTask.getId());
                    }
                }
                mResultMap.put("sDocumentStatus", sDocumentStatus);
            }

            if (sDocumentStatus == null) {

                if (oHistoricProcessInstance.getProcessDefinitionId().startsWith("_doc_")) {
                    //in case of editing task and open document from it

                    if (dtProcess_EndTime != null) {
                        sDocumentStatus = THE_STATUS_OF_TASK_IS_DOCUMENT_CLOSED;
                    } else {
                        sDocumentStatus = THE_STATUS_OF_TASK_IS_DOCUMENT_OPENED_UNASSIGNED_PROCESSED;
                    }
                } else {
                    throw new RuntimeException("Документ не знайдено!");
                }
            }

            mResultMap.put("sDocumentStatus", sDocumentStatus);
            LOG.info("Result: mResultMap={}", mResultMap);

            return mResultMap;
        } else {
            throw new DocumentAccessException(DocumentAccessException.ACCESS_DENIED);
        }
    }

    /**
     * Отфильтровать список такоим образом, чтобы там остались только последние юзертаски.
     *
     * @param aoTaskList список закрытых тасок
     * @return список последних юзертаскок
     */
    public List<HistoricTaskInstance> filterHistoryTasksByEndTime(List<HistoricTaskInstance> aoTaskList) {
        LOG.info("Task count before a filtering {}", aoTaskList.size());
        aoTaskList = (List<HistoricTaskInstance>) removeTaskDuplicate(aoTaskList);
        List<HistoricTaskInstance> aoTaskToRemove = new ArrayList<>();
        //если таски емеют одинаковый ProcessInstanceId, сверяем дату закрытия
        //таска которая была закрыта раньше добавляется в список для удаления
        Collections.sort(aoTaskList, (HistoricTaskInstance oTask1, HistoricTaskInstance oTask2) -> {
            int nResult = oTask1.getProcessInstanceId().compareTo(oTask2.getProcessInstanceId());
            if (nResult == 0) {
                //Null safe comparison of Comparables. null is assumed to be less than a non-null value.
                nResult = ObjectUtils.compare(oTask1.getEndTime(), oTask2.getEndTime(), true);
                aoTaskToRemove.add(nResult == 1 ? oTask2 : oTask1);
            }
            return nResult;
        });
        aoTaskList.removeAll(aoTaskToRemove);
        LOG.info("Task count after a filtering {}", aoTaskList.size());

        return aoTaskList;
    }

    /**
     * Отсортировать таски. Типы сортироваок: 'taskCreateTime', для вкладки задач 'datePlan' и 'executionTime'
     *
     * @param aoAllTasks массив тасок для сортировки
     * @param sSortBy    тип сортировки
     * @return обьект обвертку с отсортированными тасками и мапой по которой была сортировка
     */
    public SortedTaskVO sortTasksAndGetSortingParameters(List<TaskInfo> aoAllTasks, String sSortBy) {
        LOG.info("Sorting start sSortBy={}", sSortBy);

        if (sSortBy.equals("datePlan")) {
            return sortTasksByDatePlan(aoAllTasks, sSortBy);

        } else if (sSortBy.equals("executionTime")) {
            return sortTasksByExecutionTime(aoAllTasks, sSortBy);

        } else {
            //дефолтная сортировка по "taskCreateTime"
            return sortTasksByCreateTime(aoAllTasks, sSortBy);
        }
    }

    /**
     * Сортировать таски по дате создания.
     *
     * @param aoAllTasks список тасок
     * @param sSortBy    тип сортировки
     * @return обьект обвертку, с отсортированным списком тасок и параметрами по которым выполнялась сортировка
     */
    public SortedTaskVO sortTasksByCreateTime(List<TaskInfo> aoAllTasks, String sSortBy) {
        LOG.info("sortTasksByCreateTime start...");
        Collections.sort(aoAllTasks, (task1, task2) -> task1.getCreateTime().compareTo(task2.getCreateTime()));

        SortedTaskVO oSortedTaskVO = new SortedTaskVO();
        oSortedTaskVO.setAoListOfTasks(aoAllTasks);

        return oSortedTaskVO;
    }

    /**
     * Сортировать таски по дате выполнения.
     *
     * @param aoAllTasks список тасок
     * @param sSortBy    тип сортировки
     * @return обьект обвертку, с отсортированным списком тасок и параметрами по которым выполнялась сортировка
     */
    public SortedTaskVO sortTasksByDatePlan(List<TaskInfo> aoAllTasks, String sSortBy) {
        LOG.info("sortTasksByDatePlan start...");
        List<ProcessSubject> aoProcessSubject = oProcessSubjectDao.findAll();
        Map<String, DateTime> mTaskId_DatePlan = new HashMap<>();
        String snID_Task;
        DateTime dtDatePlan;
        //собираем мапу в которой будем искать для таски дату выполнения задания
        for (ProcessSubject oProcessSubject : aoProcessSubject) {
            snID_Task = oProcessSubject.getSnID_Task_Activiti();
            dtDatePlan = oProcessSubject.getsDatePlan();
            if (snID_Task != null && dtDatePlan != null) {
                mTaskId_DatePlan.put(snID_Task, dtDatePlan);
            }
        }
        //Сортировка выполняется по дате выполнения ProcessSubject, таски у которых нет даты выполнения
        //сортируются по дате создания. Параметр по которому выполняется сортировка кладется в мапу,
        //чтобы потом заполнить обьект обверку.
        Collections.sort(aoAllTasks, (TaskInfo task1, TaskInfo task2) -> {
            DateTime dt1 = mTaskId_DatePlan.get(task1.getId());
            DateTime dt2 = mTaskId_DatePlan.get(task2.getId());
            if (dt1 == null) {
                dt1 = new DateTime(task1.getCreateTime());
                mTaskId_DatePlan.put(task1.getId(), dt1);
            }
            if (dt2 == null) {
                dt2 = new DateTime(task2.getCreateTime());
                mTaskId_DatePlan.put(task2.getId(), dt2);
            }
            return dt1.compareTo(dt2);
        });

        SortedTaskVO oSortedTaskVO = new SortedTaskVO();
        oSortedTaskVO.setmSortingParameters(mTaskId_DatePlan);
        oSortedTaskVO.setAoListOfTasks(aoAllTasks);

        return oSortedTaskVO;
    }

    /**
     * Сортировать таски по кол-ву оставшихся дней на выполнение.
     *
     * @param aoAllTasks список тасок
     * @param sSortBy    тип сортировки
     * @return обьект обвертку, с отсортированным списком тасок и параметрами по которым выполнялась сортировка
     */
    public SortedTaskVO sortTasksByExecutionTime(List<TaskInfo> aoAllTasks, String sSortBy) {
        LOG.info("sortTasksByExecutionTime start...");
        SortedTaskVO oSortedTaskVO = sortTasksByDatePlan(aoAllTasks, sSortBy);
        Map<String, DateTime> mTaskId_DatePlan = (Map<String, DateTime>) oSortedTaskVO.getmSortingParameters();

        Map<String, Long> mTaskId_ExecutionTime = new HashMap<>();

        DateTime dtCurrentDate = DateTime.now();
        mTaskId_DatePlan.forEach((sTaskId, dtDatePlan) -> {
            //из текущей даты вычитаем дату выполнения задания
            Integer nDayDifferrence = Days.daysBetween(dtCurrentDate.toLocalDate(), dtDatePlan.toLocalDate()).getDays();
            //если получаем отрицательно число
            if (nDayDifferrence < 0) {
                nDayDifferrence = 0;
            }
            mTaskId_ExecutionTime.put(sTaskId, Long.valueOf(nDayDifferrence));
        });
        //Сортировка по оставшемуся времени на выполнение задания
        Collections.sort(aoAllTasks, (TaskInfo task1, TaskInfo task2) -> {
            Long nTime1 = mTaskId_ExecutionTime.get(task1.getId());
            Long nTime2 = mTaskId_ExecutionTime.get(task2.getId());

            return nTime1.compareTo(nTime2);
        });

        oSortedTaskVO.setmSortingParameters(mTaskId_ExecutionTime);
        oSortedTaskVO.setAoListOfTasks(aoAllTasks);

        return oSortedTaskVO;
    }

    /**
     * Удалить дубликаты таскок
     *
     * @param aoListOfTask список для фильтрации
     * @return лист без дубликатов
     */
    public List<? extends TaskInfo> removeTaskDuplicate(List<? extends TaskInfo> aoListOfTask) {
        LOG.info("removeTaskDeplicate started, size={}", aoListOfTask.size());
        Map<String, TaskInfo> mFilteredTasks = new HashMap<>();

        aoListOfTask.forEach(oTask -> {
            if (!mFilteredTasks.containsKey(oTask.getId())) {
                mFilteredTasks.put(oTask.getId(), oTask);
            }
        });
        List<? extends TaskInfo> aoResult = new ArrayList(mFilteredTasks.values());
        LOG.info("removeTaskDeplicate finished, size={}", aoResult.size());

        return aoResult;
    }

    public TaskCountersVO getTaskCountersForAllTab(String sLogin, boolean bIncludeDeleted) throws ParseException {
        LOG.info("getTaskCountersForAllTab started for sID_Group_Activiti={}", sLogin);
        TaskFilterVO oTaskFilterVO = new TaskFilterVO();
        oTaskFilterVO.setsLogin(sLogin);
        oTaskFilterVO.setbIncludeDeleted(bIncludeDeleted);
        oTaskFilterVO.setbSearchExternalTasks(true);

        TaskCountersVO oTaskCountersVO = new TaskCountersVO();

        oTaskCountersVO.setDocumentOpenedUnassignedUnprocessed(getOpenedUnassignedUnprocessedDocument(oTaskFilterVO).size());

        oTaskCountersVO.setOpenedCreatorDocument(getOpenedCreatorDocument(oTaskFilterVO).size());

        oTaskCountersVO.setDocumentOpenedUnassignedWithoutECP(getOpenedUnassignedWithoutECPDocument(oTaskFilterVO).size());

        oTaskCountersVO.setDocumentOpenedUnassignedProcessed(getOpenedUnassignedProcessedDocument(oTaskFilterVO).size());

        oTaskCountersVO.setDocumentClosed(getDocumentClosed(oTaskFilterVO).size());

        oTaskCountersVO.setOpenedAssigned(getOpenedAssignedTask(oTaskFilterVO).size());

        oTaskCountersVO.setOpenedUnassigned(getOpenedUnassignedTask(oTaskFilterVO).size());

        oTaskCountersVO.setControl(getControlTask(oTaskFilterVO).size());

        oTaskCountersVO.setClosed(getClosedTask(oTaskFilterVO).size());

        oTaskCountersVO.setTicket(oFlowService.getFlowSlotTickets(sLogin, false, null).getTotal());

        oTaskCountersVO.setExecution(getExecutionTask(oTaskFilterVO).size());

        return oTaskCountersVO;
    }

    /**
     * Получить открытые таски задачи, которые находятся в работе.
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return все открытые таски задачи, которые находятся в работе для sLogin
     */
    private List<TaskInfo> getOpenedAssignedTask(TaskFilterVO oTaskFilterVO) {
        LOG.debug("OpenedAssignedTask started with oTaskFilterVO={}", oTaskFilterVO);
        List<TaskInfo> aoOpenedAssignedTask = new ArrayList<>();
        //получили запрос, который возвращает все открытые таски
        SqlQueryVO oSqlQueryVO = getOpenedTaskSql(oTaskFilterVO);
        //добавили ограничение именно для данной вкладки (задачи в рабоче)
        String sWhere = oSqlQueryVO.getsWhere() + "      AND task.\"assignee_\" = '" + oTaskFilterVO.getsLogin() + "'" +
                "      AND task.\"proc_def_id_\" NOT LIKE '_doc%'\n";
        oSqlQueryVO.setsWhere(sWhere);
        //добавляем критерии поиска, если имеются
        oSqlQueryVO = addTaskFilterCriteria(oSqlQueryVO, oTaskFilterVO);

        String sQuery = oSqlQueryVO.getsFrom() + oSqlQueryVO.getsWhere();
        LOG.debug("getOpenedAssignedTask sQuery={}", sQuery);
        aoOpenedAssignedTask.addAll(oTaskService.createNativeTaskQuery().sql(sQuery).list());

        if (oTaskFilterVO.getbSearchExternalTasks()) {
            aoOpenedAssignedTask.addAll(
                    getExternalTask(oTaskFilterVO.getsLogin(), TASK, THE_STATUS_OF_TASK_IS_OPENED_ASSIGNED, false, null, false));
        }

        return aoOpenedAssignedTask;
    }

    /**
     * Получить открытые необработанные таски (с централа).
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return все открытые необработанные таски c централа для sLogin
     */
    private List<TaskInfo> getOpenedUnassignedTask(TaskFilterVO oTaskFilterVO) {
        LOG.debug("OpenedUnassignedTask started with oTaskFilterVO={}", oTaskFilterVO);
        List<TaskInfo> aoOpenedUnassigned = new ArrayList<>();
        //получили запрос, который возвращает все открытые таски
        SqlQueryVO oSqlQueryVO = getOpenedTaskSql(oTaskFilterVO);
        //добавили ограничение именно для данной вкладки (только заявки с централа)
        String sWhere = oSqlQueryVO.getsWhere() + "      AND task.\"proc_def_id_\" NOT LIKE '_doc%'" +
                "      AND task.\"proc_def_id_\" NOT LIKE '_task%'\n" +
                "      AND task.\"assignee_\" IS NULL \n";
        oSqlQueryVO.setsWhere(sWhere);
        //добавляем критерии поиска, если имеются
        oSqlQueryVO = addTaskFilterCriteria(oSqlQueryVO, oTaskFilterVO);

        String sQuery = oSqlQueryVO.getsFrom() + oSqlQueryVO.getsWhere();
        LOG.debug("getOpenedUnassignedTask sQuery={}", sQuery);
        aoOpenedUnassigned.addAll(oTaskService.createNativeTaskQuery().sql(sQuery).list());

        if (oTaskFilterVO.getbSearchExternalTasks()) {
            aoOpenedUnassigned.addAll(
                    getExternalTask(oTaskFilterVO.getsLogin(), TASK, THE_STATUS_OF_TASK_IS_OPENED_UNASSIGNED, false, null, false));
        }

        return aoOpenedUnassigned;
    }

    /**
     * Получить открытые необработанные таски задачи.
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return все открытые необработанные таски задачи для sLogin
     */
    private List<TaskInfo> getExecutionTask(TaskFilterVO oTaskFilterVO) {
        LOG.debug("ExecutionTask started with oTaskFilterVO={}", oTaskFilterVO);
        List<TaskInfo> aoExecutionTask = new ArrayList<>();
        //условие валидация, нельзя контролировать задачу по которой ты исполнитель
        if (!oTaskFilterVO.getsLogin().equals(oTaskFilterVO.getsLoginController())) {
            //если нет sLoginExecutor, то считаем что нужно искать таски, которые на исполнении у sLogin (тот кто выполняет поиск)
            if (oTaskFilterVO.getsLoginExecutor() == null) {
                oTaskFilterVO.setsLoginExecutor(oTaskFilterVO.getsLogin());
            }
            //получили запрос, который возвращает все открытые таски
            SqlQueryVO oSqlQueryVO = getOpenedTaskSql(oTaskFilterVO);
            //добавили ограничение именно для данной вкладки (только задачи)
            String sWhere = oSqlQueryVO.getsWhere() + "      AND task.\"proc_def_id_\" LIKE '_task%'\n"
                    + "      AND subject.\"nID_ProcessSubjectStatus\" NOT IN ('2', '3', '4')\n";
            oSqlQueryVO.setsWhere(sWhere);
            //добавляем критерии поиска, если имеются
            oSqlQueryVO = addTaskFilterCriteria(oSqlQueryVO, oTaskFilterVO);

            String sQuery = oSqlQueryVO.getsFrom() + oSqlQueryVO.getsWhere();
            LOG.debug("getExecutionTask sQuery={}", sQuery);
            aoExecutionTask.addAll(oTaskService.createNativeTaskQuery().sql(sQuery).list());

            if (oTaskFilterVO.getbSearchExternalTasks()) {
                aoExecutionTask.addAll(
                        getExternalTask(oTaskFilterVO.getsLogin(), TASK, THE_STATUS_OF_TASK_IS_EXECUTION, false, null, false));
            }
        }

        return aoExecutionTask;
    }

    /**
     * Получить завершенные таски задачи, которые были на исполнении.
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return все открытые необработанные таски задачи для sLogin
     */
    private List<TaskInfo> getExecutionFinishedTask(TaskFilterVO oTaskFilterVO) {
        LOG.debug("getExecutionFinishedTask started with oTaskFilterVO={}", oTaskFilterVO);
        List<TaskInfo> aoExecutionFinishedTask = new ArrayList<>();
        //условие валидация, нельзя контролировать задачу по которой ты исполнитель
        if (!oTaskFilterVO.getsLogin().equals(oTaskFilterVO.getsLoginController())) {
            //если нет sLoginExecutor, то считаем что нужно искать таски, которые на исполнении у sLogin (тот кто выполняет поиск)
            if (oTaskFilterVO.getsLoginExecutor() == null) {
                oTaskFilterVO.setsLoginExecutor(oTaskFilterVO.getsLogin());
            }
            //получили запрос, который возвращает все открытые таски
            SqlQueryVO oSqlQueryVO = getOpenedTaskSql(oTaskFilterVO);
            //добавили ограничение именно для данной вкладки (только задачи)
            String sWhere = oSqlQueryVO.getsWhere() + "      AND task.\"proc_def_id_\" LIKE '_task%'\n"
                    + "      AND subject.\"nID_ProcessSubjectStatus\" IN ('2', '3', '4')\n";
            oSqlQueryVO.setsWhere(sWhere);
            //добавляем критерии поиска, если имеются
            oSqlQueryVO = addTaskFilterCriteria(oSqlQueryVO, oTaskFilterVO);

            String sQuery = oSqlQueryVO.getsFrom() + oSqlQueryVO.getsWhere();
            LOG.debug("getExecutionFinishedTask sQuery={}", sQuery);
            aoExecutionFinishedTask.addAll(oTaskService.createNativeTaskQuery().sql(sQuery).list());

            if (oTaskFilterVO.getbSearchExternalTasks()) {
                aoExecutionFinishedTask.addAll(
                        getExternalTask(oTaskFilterVO.getsLogin(), TASK, THE_STATUS_OF_TASK_IS_EXECUTION_FINISHED, false, null, false));
            }
        }

        return aoExecutionFinishedTask;
    }

    /**
     * Получить открытые таски задачи, которые на контроле.
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return все открытые таски задачи, которые на контроле для sLogin
     */
    private List<TaskInfo> getControlTask(TaskFilterVO oTaskFilterVO) {
        LOG.debug("getControlTask started with oTaskFilterVO={}", oTaskFilterVO);
        List<TaskInfo> aoControlTask = new ArrayList<>();
        //условие валидация, нельзя исполнять задачу по которой ты контролирующий
        if (!oTaskFilterVO.getsLogin().equals(oTaskFilterVO.getsLoginExecutor())) {
            //если нет sLoginController, то считаем что нужно искать таски, которые на контроле у sLogin (тот кто выполняет поиск)
            if (oTaskFilterVO.getsLoginController() == null) {
                oTaskFilterVO.setsLoginController(oTaskFilterVO.getsLogin());
            }
            //получили запрос, который возвращает все открытые таски
            SqlQueryVO oSqlQueryVO = getOpenedTaskSql(oTaskFilterVO);
            //добавили ограничение именно для данной вкладки (только задачи)
            //2,3,4 - статусы которые соответствуют  викон, не викон, неактуа
            String sWhere = oSqlQueryVO.getsWhere() + "      AND task.\"proc_def_id_\" LIKE '_task%'\n"
                    + "      AND subject.\"nID_ProcessSubjectStatus\" NOT IN ('2', '3', '4')\n";
            oSqlQueryVO.setsWhere(sWhere);
            //добавляем критерии поиска, если имеются
            oSqlQueryVO = addTaskFilterCriteria(oSqlQueryVO, oTaskFilterVO);

            String sQuery = oSqlQueryVO.getsFrom() + oSqlQueryVO.getsWhere();
            LOG.debug("getControlTask sQuery={}", sQuery);
            aoControlTask.addAll(oTaskService.createNativeTaskQuery().sql(sQuery).list());

            if (oTaskFilterVO.getbSearchExternalTasks()) {
                aoControlTask.addAll(
                        getExternalTask(oTaskFilterVO.getsLogin(), TASK, THE_STATUS_OF_TASK_IS_CONTROL, false, null, false));
            }
        }

        return aoControlTask;
    }

    /**
     * Получить завершенные таски задачи, которые были на контроле.
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return все открытые необработанные таски задачи для sLogin
     */
    private List<TaskInfo> getControlFinishedTask(TaskFilterVO oTaskFilterVO) {
        LOG.debug("getControlFinishedTask started with oTaskFilterVO={}", oTaskFilterVO);
        List<TaskInfo> aoControlFinishedTask = new ArrayList<>();
        if (!oTaskFilterVO.getsLogin().equals(oTaskFilterVO.getsLoginExecutor())) {
            //если нет sLoginController, то считаем что нужно искать таски, которые на контроле у sLogin (тот кто выполняет поиск)
            if (oTaskFilterVO.getsLoginController() == null) {
                oTaskFilterVO.setsLoginController(oTaskFilterVO.getsLogin());
            }
            //получили запрос, который возвращает все открытые таски
            SqlQueryVO oSqlQueryVO = getOpenedTaskSql(oTaskFilterVO);
            //добавили ограничение именно для данной вкладки (только задачи)
            //2,3,4 - статусы которые соответствуют  викон, не викон, неактуа
            String sWhere = oSqlQueryVO.getsWhere() + "      AND task.\"proc_def_id_\" LIKE '_task%'\n"
                    + "      AND subject.\"nID_ProcessSubjectStatus\" IN ('2', '3', '4')\n";
            oSqlQueryVO.setsWhere(sWhere);
            //добавляем критерии поиска, если имеются
            oSqlQueryVO = addTaskFilterCriteria(oSqlQueryVO, oTaskFilterVO);

            String sQuery = oSqlQueryVO.getsFrom() + oSqlQueryVO.getsWhere();
            LOG.debug("getControlFinishedTask sQuery={}", sQuery);
            aoControlFinishedTask.addAll(oTaskService.createNativeTaskQuery().sql(sQuery).list());

            if (oTaskFilterVO.getbSearchExternalTasks()) {
                aoControlFinishedTask.addAll(
                        getExternalTask(oTaskFilterVO.getsLogin(), TASK, THE_STATUS_OF_TASK_IS_CONTROL_FINISHED, false, null, false));
            }
        }

        return aoControlFinishedTask;
    }

    /**
     * Получить открытые таски задачи.
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return все открытые таски задачи для sLogin
     */
    private List<TaskInfo> getOpenedTask(TaskFilterVO oTaskFilterVO) {
        LOG.debug("getOpenedTask started with oTaskFilterVO={}", oTaskFilterVO);
        List<TaskInfo> aoOpenedTask = new ArrayList<>();
        //получили запрос, который возвращает все открытые таски
        SqlQueryVO oSqlQueryVO = getOpenedTaskSql(oTaskFilterVO);
        //добавили ограничение именно для данной вкладки (не документы)
        String sWhere = oSqlQueryVO.getsWhere() + "      AND task.\"proc_def_id_\" NOT LIKE '_doc%'\n";
        oSqlQueryVO.setsWhere(sWhere);
        //добавляем критерии поиска, если имеются
        oSqlQueryVO = addTaskFilterCriteria(oSqlQueryVO, oTaskFilterVO);

        String sQuery = oSqlQueryVO.getsFrom() + oSqlQueryVO.getsWhere();
        LOG.debug("getOpenedTask sQuery={}", sQuery);
        aoOpenedTask.addAll(oTaskService.createNativeTaskQuery().sql(sQuery).list());

        if (oTaskFilterVO.getbSearchExternalTasks()) {
            aoOpenedTask.addAll(
                    getExternalTask(oTaskFilterVO.getsLogin(), TASK, THE_STATUS_OF_TASK_IS_OPENED, false, null, false));
        }

        return aoOpenedTask;
    }

    /**
     * Получить запрос для всех открытых тасок (задачи)
     *
     * @param oTaskFilterVO обьект обвертка, который содержит критирии для построения запроса
     * @return обьект обвертку SqlQueryVO, который можно дополнить и из которого строится итоговый запрос
     */
    private SqlQueryVO getOpenedTaskSql(TaskFilterVO oTaskFilterVO) {
        SqlQueryVO oSqlQueryVO = new SqlQueryVO();
        //запрос, который возвращает все открытые таски (задачи)
        String sFrom = "SELECT DISTINCT task.*\n" +
                "FROM \"public\".\"act_ru_task\" task, \"public\".\"act_ru_identitylink\" link,\n" +
                "  \"public\".\"act_ru_execution\" execution\n";
        String sWhere = " WHERE task.\"proc_inst_id_\" = execution.\"proc_inst_id_\"\n" +
                "      AND (task.\"id_\" = link.\"task_id_\" OR execution.\"proc_inst_id_\" = link.\"proc_inst_id_\")\n" +
                "      AND (link.\"group_id_\" IN (SELECT membership.\"group_id_\"\n" +
                "                                FROM \"public\".\"act_id_membership\" membership\n" +
                "                                WHERE membership.\"user_id_\" = '" + oTaskFilterVO.getsLogin() + "')\n" +
                "           OR link.\"user_id_\" = '" + oTaskFilterVO.getsLogin() + "')\n";
        oSqlQueryVO.setsFrom(sFrom);
        oSqlQueryVO.setsWhere(sWhere);

        return oSqlQueryVO;
    }

    /**
     * Получить закрытые таски задачи.
     *
     * @param oTaskFilterVO обьект с набором параметров для поиска
     * @return все закрытые таски задачи для sLogin
     */
    private List<TaskInfo> getClosedTask(TaskFilterVO oTaskFilterVO) {
        LOG.debug("getClosedTask started with oTaskFilterVO={}", oTaskFilterVO);
        List<TaskInfo> aoClosedTask = new ArrayList<>();
        //получили запрос, который возвращает все закрытые таски
        SqlQueryVO oSqlQueryVO = getAllTaskSql(oTaskFilterVO);
        //добавили ограничение именно для данной вкладки (не документы)
        String sWhere = oSqlQueryVO.getsWhere() + "      AND task.\"proc_def_id_\" NOT LIKE '_doc%'\n " +
            "      AND process.\"end_time_\" IS NOT NULL AND task.\"end_time_\" IS NOT NULL";
        oSqlQueryVO.setsWhere(sWhere);
        //добавляем критерии поиска, если имеются
        oSqlQueryVO = addTaskFilterCriteria(oSqlQueryVO, oTaskFilterVO);

        String sQuery = oSqlQueryVO.getsFrom() + oSqlQueryVO.getsWhere();
        LOG.debug("getClosedTask sQuery={}", sQuery);
        aoClosedTask.addAll(
                filterHistoryTasksByEndTime(
                        oHistoryService.createNativeHistoricTaskInstanceQuery().sql(sQuery).list()
                )
        );

        if (oTaskFilterVO.getbSearchExternalTasks()) {
            aoClosedTask.addAll(getExternalTask(oTaskFilterVO.getsLogin(), TASK, THE_STATUS_OF_TASK_IS_CLOSED, false, null, false));
        }

        return aoClosedTask;
    }

    /**
     * Получить запрос для всех закрытых тасок (и документы, и задачи)
     *
     * @param oTaskFilterVO обьект обвертка, который содержит критирии для построения запроса
     * @return обьект обвертку SqlQueryVO, который можно дополнить и из которого строится итоговый запрос
     */
    /*private SqlQueryVO getClosedTaskSql(TaskFilterVO oTaskFilterVO) {
        SqlQueryVO oSqlQueryVO = new SqlQueryVO();
        //запрос, который возвращает все закрытые таски (и документы, и задачи)
        String sFrom = "SELECT DISTINCT task.*\n" +
                "FROM \"public\".\"act_hi_taskinst\" task, \"public\".\"act_hi_identitylink\" link,\n" +
                "  \"public\".\"act_hi_procinst\" process";
        String sWhere = " WHERE task.\"proc_inst_id_\" = process.\"proc_inst_id_\"\n" +
                "      AND (task.\"id_\" = link.\"task_id_\" OR process.\"proc_inst_id_\" = link.\"proc_inst_id_\")\n" +
                "      AND (link.\"group_id_\" = '" + oTaskFilterVO.getsLogin() + "' OR link.\"user_id_\" = '" + oTaskFilterVO.getsLogin() + "')\n" +
                "      AND process.\"end_time_\" IS NOT NULL\n" +
                "      AND task.\"end_time_\" IS NOT NULL\n";
        //по дефолту без удаленных
        if (oTaskFilterVO.getbIncludeDeleted()) {
            sWhere = sWhere + "      AND (process.\"delete_reason_\" IS NULL OR process.\"delete_reason_\" = 'deleted')\n";
        } else {
            sWhere = sWhere + "      AND process.\"delete_reason_\" IS NULL\n";
        }
        oSqlQueryVO.setsFrom(sFrom);
        oSqlQueryVO.setsWhere(sWhere);

        return oSqlQueryVO;
    }*/

    /**
     * Получить все таски (и документы, и задачи, открытые, и закрытые)
     *
     * @param oTaskFilterVO обьект обвертка, который содержит критирии для построения запроса
     * @return обьект обвертку SqlQueryVO, который можно дополнить и из которого строится итоговый запрос
     */
    private SqlQueryVO getAllTaskSql(TaskFilterVO oTaskFilterVO) {
        SqlQueryVO oSqlQueryVO = new SqlQueryVO();
        //запрос, который возвращает все таски (и документы, и задачи)
        String sFrom = "SELECT DISTINCT task.*\n" +
            "FROM \"public\".\"act_hi_taskinst\" task, \"public\".\"act_hi_procinst\" process";
        String sWhere = " WHERE task.\"proc_inst_id_\" = process.\"proc_inst_id_\"\n";
        //доавлено такое условие чтобы бывл возможен поиск и по открытым и закрытым таскам автора
        if (oTaskFilterVO.getsLoginAuthor() != null) {
            sFrom = sFrom + ", \"public\".\"act_hi_varinst\" var\n";
            sWhere = sWhere + "AND var.\"proc_inst_id_\" = task.\"proc_inst_id_\"\n" +
                "      AND var.\"name_\" = 'sLoginAuthor' AND var.\"text_\" = '" + oTaskFilterVO.getsLoginAuthor() + "'\n";
        } else {
            sFrom = sFrom + ", \"public\".\"act_hi_identitylink\" link\n";
            sWhere = sWhere + "      AND (task.\"id_\" = link.\"task_id_\" OR process.\"proc_inst_id_\" = link.\"proc_inst_id_\")\n" +
                "      AND (link.\"group_id_\" IN (SELECT membership.\"group_id_\"\n" +
                "                                FROM \"public\".\"act_id_membership\" membership\n" +
                "                                WHERE membership.\"user_id_\" = '" + oTaskFilterVO.getsLogin() + "')\n" +
                "           OR link.\"user_id_\" = '" + oTaskFilterVO.getsLogin() + "')\n";
        }
        //по дефолту без удаленных
        if (oTaskFilterVO.getbIncludeDeleted()) {
            sWhere = sWhere + "      AND (process.\"delete_reason_\" IS NULL OR process.\"delete_reason_\" = 'deleted')\n";
        } else {
            sWhere = sWhere + "      AND process.\"delete_reason_\" IS NULL\n";
        }
        oSqlQueryVO.setsFrom(sFrom);
        oSqlQueryVO.setsWhere(sWhere);

        return oSqlQueryVO;
    }

    /**
     * Добавить критерии поиска.
     *
     * @param oSqlQueryVO   запрос к которому нужно добавить критерии поиска
     * @param oTaskFilterVO обьет, который содержит критерии поиска
     * @return обьект обвертку с запросом, который содержит критерии для поиска
     */
    private SqlQueryVO addTaskFilterCriteria(SqlQueryVO oSqlQueryVO, TaskFilterVO oTaskFilterVO) {
        String sFrom = oSqlQueryVO.getsFrom();
        String sWhere = oSqlQueryVO.getsWhere();
        //==============================================================================================================
        //по названию процесса
        if (oTaskFilterVO.getsProcessDefinitionKey() != null) {
            sWhere = sWhere + " AND task.\"proc_def_id_\" LIKE '" + oTaskFilterVO.getsProcessDefinitionKey() + "%'\n";
        }
        //==============================================================================================================
        //по дате ( + 23:59 чтобы сделать включительно)
        //для закрытых тасок столбец даты старта start_time_
        if (oTaskFilterVO.getsDateFrom() != null && !"".equals(oTaskFilterVO.getsDateFrom().trim())
                && oTaskFilterVO.getsDateType().equals("startTime") && sFrom.contains("act_hi_taskinst")) {
            sWhere = sWhere + "      AND task.\"start_time_\" >= '" + oTaskFilterVO.getsDateFrom() + "'\n";
        }
        if (oTaskFilterVO.getsDateTo() != null && !"".equals(oTaskFilterVO.getsDateTo().trim())
                && oTaskFilterVO.getsDateType().equals("startTime") && sFrom.contains("act_hi_taskinst")) {
            sWhere = sWhere + "      AND task.\"start_time_\" <= '" + oTaskFilterVO.getsDateTo() + " 23:59'\n";
        }
        //для активных тасок столбец даты старта create_time_
        if (oTaskFilterVO.getsDateFrom() != null && !"".equals(oTaskFilterVO.getsDateFrom().trim())
                && oTaskFilterVO.getsDateType().equals("startTime") && sFrom.contains("act_ru_task")) {
            sWhere = sWhere + "      AND task.\"create_time_\" >= '" + oTaskFilterVO.getsDateFrom() + "'\n";
        }
        if (oTaskFilterVO.getsDateTo() != null && !"".equals(oTaskFilterVO.getsDateTo().trim())
                && oTaskFilterVO.getsDateType().equals("startTime") && sFrom.contains("act_ru_task")) {
            sWhere = sWhere + "      AND task.\"create_time_\" <= '" + oTaskFilterVO.getsDateTo() + " 23:59'\n";
        }
        if (oTaskFilterVO.getsDateFrom() != null && !"".equals(oTaskFilterVO.getsDateFrom().trim())
                && oTaskFilterVO.getsDateType().equals("endTime")) {
            sWhere = sWhere + "      AND process.\"end_time_\" >= '" + oTaskFilterVO.getsDateFrom() + "'\n";
        }
        if (oTaskFilterVO.getsDateTo() != null && !"".equals(oTaskFilterVO.getsDateTo().trim())
                && oTaskFilterVO.getsDateType().equals("endTime")) {
            sWhere = sWhere + "      AND process.\"end_time_\" <= '" + oTaskFilterVO.getsDateTo() + " 23:59'\n";
        }
        if (oTaskFilterVO.getsDateFrom() != null && !"".equals(oTaskFilterVO.getsDateFrom().trim())
                && oTaskFilterVO.getsDateType().equals("executionTime")) {
            sWhere = sWhere + "      AND subject.\"sDatePlan\" >= '" + oTaskFilterVO.getsDateFrom() + "'\n";
        }
        if (oTaskFilterVO.getsDateTo() != null && !"".equals(oTaskFilterVO.getsDateTo().trim())
                && oTaskFilterVO.getsDateType().equals("executionTime")) {
            sWhere = sWhere + "      AND subject.\"sDatePlan\" <= '" + oTaskFilterVO.getsDateTo() + " 23:59'\n";
        }
        //==============================================================================================================
        //по участникам задания
        if (oTaskFilterVO.getsLoginController() != null || oTaskFilterVO.getsLoginExecutor() != null
                || (oTaskFilterVO.getsDateType() != null && oTaskFilterVO.getsDateType().equals("executionTime"))) {
            sFrom = sFrom + ", \"ProcessSubject\" subject";
            sWhere = sWhere + "      AND task.\"id_\" = subject.\"snID_Task_Activiti\"\n";
        }
        if (oTaskFilterVO.getsLoginController() != null) {
             /*Данное условие добавлено для того чтобы мог отработать кейс, когда передается логин контролирующего и
            исполнителя.*/
            if (oTaskFilterVO.getsLoginExecutor() != null
                    && oTaskFilterVO.getsFilterStatus().equals(THE_STATUS_OF_TASK_IS_EXECUTION)) {
                sWhere = sWhere + "AND exists(SELECT subject.*\n" +
                        "                 FROM \"ProcessSubject\" subject\n" +
                        "                 WHERE subject.\"sLogin\" = '" + oTaskFilterVO.getsLoginController() + "'\n" +
                        "                       AND subject.\"sLoginRole\" = 'Controller'\n" +
                        "                       AND task.\"proc_inst_id_\" = subject.\"snID_Process_Activiti\")";
            } else {
                sWhere = sWhere + "      AND subject.\"sLogin\" = '" + oTaskFilterVO.getsLoginController() + "'" +
                        " AND subject.\"sLoginRole\" = 'Controller'";
            }
        }
        if (oTaskFilterVO.getsLoginExecutor() != null) {
            /*Данное условие добавлено для того чтобы мог отработать кейс, когда передается логин контролирующего и
            исполнителя.*/
            if (oTaskFilterVO.getsLoginController() != null
                    && oTaskFilterVO.getsFilterStatus().equals(THE_STATUS_OF_TASK_IS_CONTROL)) {
                sWhere = sWhere + "AND exists(SELECT subject.*\n" +
                        "                 FROM \"ProcessSubject\" subject\n" +
                        "                 WHERE subject.\"sLogin\" = '" + oTaskFilterVO.getsLoginExecutor() + "'\n" +
                        "                       AND subject.\"sLoginRole\" = 'Executor'\n" +
                        "                       AND task.\"proc_inst_id_\" = subject.\"snID_Process_Activiti\")";
            } else {
                sWhere = sWhere + "      AND subject.\"sLogin\" = '" + oTaskFilterVO.getsLoginExecutor() + "'" +
                        " AND subject.\"sLoginRole\" = 'Executor'";
            }
        }
        //==============================================================================================================
        //по тексту по полям
        if (oTaskFilterVO.getsFind() != null ||
                (oTaskFilterVO.getAoFilterField() != null && !oTaskFilterVO.getAoFilterField().isEmpty())) {
            sFrom = sFrom + ", \"public\".\"act_hi_detail\" detail\n";
            sWhere = sWhere + "      AND detail.\"proc_inst_id_\" = task.\"proc_inst_id_\"\n";
        }
        if (oTaskFilterVO.getsFind() != null) {
            sWhere = sWhere + "      AND detail.\"text_\" LIKE '%" + oTaskFilterVO.getsFind() + "%'\n";
        }
        if (oTaskFilterVO.getAoFilterField() != null && !oTaskFilterVO.getAoFilterField().isEmpty()) {
            sWhere = sWhere + parseFilterField(oTaskFilterVO.getAoFilterField());
        }

        oSqlQueryVO.setsFrom(sFrom);
        oSqlQueryVO.setsWhere(sWhere);

        return oSqlQueryVO;
    }

    /**
     * Распарсить FilterFieldVO в sql
     *
     * @param aoFilterFieldVO обьет, который содержит критерии поиска
     * @return возвращает sql с доп условияммя для поиска
     */
    private String parseFilterField(List<FilterFieldVO> aoFilterFieldVO) {
        String sWhere = "";
        for (FilterFieldVO oFilterFieldVO : aoFilterFieldVO) {
            if (oFilterFieldVO.validate()) {
                sWhere = "      AND detail.\"name_\" = '" + oFilterFieldVO.getsID_Field()
                        + "' AND detail.\"text_\"" + oFilterFieldVO.parseOperation() + "\n";
            } else {
                throw new IllegalArgumentException("Wrong filter field parameter " + oFilterFieldVO);
            }
        }
        return sWhere;
    }

    /**
     * Author - Yegor Kovylin
     *
     * @param snID_Task    - task id
     * @param snID_Process - process id
     * @param sLoginTab   - the user from whom we accessing the task
     */
    public void checkTaskPermission(String snID_Task, String snID_Process, String sLoginTab) {
        LOG.info("checkTaskPermission start with {}, {}, {}", snID_Task, snID_Process, sLoginTab);
        int nLoginContained_Count = oIdentityService.createNativeGroupQuery()
                .sql("SELECT \"group_id_\"\n" +
                        "FROM \"public\".\"act_hi_identitylink\"\n" +
                        "WHERE \"task_id_\" = \'" + snID_Task + "\' AND (\"group_id_\" IN (SELECT membership.\"group_id_\"\n" +
                        "                                                 FROM \"public\".\"act_id_membership\" membership\n" +
                        "                                                 WHERE membership.\"user_id_\" = '" + sLoginTab + "')\n" +
                        "                                 OR \"user_id_\" = '" + sLoginTab + "')")
                .list().size()
                +
                oIdentityService.createNativeGroupQuery()
                        .sql("SELECT \"user_id_\"\n" +
                                "FROM \"public\".\"act_hi_identitylink\"\n" +
                                "WHERE \"proc_inst_id_\" = '" + snID_Process + "' AND \"user_id_\" = '" + sLoginTab + "'")
                        .list().size();

        if (nLoginContained_Count == 0) {
            /*
            //if we can't find any group or user in identityLinks  - maybe we work with a task from central, where groups are not personalize
            //chek it:
            List<IdentityLink> aIdentityLink_task = oTaskService.getIdentityLinksForTask(nID_Task);
            List<Group> aGroup = oIdentityService.createGroupQuery().groupMember(sLoginTab).list();
            for (Group oGroup : aGroup) {
                LOG.info("oGroup is {}", oGroup.getId());
                for (IdentityLink oIdentityLink_Task : aIdentityLink_task) {
                    LOG.info("oGroup_Task is {}", oIdentityLink_Task.getGroupId());
                    if (oGroup.getId().equals(oIdentityLink_Task.getGroupId())) {
                        loginContained_Counted++;
                    }
                }
            }
            */
            //if we still can't find any group or user in identityLinks - then user acessing unfamiliar task
            if (nLoginContained_Count == 0) {
                DocumentStep oDocumentStep = oDocumentStepDao.getDocumentStepByID_ProcessAndName(snID_Process, "_");
                LOG.info("oDocumentStep is {}", oDocumentStep.getsKey_Step());
                if (oDocumentStep != null) {
                    List<DocumentStepSubjectRight> aDocumentStepSubjectRight = oDocumentStep.aDocumentStepSubjectRight();
                    for (DocumentStepSubjectRight oDocumentStepSubjectRight : aDocumentStepSubjectRight) {
                        if (sLoginTab.equals(oDocumentStepSubjectRight.getsKey_GroupPostfix())) {
                            nLoginContained_Count++;
                        }
                    }
                }
            }

            if (nLoginContained_Count == 0) {
                throw new RuntimeException(ACCESS_DENIED);
            }
        }
    }

    /**
     * @param sIDUserTask          - usertask form id
     * @param nID_Process          - process id
     * @param sProcessDefinitionId - process definition
     * @return list of HistoryVariableVO-objects - history fields in normalized view
     * @Author - Yegor Kovylin
     */
    public List<HistoryVariableVO> getHistoryFields(String sIDUserTask, String nID_Process, String sProcessDefinitionId) {
        List<HistoryVariableVO> aResultField = new ArrayList<>();
        UserTask oUserTask = getUserTask(sIDUserTask, nID_Process, sProcessDefinitionId);
        LOG.info("oUserTask name {}", oUserTask.getName());

        List<org.activiti.bpmn.model.FormProperty> aTaskFormProperty = null;

        aTaskFormProperty = oUserTask.getFormProperties();

        if (aTaskFormProperty == null) {
            throw new RuntimeException("Can't find any property for current usertask");
        }

        List<HistoricVariableInstance> aHistoricVariableInstance = oHistoryService.createHistoricVariableInstanceQuery()
                .processInstanceId(nID_Process.toString()).list();

        for (org.activiti.bpmn.model.FormProperty oFormProperty : aTaskFormProperty) {
            LOG.info("oFormProperty id is {}", oFormProperty.getId());

            for (HistoricVariableInstance oHistoricVariableInstance : aHistoricVariableInstance) {
                if (oFormProperty.getId().equals(oHistoricVariableInstance.getVariableName())) {
                    LOG.info("oHistoricVariableInstance name {}", oHistoricVariableInstance.getVariableName());

                    if (oFormProperty.getName().contains("bVisible=false")) {
                        break;
                    }

                    HistoryVariableVO oHistoryVariableVO = new HistoryVariableVO();
                    oHistoryVariableVO.setsId(oFormProperty.getId());
                    oHistoryVariableVO.setsName(oFormProperty.getName().split(";")[0]);
                    oHistoryVariableVO.setsType(oFormProperty.getType());
                    oHistoryVariableVO.setbReadable(oFormProperty.isReadable());
                    oHistoryVariableVO.setoValue(oHistoricVariableInstance.getValue());

                    if (oFormProperty.getType().equals("file") || oFormProperty.getType().equals("table")) {
                        if (oHistoryVariableVO.getoValue() != null && ((String) oHistoryVariableVO.getoValue()).contains("sKey")) {
                            aResultField.add(oHistoryVariableVO);
                        }
                    } else if (oFormProperty.getType().equals("enum")) {
                        LOG.info("oHistoryVariableVO enum case before editing {}", oHistoryVariableVO);
                        List<FormValue> aEnumFormProperty = oFormProperty.getFormValues();

                        for (FormValue oEnumFormProperty : aEnumFormProperty) {

                            if (oHistoricVariableInstance.getValue() != null
                                    && oHistoricVariableInstance.getValue().equals(oEnumFormProperty.getId())) {
                                LOG.info("oEnumFormProperty id {}", oEnumFormProperty.getId());
                                oHistoryVariableVO.setoValue(oEnumFormProperty.getName());
                                break;
                            }
                        }
                        aResultField.add(oHistoryVariableVO);
                    } else {
                        aResultField.add(oHistoryVariableVO);
                    }
                } else if (oFormProperty.getId().startsWith("sBody_")) { //in case of printform-variable - we should return it for front

                    boolean isNewPrintform = true;

                    for (HistoryVariableVO oHistoryVariableVO_Added : aResultField) {
                        if (oHistoryVariableVO_Added.getsId().equals(oFormProperty.getId())) {
                            isNewPrintform = false;
                            break;
                        }
                    }

                    if (isNewPrintform) {
                        HistoryVariableVO oHistoryVariableVO = new HistoryVariableVO();
                        oHistoryVariableVO.setsId(oFormProperty.getId());
                        oHistoryVariableVO.setsName(oFormProperty.getName());
                        oHistoryVariableVO.setsType(oFormProperty.getType());
                        oHistoryVariableVO.setoValue(oFormProperty.getDefaultExpression());
                        aResultField.add(oHistoryVariableVO);
                    }
                }
            }
        }

        return aResultField;
    }
    
    
    public UserTask getUserTask(String sIDUserTask, String nID_Process, String sProcessDefinitionId){
        
        LOG.info("getTaskData try to find history variables");
        List<Task> aoTaskActive = oTaskService.createTaskQuery()
                .processInstanceId(nID_Process)
                .active()
                .list();

        Task oTaskActive = null;
        if (!aoTaskActive.isEmpty()) {
            oTaskActive = aoTaskActive.get(0);
        }
        String sTaskDefinitionActive = oTaskActive != null ? oTaskActive.getTaskDefinitionKey() : null;
        LOG.info("sTaskDefinitionActive is {}", sTaskDefinitionActive);

        BpmnModel model = oRepositoryService.getBpmnModel(sProcessDefinitionId);
        List<org.activiti.bpmn.model.Process> aProcess = model.getProcesses();

        UserTask oUserTask = null;

        if (aProcess != null) {
            LOG.info("oProcess is {}", aProcess.get(0).getId());

            for (Object oFlowElement : aProcess.get(0).getFlowElements()) {
                if (oFlowElement instanceof UserTask) {

                    UserTask oUserTask_Curr = (UserTask) oFlowElement;
                    LOG.info("oUserTask_Curr is {}", oUserTask_Curr.getId());
                    if (sIDUserTask.equals(oUserTask_Curr.getId())) {
                        oUserTask = oUserTask_Curr;
                    }

                    if (sTaskDefinitionActive != null && oUserTask_Curr.getId().equals(sTaskDefinitionActive)) {
                        LOG.info("oUserTask before active is {}", oUserTask_Curr.getId());
                        break;
                    }
                }
            }

        } else {
            throw new RuntimeException("Can't find bpmn model for current process");
        }

        if (oUserTask == null) {
            throw new RuntimeException("Can't find any userTask for current process");
        }
        
        return oUserTask;
    }
    
    /**
     * @param sSessionLogin
     * @param sID_BP
     * @throws NotFoundException
     * @author Yegor Kovylin
     * cheks if login has access to creating process
     */
    public void checkSessionPermition(String sSessionLogin, String sID_BP) throws NotFoundException {
        LOG.info("checkSessionPermition started...");
        LOG.info("sSessionLogin is {}", sSessionLogin);
        LOG.info("sID_BP is {}", sID_BP);
        List<Map<String, String>> mAllBPs = new ArrayList<>();
        mAllBPs.addAll(oActionProcessServcie.getBPsForParticipant(sSessionLogin, null));
        LOG.info("mAllBPs Participant {}", mAllBPs);
        List<SubjectRightBPVO> aoSubjectRightBPVO = oSubjectRightBPService.getBPs_ForReferent_bysLogin(sSessionLogin);

        aoSubjectRightBPVO.forEach(oSubjectRightBPVO -> {
            Map<String, String> mBpID_Name = new HashMap<>();
            mBpID_Name.put("sID", oSubjectRightBPVO.getoSubjectRightBP().getsID_BP());
            mBpID_Name.put("sName", oSubjectRightBPVO.getsName_BP());
            mAllBPs.add(mBpID_Name);
        });
        LOG.info("mAllBPs Referent {}", mAllBPs);
        LOG.info("mAllBPs is {}", mAllBPs);
        boolean bHavePermition = false;

        for (Map<String, String> mBP : mAllBPs) {
            for (String key : mBP.keySet()) {
                LOG.info("key {}", key);
                if (mBP.get(key).equals(sID_BP)) {
                    bHavePermition = true;
                    break;
                }
            }
        }

        if (!bHavePermition) {
            throw new RuntimeException(ACCESS_DENIED);
        }
    }

    /**
     * Добавить права активити для доступа к таске
     *  @param snID_Process_Activiti ид процесса-документа
     * @param sLogin логин которому нужно дать права
     */
    public void addIdentityLinkToDocument(String snID_Process_Activiti, String sLogin) {
        LOG.info("addIdentityLinkToDocument was started");
        LOG.info("snID_Process_Activiti {}", snID_Process_Activiti);
        LOG.info("sLogin {}", sLogin);

        List<Task> aoTask = oTaskService.createTaskQuery().processInstanceId(snID_Process_Activiti).active().list();
        if (!aoTask.isEmpty()) {
            //процесс не закрыт даем права через сервис актитвити
            for(Task oTask : aoTask) {
                /*long loginContained_Count = oIdentityService.createNativeGroupQuery()
                        .sql("SELECT COUNT(\"group_id_\")" +
                                " FROM \"public\".\"act_hi_identitylink\"" +
                                " WHERE \"task_id_\" = \'" + oTask.getId() + "\' AND \"group_id_\" = \'" + sLogin + "\'")
                        .count()
                        +
                        oIdentityService.createNativeGroupQuery()
                                .sql("SELECT COUNT(\"user_id_\")" +
                                        " FROM \"public\".\"act_hi_identitylink\"" +
                                        " WHERE \"proc_inst_id_\" = \'" + snID_Process_Activiti + "\' AND \"user_id_\" = \'" + sLogin + "\'")
                                .count();
                LOG.info("loginContained_Count is {}", loginContained_Count);

                if (loginContained_Count == 0) {
                    oTaskService.addCandidateGroup(oTask.getId(), sLogin);
                }
                */
                oTaskService.addCandidateGroup(oTask.getId(), sLogin);
            }
        } else {
            //in case when we add to view from history
            List<HistoricTaskInstance> aHistoricTaskInstance = oHistoryService.createHistoricTaskInstanceQuery()
                    .processInstanceId(snID_Process_Activiti)
                    .orderByHistoricTaskInstanceEndTime()
                    .desc()
                    .list();
            if (aHistoricTaskInstance != null && !aHistoricTaskInstance.isEmpty()) {
                HistoricTaskInstance oHistoricTaskInstance = aHistoricTaskInstance.get(0);
                //максимальное кол-во записей увеличиваем на 1 для вставки
                String sqlQuery = "INSERT INTO \"public\".\"act_hi_identitylink\"(\"id_\", \"group_id_\",\"type_\",\"user_id_\",\"task_id_\",\"proc_inst_id_\") VALUES("
                        + "(SELECT CAST((SELECT MAX(CAST(\"id_\" as int)) + 1 FROM \"public\".\"act_hi_identitylink\") as text)), \'" + sLogin + "\', 'candidate', null, \'" + oHistoricTaskInstance.getId() + "\', null)";
                LOG.debug("addIdentityLinkToDocument sql query {}", sqlQuery);

                oIdentityService.createNativeUserQuery().sql(sqlQuery).singleResult();
            }
        }
    }

    public List<TaskInfo> getUrgentDocuments(TaskFilterVO oTaskFilterVO) {
        List<TaskInfo> aoUrgentTask = new ArrayList<>();

        if (oTaskFilterVO.getsFilterStatus().equals("OpenedCreatorDocument")) {
            aoUrgentTask.addAll(getUrgentOpenedCreatorDocument(oTaskFilterVO.getsLogin()));
        }
        if (oTaskFilterVO.getsFilterStatus().equals("DocumentOpenedUnassignedUnprocessed")) {
            aoUrgentTask.addAll(getUrgentOpenedUnassignedUnprocessedDocument(oTaskFilterVO));
        }

        return aoUrgentTask;
    }

    /**
     * Проверка нужно ли отдавать дополнительную информацию по таскам.
     *
     * @param oTaskFilterVO обьект, который содержит параметры для поиска тасок
     * @return результат, нужно ли добавлять дополнительную информацию
     */
    public boolean needAdditionalTaskInfo(TaskFilterVO oTaskFilterVO) {
        Boolean bNeedAdditionalTaskInfo = false;
        //Список вкладок для которых нужно отдавать дополнительную информацию
        if (oTaskFilterVO.getsFilterStatus().equals(THE_STATUS_OF_TASK_IS_EXECUTION_FINISHED)
                || oTaskFilterVO.getsFilterStatus().equals(THE_STATUS_OF_TASK_IS_EXECUTION)
                || oTaskFilterVO.getsFilterStatus().equals(THE_STATUS_OF_TASK_IS_CONTROL_FINISHED)
                || oTaskFilterVO.getsFilterStatus().equals(THE_STATUS_OF_TASK_IS_EXECUTION_FINISHED)) {

            bNeedAdditionalTaskInfo = true;
        }

        return bNeedAdditionalTaskInfo;
    }

    /**
     *Добавить дополнительную инфрмацию к таскам. (Сразу будет реализовано логин, ФИО и должность)
     *
     * @param aoTaskDataVO лист тасок, к которым нужно добавить доп инфу
     * @return лист тасок с дополнительной информацией
     */
    public List<TaskDataVO> addAdditionalTaskInfo(List<TaskDataVO> aoTaskDataVO) {
        LOG.info("addAdditionalTaskInfo start...");
        List<String> asTaskId = aoTaskDataVO.stream()
                .map(TaskDataVO::getId)
                .collect(Collectors.toList());
        LOG.info("addAdditionalTaskInfo task id count {}", asTaskId.size());

        List<ProcessSubject> aoProcessSubject = oProcessSubjectDao.findAllByInValues("snID_Task_Activiti", asTaskId);
        LOG.info("addAdditionalTaskInfo aoProcessSubject count {}", aoProcessSubject.size());

        Map<String, Long> mTaskId_ProcessSubjectTaskId = aoProcessSubject.stream().collect(
                Collectors.toMap(ProcessSubject::getSnID_Task_Activiti, ProcessSubject::getnID_ProcessSubjectTask));
        LOG.info("mTaskId_ProcessSubjectTaskId {}", mTaskId_ProcessSubjectTaskId);

        List<Long> anProcessSubjectTaskId = new ArrayList<>(mTaskId_ProcessSubjectTaskId.values());
        LOG.info("anProcessSubjectTaskId {}", anProcessSubjectTaskId);

        aoProcessSubject = oProcessSubjectDao.findAllByInValues("nID_ProcessSubjectTask", anProcessSubjectTaskId);
        LOG.info("all related aoProcessSubject count {}", aoProcessSubject.size());

        Map<String, String> mTaskId_sID_Group_Activiti = new HashMap<>();
        for (Map.Entry<String, Long> entry : mTaskId_ProcessSubjectTaskId.entrySet()) {
            String snTaskId = entry.getKey();
            Long nProcessSubjectTaskId = entry.getValue();
            List<ProcessSubject> aoProcessSubject_ForCurrentTask = aoProcessSubject.stream()
                    .filter(oProcessSubject -> nProcessSubjectTaskId.equals(oProcessSubject.getnID_ProcessSubjectTask()))
                    .collect(Collectors.toList());
            LOG.info("addAdditionalTaskInfo sorting task work {}", aoProcessSubject_ForCurrentTask);
        }

        return aoTaskDataVO;
    }
}
