package org.igov.service.business.action.task.systemtask.mail;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.ByteArrayDataSource;
import org.igov.io.GeneralConfig;
import org.igov.io.db.kv.statical.IBytesDataStorage;
import org.igov.io.fs.FileSystemDictonary;
import org.igov.io.mail.Mail;
import org.igov.io.sms.ManagerSMS;
import org.igov.model.document.image.DocumentImageFileVO;
import org.igov.service.business.access.AccessKeyService;
import org.igov.service.business.action.event.HistoryEventService;
import org.igov.service.business.action.task.core.AbstractModelTask;
import org.igov.service.business.action.task.core.ActionTaskService;
import org.igov.service.business.action.task.systemtask.misc.CancelTaskUtil;
import org.igov.service.business.document.image.DocumentImageService;
import org.igov.service.business.finance.Currency;
import org.igov.service.business.finance.Liqpay;
import org.igov.service.business.object.Language;
import org.igov.service.business.place.PlaceService;
import org.igov.service.business.util.CustomRegexPattern;
import org.igov.service.business.util.DateUtilFormat;
import org.igov.service.controller.security.AccessContract;
import org.igov.service.controller.security.AuthenticationTokenSelector;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.igov.util.Tool;
import org.igov.util.ToolWeb;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataSource;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.igov.io.fs.FileSystemData.getFileData_Pattern;
import static org.igov.util.ToolLuna.getProtectedNumber;

public abstract class Abstract_MailTaskCustom extends AbstractModelTask implements JavaDelegate, CustomRegexPattern {

    static final transient Logger LOG = LoggerFactory
            .getLogger(Abstract_MailTaskCustom.class);

    @Value("${general.Mail.sHost}")
    public String mailServerHost;
    @Value("${general.Mail.nPort}")
    public String mailServerPort;
    @Value("${general.Mail.sAddressDefaultFrom}")
    public String mailServerDefaultFrom;
    @Value("${general.Mail.sUsername}")
    public String mailServerUsername;
    @Value("${general.Mail.sPassword}")
    public String mailServerPassword;
    @Value("${general.Mail.sAddressNoreply}")
    public String mailAddressNoreplay;
    @Value("${general.Mail.bUseSSL}")
    private boolean bSSL;
    @Value("${general.Mail.bUseTLS}")
    private boolean bTLS;

    public Expression from;
    public Expression to;
    public Expression subject;
    public Expression text;
    protected Expression nID_Subject;

    @Autowired
    public HistoryService historyService;

    @Autowired
    public ManagerSMS ManagerSMS;

    @Autowired
    public AccessKeyService accessCover;
    @Autowired
    public GeneralConfig generalConfig;
    @Autowired
    private ApplicationContext context;
    @Autowired
    public Liqpay liqBuy;
    @Autowired
    private CancelTaskUtil cancelTaskUtil;
    
    @Autowired
    private HistoryEventService historyEventService;
    @Autowired
    private IBytesDataStorage durableBytesDataStorage;
    @Autowired
    private PlaceService placeService;
    @Autowired
    private DocumentImageService oDocumentImageService;

    protected String replaceTags(String sTextSource, DelegateExecution execution)
            throws Exception {

        if (sTextSource == null) {
            return null;
        }

        String sTextReturn = sTextSource;

        sTextReturn = replaceTags_LIQPAY(sTextReturn, execution);

        sTextReturn = populatePatternWithContent(sTextReturn);

        sTextReturn = replaceTags_Enum(sTextReturn, execution);

        sTextReturn = replaceTags_Catalog(sTextReturn, execution);

        sTextReturn = new FileSystemDictonary()
                .replaceMVSTagWithValue(sTextReturn);

        Long nID_Order = getProtectedNumber(Long.valueOf(execution
                .getProcessInstanceId()));

        if (sTextReturn.contains(TAG_nID_Protected)) {
            sTextReturn = sTextReturn.replaceAll("\\Q" + TAG_nID_Protected
                    + "\\E", "" + nID_Order);
        }

        if (sTextReturn.contains(TAG_sID_Order)) {
            String sID_Order = generalConfig.getOrderId_ByOrder(nID_Order);
            sTextReturn = sTextReturn.replaceAll("\\Q" + TAG_sID_Order + "\\E",
                    "" + sID_Order);
        }

        if (sTextReturn.contains(TAG_CANCEL_TASK)) {
            String sHTML_CancelTaskButton = cancelTaskUtil.getCancelFormHTML(
                    nID_Order, false);
            sTextReturn = sTextReturn.replace(TAG_CANCEL_TASK,
                    sHTML_CancelTaskButton);
        }

        if (sTextReturn.contains(TAG_CANCEL_TASK_SIMPLE)) {
            String sHTML_CancelTaskButton = cancelTaskUtil.getCancelFormHTML(
                    nID_Order, true);
            sTextReturn = sTextReturn.replace(TAG_CANCEL_TASK_SIMPLE,
                    sHTML_CancelTaskButton);
        }

        if (sTextReturn.contains(TAG_nID_SUBJECT)) {
            sTextReturn = sTextReturn.replaceAll("\\Q" + TAG_nID_SUBJECT
                    + "\\E", "" + nID_Subject);
        }

        if (sTextReturn.contains(TAG_sDateCreate)) {
            Date oProcessInstanceStartDate = historyService
                    .createProcessInstanceHistoryLogQuery(
                            execution.getProcessInstanceId()).singleResult()
                    .getStartTime();
            DateTimeFormatter formatter = JsonDateTimeSerializer.DATETIME_FORMATTER;
            String sDateCreate = formatter.print(oProcessInstanceStartDate
                    .getTime());
            sTextReturn = sTextReturn.replaceAll("\\Q" + TAG_sDateCreate
                    + "\\E", "" + sDateCreate);
        }

        sTextReturn = replaceTags_sURL_SERVICE_MESSAGE(sTextReturn, execution,
                nID_Order);
        sTextReturn = replaceTags_sURL_FEEDBACK_MESSAGE(sTextReturn, execution,
                nID_Order);

        return sTextReturn;
    }

    private String replaceTags_Enum(String textWithoutTags,
            DelegateExecution execution) {
        List<String> previousUserTaskId = getPreviousTaskId(execution);
        int nLimit = StringUtils.countMatches(textWithoutTags,
                TAG_Function_AtEnum);

        Map<String, FormProperty> aProperty = new HashMap<>();
        int foundIndex = 0;
        while (nLimit > 0) {
            nLimit--;
            int nAt = textWithoutTags.indexOf(TAG_Function_AtEnum, foundIndex);
            foundIndex = nAt + 1;
            int nTo = textWithoutTags.indexOf(TAG_Function_To, foundIndex);
            foundIndex = nTo + 1;
            String sTAG_Function_AtEnum = textWithoutTags.substring(nAt
                    + TAG_Function_AtEnum.length(), nTo);

            if (aProperty.isEmpty()) {
                loadPropertiesFromTasks(execution, previousUserTaskId,
                        aProperty);
            }
            boolean bReplaced = false;
            for (FormProperty property : aProperty.values()) {
                String sType = property.getType().getName();
                String snID = property.getId();
                if (!bReplaced && "enum".equals(sType)
                        && sTAG_Function_AtEnum.equals(snID)) {

                    Object variable = execution.getVariable(property.getId());
                    if (variable != null) {
                        String sID_Enum = variable.toString();
                        String sValue = ActionTaskService.parseEnumProperty(
                                property, sID_Enum);

                        textWithoutTags = textWithoutTags.replaceAll("\\Q"
                                + TAG_Function_AtEnum + sTAG_Function_AtEnum
                                + TAG_Function_To + "\\E", sValue);
                        bReplaced = true;
                    }
                }
            }

        }
        return textWithoutTags;
    }

    private String replaceTags_Catalog(String textStr,
            DelegateExecution execution) throws Exception {
        StringBuffer outputTextBuffer = new StringBuffer();
        String replacement = "";
        Matcher matcher = TAG_sPATTERN_CONTENT_CATALOG.matcher(textStr);
        if (matcher.find()) {
            matcher = TAG_sPATTERN_CONTENT_CATALOG.matcher(textStr);
            List<String> aPreviousUserTask_ID = getPreviousTaskId(execution);
            Map<String, FormProperty> mProperty = new HashMap<>();
            loadPropertiesFromTasks(execution, aPreviousUserTask_ID, mProperty);
            while (matcher.find()) {
                String tag_Payment_CONTENT_CATALOG = matcher.group();
                if (!tag_Payment_CONTENT_CATALOG
                        .startsWith(TAG_Function_AtEnum)) {
                    String prefix;
                    Matcher matcherPrefix = TAG_PATTERN_DOUBLE_BRACKET
                            .matcher(tag_Payment_CONTENT_CATALOG);
                    if (matcherPrefix.find()) {
                        prefix = matcherPrefix.group();
                        String form_ID = StringUtils.replace(prefix, "{[", "");
                        form_ID = StringUtils.replace(form_ID, "]}", "");
                        FormProperty formProperty = mProperty.get(form_ID);
                        if (formProperty != null && formProperty.getValue() != null) {
                            replacement = formProperty.getValue();
                        } else {
                            List<String> aID = new ArrayList<>();
                            aID.add(form_ID);
                            List<String> proccessVariable = AbstractModelTask
                                    .getVariableValues(execution, aID);
                            if (!proccessVariable.isEmpty()
                                    && proccessVariable.get(0) != null) {
                                replacement = proccessVariable.get(0);
                            }
                        }

                        if (formProperty != null) {
                            String sType = formProperty.getType().getName();
                            if ("date".equals(sType)) {
                                if (formProperty.getValue() != null) {
                                    replacement = getFormattedDateS(formProperty
                                            .getValue());
                                }
                            }
                        }
                    }
                }
                matcher.appendReplacement(outputTextBuffer, replacement);
                replacement = "";
            }
        }
        return matcher.appendTail(outputTextBuffer).toString();
    }

    private String replaceTags_LIQPAY(String textStr,
            DelegateExecution execution) throws Exception {
        String LIQPAY_CALLBACK_URL = generalConfig.getSelfHost()
                + "/wf/service/finance/setPaymentStatus_TaskActiviti?sID_Order=%s&sID_PaymentSystem=Liqpay&sData=%s&sPrefix=%s";

        StringBuffer outputTextBuffer = new StringBuffer();
        Matcher matcher = TAG_PAYMENT_BUTTON_LIQPAY.matcher(textStr);
        while (matcher.find()) {

            String tag_Payment_Button_Liqpay = matcher.group();
            String prefix = "";
            Matcher matcherPrefix = TAG_PATTERN_PREFIX
                    .matcher(tag_Payment_Button_Liqpay);
            if (matcherPrefix.find()) {
                prefix = matcherPrefix.group();
            }

            String pattern_merchant = String
                    .format(PATTERN_MERCHANT_ID, prefix);
            String pattern_sum = String.format(PATTERN_SUM, prefix);
            String pattern_currency = String
                    .format(PATTERN_CURRENCY_ID, prefix);
            String pattern_description = String.format(PATTERN_DESCRIPTION,
                    prefix);
            String pattern_subject = String.format(PATTERN_SUBJECT_ID, prefix);

            String sID_Merchant = execution.getVariable(pattern_merchant) != null ? execution
                    .getVariable(pattern_merchant).toString() : execution
                    .getVariable(String.format(PATTERN_MERCHANT_ID, ""))
                    .toString();
            String sSum = execution.getVariable(pattern_sum) != null ? execution
                    .getVariable(pattern_sum).toString() : execution
                    .getVariable(String.format(PATTERN_SUM, "")).toString();
            if (sSum != null) {
                sSum = sSum.replaceAll(",", ".");
            }
            String sID_Currency = execution.getVariable(pattern_currency) != null ? execution
                    .getVariable(pattern_currency).toString() : execution
                    .getVariable(String.format(PATTERN_CURRENCY_ID, ""))
                    .toString();
            Currency oID_Currency = Currency
                    .valueOf(sID_Currency == null ? "UAH" : sID_Currency);

            Language sLanguage = Liqpay.DEFAULT_LANG;
            String sDescription = execution.getVariable(pattern_description) != null ? execution
                    .getVariable(pattern_description).toString() : execution
                    .getVariable(String.format(PATTERN_DESCRIPTION, ""))
                    .toString();

            String pattern_expired_period_hour = String.format(PATTERN_EXPIRED_PERIOD_HOUR, prefix);
            Integer nExpired_Period_Hour = execution.getVariable(pattern_expired_period_hour) != null
                    ? ((Long) execution.getVariable(pattern_expired_period_hour)).intValue() : null;

            String sID_Order = "TaskActiviti_" + execution.getId().trim()
                    + prefix;
            String sURL_CallbackStatusNew = String.format(LIQPAY_CALLBACK_URL,
                    sID_Order, "", prefix);
            String sURL_CallbackPaySuccess = null;
            Long nID_Subject = Long.valueOf(execution
                    .getVariable(pattern_subject) != null ? execution
                    .getVariable(pattern_subject).toString() : execution
                    .getVariable(String.format(PATTERN_SUBJECT_ID, ""))
                    .toString());
            nID_Subject = (nID_Subject == null ? 0 : nID_Subject);
            boolean bTest = generalConfig.isTest_LiqPay();
            String htmlButton = liqBuy.getPayButtonHTML_LiqPay(sID_Merchant,
                    sSum, oID_Currency, sLanguage, sDescription, sID_Order,
                    sURL_CallbackStatusNew, sURL_CallbackPaySuccess,
                    nID_Subject, bTest, nExpired_Period_Hour);
            matcher.appendReplacement(outputTextBuffer, htmlButton);
        }
        return matcher.appendTail(outputTextBuffer).toString();
    }

    private String replaceTags_sURL_SERVICE_MESSAGE(String textWithoutTags,
            DelegateExecution execution, Long nID_Order) throws Exception {

        StringBuffer outputTextBuffer = new StringBuffer();
        Matcher matcher = TAG_sURL_SERVICE_MESSAGE.matcher(textWithoutTags);
        while (matcher.find()) {
            String tag_sURL_SERVICE_MESSAGE = matcher.group();
            String prefix = "";
            Matcher matcherPrefix = TAG_PATTERN_PREFIX
                    .matcher(tag_sURL_SERVICE_MESSAGE);
            if (matcherPrefix.find()) {
                prefix = matcherPrefix.group();
            }
            String URL_SERVICE_MESSAGE = generalConfig.getSelfHostCentral()
                    + "/wf/service/subject/message/setMessageRate";

            String sURI = ToolWeb.deleteContextFromURL(URL_SERVICE_MESSAGE);

            String sQueryParamPattern = "?"
                    + "&sID_Rate="
                    + prefix.replaceAll("_", "")
                    + "&sID_Order="
                    + generalConfig.getOrderId_ByOrder(nID_Order);

            String sQueryParam = String.format(sQueryParamPattern);
            if (nID_Subject != null) {
                sQueryParam = sQueryParam + "&nID_Subject=" + nID_Subject;
            }
            sQueryParam = sQueryParam
                    // TODO: Need remove in future!!!
                    + "&" + AuthenticationTokenSelector.ACCESS_CONTRACT + "="
                    + AccessContract.RequestAndLoginUnlimited.name();
            LOG.info("(sURI={},{})", sURI, sQueryParam);
            String sAccessKey = accessCover.getAccessKeyCentral(sURI
                    + sQueryParam, AccessContract.RequestAndLoginUnlimited);
            String replacemet = URL_SERVICE_MESSAGE + sQueryParam + "&"
                    + AuthenticationTokenSelector.ACCESS_KEY + "=" + sAccessKey;
            LOG.info("(replacemet URL={}) ", replacemet);
            matcher.appendReplacement(outputTextBuffer, replacemet);
        }
        return matcher.appendTail(outputTextBuffer).toString();
    }

    private String replaceTags_sURL_FEEDBACK_MESSAGE(String textWithoutTags,
            DelegateExecution execution, Long nID_Order) throws Exception {

        StringBuffer outputTextBuffer = new StringBuffer();
        Matcher oMatcher = TAG_sURL_FEEDBACK_MESSAGE.matcher(textWithoutTags);
        while (oMatcher.find()) {
            String tag_sURL_FEEDBACK_MESSAGE = oMatcher.group();
            List<String> aPreviousUserTask_ID = getPreviousTaskId(execution);
            Map<String, FormProperty> mProperty = new HashMap<>();
            loadPropertiesFromTasks(execution, aPreviousUserTask_ID, mProperty);

            String sAuthorMail = "";
            String sAuthorLastName = "";
            String sAuthorFirstName = "";
            String sAuthorMiddleName = "";
            String sAuthorFIO = "";
            String sAuthorFIO_Original = "";

            String sPrefix = "";
            Matcher oMatcherPrefix = TAG_PATTERN_PREFIX.matcher(tag_sURL_FEEDBACK_MESSAGE);
            if (oMatcherPrefix.find()) {
                sPrefix = oMatcherPrefix.group();
            }

            for (Entry<String, FormProperty> oFormPropertyEntry : mProperty.entrySet()) {
                FormProperty oFormProperty = oFormPropertyEntry.getValue();

                if (oFormProperty != null) {
                    String sID = oFormProperty.getId();

                    if ("email".equalsIgnoreCase(sID) && oFormProperty.getValue() != null && !"null".equalsIgnoreCase(oFormProperty.getValue())) {
                        sAuthorMail = oFormProperty.getValue();
                    }
                    if ("bankIdlastName".equalsIgnoreCase(sID) && oFormProperty.getValue() != null && !"null".equalsIgnoreCase(oFormProperty.getValue())) {
                        sAuthorLastName = oFormProperty.getValue();
                    }
                    if ("bankIdfirstName".equalsIgnoreCase(sID) && oFormProperty.getValue() != null && !"null".equalsIgnoreCase(oFormProperty.getValue())) {
                        sAuthorFirstName = oFormProperty.getValue();
                    }
                    if ("bankIdmiddleName".equalsIgnoreCase(sID) && oFormProperty.getValue() != null && !"null".equalsIgnoreCase(oFormProperty.getValue())) {
                        sAuthorMiddleName = oFormProperty.getValue();
                    }
                    if ("clFIO".equalsIgnoreCase(sID) && oFormProperty.getValue() != null && !"null".equalsIgnoreCase(oFormProperty.getValue())) {
                        sAuthorFIO_Original = oFormProperty.getValue();
                    }
                }
            }

            if (sAuthorFIO_Original != null && !"".equals(sAuthorFIO_Original.trim())) {
                String[] as = sAuthorFIO_Original.split("\\ ");
                if (as.length > 0 && (sAuthorLastName == null || "".equals(sAuthorLastName.trim()))) {
                    sAuthorLastName = as[0];
                }
                if (as.length > 1 && (sAuthorFirstName == null || "".equals(sAuthorFirstName.trim()))) {
                    sAuthorFirstName = as[1];
                }
                if (as.length > 2 && (sAuthorMiddleName == null || "".equals(sAuthorMiddleName.trim()))) {
                    sAuthorMiddleName = as[2];
                }
            }
            if (sAuthorFirstName != null && !"".equals(sAuthorFirstName.trim())) {
            }
            if (sAuthorMiddleName != null && !"".equals(sAuthorMiddleName.trim())) {
            }
            sAuthorFIO = sAuthorFirstName + " " + sAuthorMiddleName;

            String sPlace = "";
            String sID_Place_UA = "";
            Long nID_Service = 0L;
            try {
                String jsonHistoryEvent = historyEventService.getHistoryEvent(generalConfig.getOrderId_ByOrder(nID_Order));
                LOG.info("get history event for bp: (jsonHistoryEvent={})", jsonHistoryEvent);
                JSONObject historyEvent = new JSONObject(jsonHistoryEvent);
                nID_Service = historyEvent.getLong("nID_Service");
                sID_Place_UA = historyEvent.getString("sID_UA");
                String snID_Process = execution.getProcessInstanceId();
                sPlace = placeService.getPlaceByProcess(snID_Process);
            } catch (Exception oException) {
                LOG.error("ex!: {}", oException.getMessage());
                LOG.debug("FAIL:", oException);
            }

            String sURL_FEEDBACK_MESSAGE = generalConfig.getSelfHostCentral()
                    + "/wf/service/subject/message/setFeedbackExternal";
            String sURI = ToolWeb.deleteContextFromURL(sURL_FEEDBACK_MESSAGE);
            String sQueryParamPattern = "?"
                    + "&sID_Source=" + "self"
                    + "&sAuthorFIO=" + sAuthorFIO
                    + "&sPlace=" + sPlace
                    + "&sID_Place_UA=" + sID_Place_UA
                    + "&sMail=" + sAuthorMail
                    + "&sBody=" + ""
                    + "&nID_Rate=" + sPrefix.replaceAll("_", "")
                    + "&nID_Service=" + nID_Service
                    + "&bSelf=" + true;

            String sQueryParam = String.format(sQueryParamPattern);
            if (nID_Subject != null) {
                sQueryParam = sQueryParam + "&nID_Subject=" + nID_Subject;
            }
            String sID_Order = generalConfig.getOrderId_ByProcess(Long.valueOf(execution.getProcessInstanceId()));
            if (sID_Order != null) {
                sQueryParam = sQueryParam + "&sID_Order=" + sID_Order;
            }
            sQueryParam = sQueryParam
                    // TODO: Need remove in future!!!
                    + "&" + AuthenticationTokenSelector.ACCESS_CONTRACT + "="
                    + AccessContract.RequestAndLoginUnlimited.name();
            LOG.info("(sURI={},{})", sURI, sQueryParam);
            String sAccessKey = accessCover.getAccessKeyCentral(sURI
                    + sQueryParam, AccessContract.RequestAndLoginUnlimited);
            String sReplacemet = sURL_FEEDBACK_MESSAGE + sQueryParam + "&"
                    + AuthenticationTokenSelector.ACCESS_KEY + "=" + sAccessKey;
            LOG.info("(replacemet URL={}) ", sReplacemet);
            oMatcher.appendReplacement(outputTextBuffer, sReplacemet);
        }
        return oMatcher.appendTail(outputTextBuffer).toString();
    }

    private void loadPropertiesFromTasks(DelegateExecution oDelegateExecution,
            List<String> asID_UserTaskPrevious, Map<String, FormProperty> aFormPropertyReturn) {

        for (String sID_UserTaskPrevious : asID_UserTaskPrevious) {
            try {
                FormData oFormData = null;
                if (asID_UserTaskPrevious != null && !asID_UserTaskPrevious.isEmpty()) {
                    oFormData = oDelegateExecution.getEngineServices()
                            .getFormService().getTaskFormData(sID_UserTaskPrevious);
                }

                if (oFormData != null && oFormData.getFormProperties() != null) {
                    for (FormProperty oFormProperty : oFormData.getFormProperties()) {
                        aFormPropertyReturn.put(oFormProperty.getId(), oFormProperty);
                        LOG.info("Matching1 property (Id={},Name={},Type={},Value={})",
                                oFormProperty.getId(), oFormProperty.getName(),
                                oFormProperty.getType().getName(),
                                oFormProperty.getValue());
                    }
                }
            } catch (Exception e) {
                LOG.error(
                        "Error: {}, occured while looking for a form for task:{}",
                        e.getMessage(), sID_UserTaskPrevious);
                LOG.debug("FAIL:", e);
            }
        }
        try {
            FormData oTaskFormData = oDelegateExecution.getEngineServices()
                    .getFormService()
                    .getStartFormData(oDelegateExecution.getProcessDefinitionId());
            if (oTaskFormData != null
                    && oTaskFormData.getFormProperties() != null) {
                for (FormProperty oFormProperty : oTaskFormData.getFormProperties()) {
                    aFormPropertyReturn.put(oFormProperty.getId(), oFormProperty);
                    LOG.info("Matching2 property (Id={},Name={},Type={},Value={})",
                            oFormProperty.getId(), oFormProperty.getName(),
                            oFormProperty.getType().getName(),
                            oFormProperty.getValue());
                }
            }
        } catch (Exception e) {
            LOG.error(
                    "Error: {}, occured while looking for a start form for a process.",
                    e.getMessage());
            LOG.debug("FAIL:", e);
        }
    }

    private List<String> getPreviousTaskId(DelegateExecution execution) {
        ExecutionEntity ee = (ExecutionEntity) execution;

        List<String> tasksRes = new LinkedList<>();
        List<String> resIDs = new LinkedList<>();

        for (FlowElement flowElement : execution.getEngineServices()
                .getRepositoryService()
                .getBpmnModel(ee.getProcessDefinitionId()).getMainProcess()
                .getFlowElements()) {
            if (flowElement instanceof UserTask) {
                UserTask userTask = (UserTask) flowElement;
                resIDs.add(userTask.getId());

            }
        }

        for (String taskIdInBPMN : resIDs) {
            List<Task> tasks = execution.getEngineServices().getTaskService()
                    .createTaskQuery().executionId(execution.getId())
                    .taskDefinitionKey(taskIdInBPMN).list();
            if (tasks != null) {
                for (Task task : tasks) {
                    LOG.info(
                            "Task with (ID={}, name={}, taskDefinitionKey={})",
                            task.getId(), task.getName(),
                            task.getTaskDefinitionKey());
                    tasksRes.add(task.getId());
                }
            }
        }
        return tasksRes;
    }

    protected Long getLongFromFieldExpression(Expression expression,
            DelegateExecution execution) {
        if (expression != null) {
            Object value = expression.getValue(execution);
            if (value != null) {
                return Long.valueOf(value.toString());
            }
        }
        return null;
    }

    public static String populatePatternWithContent(String inputText)
            throws IOException, URISyntaxException {
        StringBuffer outputTextBuffer = new StringBuffer();
        Matcher matcher = TAG_sPATTERN_CONTENT_COMPILED.matcher(inputText);
        while (matcher.find()) {
            matcher.appendReplacement(outputTextBuffer,
                    getPatternContentReplacement(matcher));
        }
        matcher.appendTail(outputTextBuffer);
        return outputTextBuffer.toString();
    }

    /*
	 * Access modifier changed from private to default to enhance testability
     */
    private static String getPatternContentReplacement(Matcher matcher) throws IOException,
            URISyntaxException {
        String sPath = matcher.group(1);
        byte[] bytes = getFileData_Pattern(sPath);
        String sData = Tool.sData(bytes);
        return sData;
    }

    public Mail mail_BaseFromTask(DelegateExecution oExecution)
            throws Exception {
        System.setProperty("mail.mime.address.strict", "false");
        String saToMail = getStringFromFieldExpression(to, oExecution);
        LOG.info("saToMail {}", saToMail);
        String sHead = getStringFromFieldExpression(subject, oExecution);
        LOG.info("sHead {}", sHead);
        String sBodySource = getStringFromFieldExpression(text, oExecution);
        String sBody = replaceTags(sBodySource, oExecution);
        //LOG.info("sBody {}", sBody);
        Multipart oMultiparts = new MimeMultipart();
        Mail oMail = context.getBean(Mail.class);
        oMail._From(mailAddressNoreplay)._To(saToMail)._Head(sHead)
                ._Body(sBody)._AuthUser(mailServerUsername)
                ._AuthPassword(mailServerPassword)._Host(mailServerHost)
                ._Port(Integer.valueOf(mailServerPort))
                ._SSL(bSSL)._TLS(bTLS)._oMultiparts(oMultiparts);

        return oMail;
    }

    /**
     * Метод для получения json содержащий sKey - sID_StorageType записи в монго
     * текста письма
     *
     * @param oExecution
     * @return
     */
    public String loadFormPropertyFromTaskHTMLText(DelegateExecution oExecution) {
        List<String> previousUserTaskId = getPreviousTaskId(oExecution);
        List<String> aFormPropertyReturnJsonForMongo = new ArrayList<>();
        for (String sID_UserTaskPrevious : previousUserTaskId) {
            try {
                FormData oFormData = null;
                if (previousUserTaskId != null && !previousUserTaskId.isEmpty()) {
                    oFormData = oExecution.getEngineServices()
                            .getFormService().getTaskFormData(sID_UserTaskPrevious);
                    if (oFormData != null && oFormData.getFormProperties() != null) {
                        for (FormProperty oFormProperty : oFormData.getFormProperties()) {
                            if (oFormProperty.getValue() != null && "fileHTML".equals(oFormProperty.getType().getName())) {
                                aFormPropertyReturnJsonForMongo.add(oFormProperty.getValue());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error(
                        "Error: {}, occured while looking for a form for task:{}",
                        e.getMessage(), sID_UserTaskPrevious);
                LOG.debug("FAIL:", e);
            }
        }

        if (!aFormPropertyReturnJsonForMongo.isEmpty()) {
            return aFormPropertyReturnJsonForMongo.get(0);
        }
        return "{\"\":\"\"}";
    }

    public void sendMail(DelegateExecution oExecution, String sAttachmentsForSend)
            throws Exception {
        //если тестовый сервер - письма чиновнику на адрес smailclerkigov@gmail.com
        convertExecutionVariableValue(oExecution);
        Mail oMail = mail_BaseFromTask(oExecution);
        if (sAttachmentsForSend != null && !"".equals(sAttachmentsForSend.trim())) {
            sendAttachOldChema(oMail, oExecution, sAttachmentsForSend);
            sendAttachNewChema(oMail, sAttachmentsForSend);
        }
        if (generalConfig.isSelfTest() && oMail.getBody() != null && oMail.getBody().contains("Шановний колего!")) {
            
            String sHead = oMail.getHead();
            String sBody = oMail.getBody();
                    
            oMail = context.getBean(Mail.class);
            oMail._From(oMail.getFrom())._To(generalConfig.getsAddrClerk())._Head(sHead)
                    ._Body(sBody)._AuthUser(generalConfig.getsUsnameClerk())
                    ._AuthPassword(generalConfig.getsPassClerk())._Host(oMail.getHost())
                    ._Port(oMail.getPort())
                    ._SSL(oMail.isSSL())._TLS(oMail.isTLS())
                    ._oMultiparts(new MimeMultipart());
            //LOG.info("oMail.getFrom() {}", oMail.getFrom());
            //LOG.info(" oMail.getTo() {}", oMail.getTo());
            //LOG.info("oMail.getBody() {}", oMail.getBody());
            //LOG.info("oMail.getHead() {}", oMail.getHead());
            
        }        
        oMail.send();
        saveServiceMessage_Mail(oMail.getHead(), oMail.getBody(), generalConfig.getOrderId_ByProcess(Long.valueOf(oExecution.getProcessInstanceId())), oMail.getTo());
        LOG.info("sendMailOfTask ok!");
    }
    
    private String getFormattedDate(Date date) {
        LOG.info("getFormattedDate -->>>>" + date);
        if (date == null) {
            return StringUtils.EMPTY;
        }

        Calendar oCalendar = Calendar.getInstance();
        oCalendar.setTime(date);
        SimpleDateFormat oSimpleDateFormat = new SimpleDateFormat(DateUtilFormat.DATE_FORMAT_dd_SLASH_MM_SLASH_yyyy);
        return oSimpleDateFormat.format(oCalendar.getTime());
    }

    private String getFormattedDateS(String date) {
        LOG.info("getFormattedDateS -->>>>" + date);
        DateTimeFormatter dateStringFormat = DateTimeFormat
                .forPattern(DateUtilFormat.DATE_FORMAT_dd_SLASH_MM_SLASH_yyyy);
        DateTime dateTime = dateStringFormat.parseDateTime(date);
        Date d = dateTime.toDate();
        return getFormattedDate(d);
    }

    @Override
    public void execute(DelegateExecution oExecution) throws Exception {
    }

    protected void saveServiceMessage_Mail(String sHead, String sBody, String sID_Order, String sMail) {

        if (sBody != null && sBody.contains("Шановний колего!")) {
            //Не сохраняем в истории заявки гражданина письмо чиновнику //Юлия
            return;
        }

        final Map<String, String> mParam = new HashMap<>();
        mParam.put("sHead", "Відправлено листа");//"Відправлено листа"
        mParam.put("sBody", sHead);//sBody
        mParam.put("sID_Order", sID_Order);
        mParam.put("sMail", sMail);

        mParam.put("nID_SubjectMessageType", "" + 10L);
        mParam.put("sID_DataLinkSource", "Region");
        mParam.put("sID_DataLinkAuthor", "System");
        String sID_DataLink;
        sID_DataLink = durableBytesDataStorage.saveData(sBody.getBytes(Charset.forName("UTF-8")));
        mParam.put("sID_DataLink", sID_DataLink);

        mParam.put("RequestMethod", RequestMethod.GET.name());

        ScheduledExecutorService oScheduledExecutorService = Executors
                .newSingleThreadScheduledExecutor();
        Runnable oRunnable = new Runnable() {

            @Override
            public void run() {
                LOG.info(
                        "try to save service message with params with a delay: (params={})",
                        mParam);
                String jsonServiceMessage;
                try {
                    jsonServiceMessage = historyEventService
                            .addServiceMessage(mParam);
                    LOG.info("(jsonServiceMessage={})", jsonServiceMessage);
                } catch (Exception e) {
                    LOG.error("( saveServiceMessage error={})", e.getMessage());
                }
            }
        };
        oScheduledExecutorService.schedule(oRunnable, 10, TimeUnit.SECONDS);
        oScheduledExecutorService.shutdown();

        LOG.info(
                "Configured thread to run in 10 seconds with params: (params={})",
                mParam);
    }

    protected void convertExecutionVariableValue(DelegateExecution oExecution) {
        try {
            Map<String, Object> mExecutionVaraibles = oExecution.getVariables();
            LOG.info("mExecutionVaraibles={} " + mExecutionVaraibles);
            if (!mExecutionVaraibles.isEmpty()) {
                Map<String, Object> mOnlyDateVariables = new HashMap<>();
                // выбираем все переменные типа Date, приводим к нужному формату 
                mExecutionVaraibles.forEach((sKey, oValue) -> {
                    if (oValue != null) {
                        String soValue = oValue.toString();
                        if (isValidDate(soValue, "dd/MM/yyyy")) {
                            DateTime oDateTime = DateTime.parse(soValue,
                                    DateTimeFormat.forPattern("dd/MM/yyyy"));
                            LOG.info("Parsing success. Begin formating");
                            String sDateFromated = oDateTime.toString("dd MMMM yyyy", new Locale("uk", "UA"));
                            mOnlyDateVariables.put(sKey, sDateFromated);
                        }
                    }
                });
                LOG.info("mOnlyDateVariables={} " + mOnlyDateVariables);
                //сетим отформатированные переменные в екзекьюшен
                oExecution.setVariables(mOnlyDateVariables);
                mExecutionVaraibles = oExecution.getVariables();
                LOG.info("mExecutionVaraibles aftet formating={} " + mExecutionVaraibles);
            }
        } catch (Exception oException) {
            LOG.error("Error: date not formated {}", oException.getMessage());
        }
    }
    
    public void sendMail_LinkToDocument(DelegateExecution oExecution, String sAttachmentsForSend) throws Exception 
    {
        convertExecutionVariableValue(oExecution);
        
        if (sAttachmentsForSend != null && !"".equals(sAttachmentsForSend.trim())) 
        {
            System.setProperty("mail.mime.address.strict", "false");
            
            String saToMail = getStringFromFieldExpression(to, oExecution);
            String sHead = getStringFromFieldExpression(subject, oExecution);
            String sBodySource = getStringFromFieldExpression(text, oExecution);
            LOG.info("sHead is {}", sHead);
            LOG.info("sBodySource is {}", sBodySource);
            
            if(sHead == null){
                sHead = "Вам надіслано документа для візування";
            }
            
            Boolean bEmptyBody = false;
            
            if(sBodySource == null){
                sBodySource = "Будь ласка перейдіть по цьому посиланню та підпішіть документ. ";
                bEmptyBody = true;
            }
            
            String sBody = replaceTags(sBodySource, oExecution);
            
            Multipart oMultiparts = new MimeMultipart();
            Mail oMail = context.getBean(Mail.class);
            
            List<DocumentImageFileVO> aDocumentImageFileVO = addProcessLinkToDocument(sAttachmentsForSend);
            for(DocumentImageFileVO oDocumentImageFileVO : aDocumentImageFileVO){
                if(bEmptyBody){
                    sBody = sBody + "</br>" + oDocumentImageFileVO.getsURL();
                }else{
                    sBody = sBody.replaceAll("\\[sDocumentURL\\]", oDocumentImageFileVO.getsURL());
                }
            }
            
            oMail._From(mailAddressNoreplay)._To(saToMail)._Head(sHead)
                ._Body(sBody)._AuthUser(mailServerUsername)
                ._AuthPassword(mailServerPassword)._Host(mailServerHost)
                ._Port(Integer.valueOf(mailServerPort))
                ._SSL(bSSL)._TLS(bTLS)._oMultiparts(oMultiparts);

            
            oMail.send();
        }
    }
       
    protected List<DocumentImageFileVO> addProcessLinkToDocument(String sAttachmentsForSend) {
        LOG.info("addProcessLinkToDocument after parsing: " + sAttachmentsForSend);
        List<DocumentImageFileVO> aDocumentImageFileVO = new ArrayList<>();
        try {
            org.json.simple.JSONObject oJsonTaskAttachVO = null;
            JSONParser parser = new JSONParser();

            Pattern pattern = Pattern.compile("\\{(.*?)\\}");
            Matcher match = pattern.matcher(sAttachmentsForSend);
            
            while (match.find()) {
                String sJsonAttach = match.group(0);
                LOG.info("match.group : " + sJsonAttach);
                try {
                    oJsonTaskAttachVO = (org.json.simple.JSONObject) parser.parse(sJsonAttach);
                    MultipartFile oMultipartFile = null;

                    if (oJsonTaskAttachVO != null && oJsonTaskAttachVO.get("sID_StorageType") != null) {
                        oMultipartFile = oAttachmetService
                                .getAttachment(null, null, (String) oJsonTaskAttachVO.get("sKey"), (String) oJsonTaskAttachVO.get("sID_StorageType"));
                        
                        /*String sFileBody = Base64.getEncoder().encodeToString(oMultipartFile.getBytes());
                        String sMD5_Hash = Base64.getEncoder().encodeToString((org.apache.commons.codec.digest.DigestUtils.
                                md5Hex(oMultipartFile.getInputStream())).getBytes());*/
                        //DocumentImageFileVO oDocumentImageFileVO = oDocumentImageService.setDocumentImageFile("", sFileBody, sMD5_Hash);
                        DocumentImageFileVO oDocumentImageFileVO = oDocumentImageService.setDocumentImageFile("", oMultipartFile);
                        aDocumentImageFileVO.add(oDocumentImageFileVO);
                    } else {
                        LOG.error("aAttachment has nothing!");
                        throw new ActivitiObjectNotFoundException("add the file to send");
                    }

                } catch (Exception ex) {
                    LOG.info("There aren't TaskAttachVO objects in mail - JSON parsing error: ", ex);
                }
            }
        } catch (Exception ex) {
            LOG.info("Error during new file mail processing ", ex);
        }
        return aDocumentImageFileVO;
    }
    
    protected void sendAttachNewChema(Mail oMail, String sAttachmentsForSend) {
        try {
            LOG.info("sAttachmentsForSend after parsing: " + sAttachmentsForSend);
            org.json.simple.JSONObject oJsonTaskAttachVO = null;
            JSONParser parser = new JSONParser();

            Pattern pattern = Pattern.compile("\\{(.*?)\\}");
            Matcher match = pattern.matcher(sAttachmentsForSend);

            while (match.find()) {
                String sJsonAttach = match.group(0);
                LOG.info("match.group : " + sJsonAttach);
                try {
                    oJsonTaskAttachVO = (org.json.simple.JSONObject) parser.parse(sJsonAttach);
                    MultipartFile oMultipartFile = null;

                    if (oJsonTaskAttachVO != null && oJsonTaskAttachVO.get("sID_StorageType") != null) {
                        String sFileName = (String) oJsonTaskAttachVO.get("sFileNameAndExt");

                        oMultipartFile = oAttachmetService
                                .getAttachment(null, null, (String) oJsonTaskAttachVO.get("sKey"), (String) oJsonTaskAttachVO.get("sID_StorageType"));

                        InputStream oInputStream_Attachment = oMultipartFile.getInputStream();

                        DataSource oDataSource = new ByteArrayDataSource(oInputStream_Attachment, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                        oMail._Attach(oDataSource, sFileName, "(no description)");
                        LOG.info("oMultiPartEmail.attach was sending by new schema!");

                    } else {
                        LOG.error("aAttachment has nothing!");
                        throw new ActivitiObjectNotFoundException("add the file to send");
                    }

                } catch (Exception ex) {
                    LOG.info("There aren't TaskAttachVO objects in mail - JSON parsing error: ", ex);
                }

            }
        } catch (Exception ex) {
            LOG.info("Error during new file mail processing ", ex);
        }
    }

    protected void sendAttachOldChema(Mail oMail, DelegateExecution oExecution, String sAttachmentsForSend) {
        try {

            String sOldAttachmentsForSend = sAttachmentsForSend.replaceAll("\\{(.*?)\\}\\,", "").replaceAll("\\{(.*?)\\}", "")
                    .replaceAll("^\"|\"$", "").trim();
            LOG.info("sOldAttachmentsForSend: " + sOldAttachmentsForSend.trim());

            if (!sOldAttachmentsForSend.trim().equals("")) {
                List<Attachment> aAttachment = findAddedAttachments(sOldAttachmentsForSend.trim(), oExecution.getId());
                if (!aAttachment.isEmpty()) {
                    InputStream oInputStream_Attachment = null;
                    String sFileName;
                    String sFileExt;
                    String sDescription;
                    for (Attachment oAttachment : aAttachment) {
                        sFileName = oAttachment.getName();
                        String sExt = "";
                        int nAt = sFileName.lastIndexOf(".");
                        if (nAt >= 0) {
                            sExt = sFileName.substring(nAt);
                        }

                        if (sFileName != null && !sFileName.toLowerCase().endsWith(".xml") && !sFileName.toLowerCase().endsWith(".rpl")) {
                            sFileName = "Attach_" + oAttachment.getId() + sExt; //
                        }

                        sFileExt = oAttachment.getType().split(";")[0];
                        sDescription = oAttachment.getDescription();
                        if (sDescription == null || "".equals(sDescription.trim())) {
                            sDescription = "(no description)";
                        }
                        LOG.info("Old attach whith name: " + sFileName + " and with id: " + oAttachment.getId());

                        LOG.info("(oAttachment.getId()={}, sFileName={}, sFileExt={}, sDescription={})",
                                oAttachment.getId(), sFileName, sFileExt, sDescription);
                        oInputStream_Attachment = oExecution.getEngineServices().getTaskService()
                                .getAttachmentContent(oAttachment.getId());
                        if (oInputStream_Attachment == null) {
                            LOG.error("Attachment with (id={}) doesn't have content associated with it.", oAttachment.getId());
                            throw new ActivitiObjectNotFoundException(
                                    "Attachment with id '" + oAttachment.getId() + "' doesn't have content associated with it.",
                                    Attachment.class);
                        }
                        DataSource oDataSource = new ByteArrayDataSource(oInputStream_Attachment, sFileExt);
                        oMail._Attach(oDataSource, sFileName, sDescription);
                        LOG.info("oMultiPartEmail.attach: Ok!");
                    }
                }
            }
        } catch (Exception ex) {
            LOG.info("Error during old file mail processing ", ex);
        }
    }

    protected void sendSms(DelegateExecution oExecution, Expression sPhone_SMS, Expression sText_SMS) throws Exception {
        
        if(generalConfig.isSelfTest()){
            return;
        }
        
        try {
            //System.setProperty("mail.mime.address.strict", "false");
            String sPhone_SMS_Value = getStringFromFieldExpression(sPhone_SMS, oExecution);
            if (sPhone_SMS_Value != null) {
                String sText_SMS_Value = getStringFromFieldExpression(sText_SMS, oExecution);
                if (sText_SMS_Value != null) {
                    sText_SMS_Value = replaceTags(sText_SMS_Value, oExecution);
                    String sReturn;
                    sPhone_SMS_Value = sPhone_SMS_Value.replaceAll("\\ ", "");
                    sReturn = ManagerSMS.sendSms(sPhone_SMS_Value, sText_SMS_Value,
                            generalConfig.getOrderId_ByOrder(getProtectedNumber(Long.valueOf(oExecution.getProcessInstanceId()))));
                    LOG.info("(sReturn={})", sReturn);
                }
            }
        } catch (Exception ex) {
            LOG.error("Eror during sms sending in MailTaskWithAttachmentsAndSMS:", ex);
            throw ex;
        }
    }

    //Проверка является ли стринга датой, согласно заданного патерна
    private boolean isValidDate(String sDateIn, String sDatePattern) {
        if (sDateIn == null) {
            return false;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(sDatePattern);

        if (sDateIn.trim().length() != dateFormat.toPattern().length()) {
            return false;
        }
        try {
            dateFormat.parse(sDateIn.trim());
        } catch (ParseException ParseException) {
            return false;
        }
        return true;
    }
}
