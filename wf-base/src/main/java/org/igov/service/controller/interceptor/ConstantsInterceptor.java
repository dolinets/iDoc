package org.igov.service.controller.interceptor;

import java.util.regex.Pattern;


/**
 * интерфейс для объявления статических переменных, используемых в интерсепторе
 *
 * @author inna
 */
public interface ConstantsInterceptor {
    String FORM_FORM_DATA = "/form/form-data";
    String START_PROCESS = "/startProcess";
    String DOCUMENT_SERVICE = "/action/task/setDocument";
    String RUNTIME_TASKS = "/runtime/tasks";
    String UPDATE_PROCESS = "task/updateProcess";
    String POST = "POST";
    String PUT = "PUT";
    String GET = "GET";
    String DELETE = "DELETE";
    String SET_CHAT_MESSAGE = "/process/setProcessChatMessage";
    String EDIT_CHAT_MESSAGE = "/process/updateProcessChatMessage";
    String ADD_ACCEPTOR = "/document/addAcceptor";
    String ADD_VIEWER = "/document/addViewer";
    String ADD_VISOR = "/document/addVisor";
    String DELEGATE = "/document/delegateDocumentStepSubject";
    String CANCEL_SIGN = "/document/cancelDocumentSubmit";
    String SYSTEM_ERR = "SYSTEM_ERR";
    String SERVICE_DOCUMENT_GET_DOCUMENT_ABSTRACT = "/service/document/getDocumentAbstract";
    String SERVICE_OBJECT_FILE = "/service/object/file/";
    String SERVICE_DOCUMENT_SET_DOCUMENT_FILE = "/service/document/setDocumentFile";
    String SERVICE_DOCUMENT_GET_DOCUMENTS = "/service/document/getDocuments";
    String SERVICE_DOCUMENT_GET_DOCUMENT_FILE = "/service/document/getDocumentFile";
    String SERVICE_DOCUMENT_GET_DOCUMENT_CONTENT = "/service/document/getDocumentContent";
    String SERVICE_ACTION_EVENT_GET_HISTORY_EVENTS = "/service/action/event/getHistoryEvents";
    String SERVICE_ACTION_EVENT_GET_HISTORY_EVENTS_SERVICE = "/service/action/event/getHistoryEventsService";
    String SERVICE_ACTION_EVENT_GET_LAST_TASK_HISTORY = "/service/action/event/getLastTaskHistory";
    String SERVICE_OBJECT_PLACE_GET_PLACES_TREE = "/service/object/place/getPlacesTree";
    String SERVICE_SUBJECT_MESSAGE_GET_SERVICE_MESSAGES = "/service/subject/message/getServiceMessages";
    String SERVICE_SUBJECT_MESSAGE_GET_MESSAGES = "/service/subject/message/getMessages";
    String SERVICE_ACTION_TASK_GET_LOGIN_B_PS = "/service/action/task/getLoginBPs";
    String SERVICE_HISTORY_HISTORIC_TASK_INSTANCES = "/service/history/historic-task-instances";
    String SERVICE_RUNTIME_TASKS = "/service/runtime/tasks";
    String SERVICE_ACTION_FLOW_GET_FLOW_SLOTS_SERVICE_DATA = "/service/action/flow/getFlowSlots";
    String SERVICE_ACTION_TASK_GET_ORDER_MESSAGES_LOCAL = "/service/action/task/getOrderMessages_Local";
    String SERVICE_ACTION_TASK_GET_START_FORM_DATA = "/service/action/task/getStartFormData";
    String SERVICE_REPOSITORY_PROCESS_DEFINITIONS = "/service/repository/process-definitions";
    String SERVICE_FORM_FORM_DATA = "/service/form/form-data";
    String SERVICE_CANCELTASK = "/service/action/task/cancelTask";
    String SERVICE_ACTION_ITEM_GET_SERVICES_TREE = "/service/action/item/getServicesTree";
    String SERVICE_ACTION_ITEM_GET_SERVICE = "/service/action/item/getService";
    String URI_SYNC_CONTACTS = "/wf/service/subject/syncContacts";
    String DNEPR_MVK_291_COMMON_BP = "dnepr_mvk_291_common|_test_UKR_DOC|dnepr_mvk_889|_doc_justice_171|_doc_justice_172|_doc_justice_173|_doc_justice_11|_doc_justice_12|_doc_justice_13|_doc_justice_14|_doc_justice_15|_doc_justice_16";
    String SERVICE_SUBJECT_PROCESS_SET_PROCESS_SUBJECT_STATUS = "/service/subject/process/setProcessSubjectStatus";
    String URI_DASHBOARD_ENTER = "/wf/service/access/login";
    String URI_URGENT_DOCUMENT = "/wf/service/common/document/setDocumentUrgent";
    Pattern TAG_PATTERN_PREFIX = Pattern.compile("runtime/tasks/[0-9]+$");
    Pattern SREQUESTBODY_PATTERN = Pattern.compile("\"assignee\":\"[а-яА-Яa-z_A-z0-9]+\"");
    String SERVICE_DOCUMENT_REMOVE_DOCUMENT_STEP_SUBJECT = "document/removeDocumentStepSubject";
}
