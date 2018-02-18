package org.igov.model.action.vo;

import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.TaskInfo;
import org.igov.model.document.DocumentStepType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author idenysenko
 */
public class TaskDataVO implements TaskInfo {
    //для инициализации у всех инстансов TaskDataVO поля variables пустым массивом
    private static final List FOR_VARIABLES = new ArrayList<>();
    
    private String id;
    private String sUrl;
    private String owner;
    private String assignee;
    private String sStatus;
    private DelegationState delegationState;
    private String name;
    private String sProcessName;
    private String description;
    private Date createTime;
    private String sCreateTime;
    private Date dueDate;
    private int priority;
    private Boolean suspended;
    private String taskDefinitionKey;
    private String tenantId;
    private String category;
    private String formKey;
    private String parentTaskId;
    private String parentTaskUrl;
    private String executionId;
    private String executionUrl;
    private String processInstanceId;
    private String processInstanceUrl;
    private String processDefinitionId;
    private String processDefinitionUrl;
    private String datePlan; //дата выполнения,актуально только для задач
    private Long nOrder;
    private Long nDayPlan;
    private String sID_Order;
    private Boolean bUrgent;
    private List variables = FOR_VARIABLES;
    private Map<String, Object> flowSlotTicketData;
    private Map<String, Object> globalVariables;
    private Map<String, Object> processVariables;
    private Map<String, Object> taskLocalVariables;
    private DocumentStepType oDocumentStepType = null;
    private Map<String,Object> mAdditionalTaskInfo;
       
    public TaskDataVO() {
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getsUrl() {
        return sUrl;
    }

    public void setsUrl(String sUrl) {
        this.sUrl = sUrl;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public DelegationState getDelegationState() {
        return delegationState;
    }

    public void setDelegationState(DelegationState delegationState) {
        this.delegationState = delegationState;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Boolean getSuspended() {
        return suspended;
    }

    public void setSuspended(Boolean suspended) {
        this.suspended = suspended;
    }

    @Override
    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String getFormKey() {
        return formKey;
    }

    public void setFormKey(String formKey) {
        this.formKey = formKey;
    }

    @Override
    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public String getParentTaskUrl() {
        return parentTaskUrl;
    }

    public void setParentTaskUrl(String parentTaskUrl) {
        this.parentTaskUrl = parentTaskUrl;
    }

    @Override
    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getExecutionUrl() {
        return executionUrl;
    }

    public void setExecutionUrl(String executionUrl) {
        this.executionUrl = executionUrl;
    }

    @Override
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessInstanceUrl() {
        return processInstanceUrl;
    }

    public void setProcessInstanceUrl(String processInstanceUrl) {
        this.processInstanceUrl = processInstanceUrl;
    }

    @Override
    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public String getProcessDefinitionUrl() {
        return processDefinitionUrl;
    }

    public void setProcessDefinitionUrl(String processDefinitionUrl) {
        this.processDefinitionUrl = processDefinitionUrl;
    }

    public List getVariables() {
        return variables;
    }

    public Long getnOrder() {
        return nOrder;
    }

    public void setnOrder(Long nOrder) {
        this.nOrder = nOrder;
    }
    
    public void setVariables(List variables) {
        this.variables = variables;
    }
    
    public Map<String, Object> getFlowSlotTicketData() {
        return flowSlotTicketData;
    }

    public void setFlowSlotTicketData(Map<String, Object> flowSlotTicketData) {
        this.flowSlotTicketData = flowSlotTicketData;
    }

    public Map<String, Object> getGlobalVariables() {
        return globalVariables;
    }

    public void setGlobalVariables(Map<String, Object> globalVariables) {
        this.globalVariables = globalVariables;
    }

    public String getDatePlan() {
        return datePlan;
    }

    public void setDatePlan(String datePlan) {
        this.datePlan = datePlan;
    }

    public DocumentStepType getoDocumentStepType() {
        return oDocumentStepType;
    }

    public void setoDocumentStepType(DocumentStepType oDocumentStepType) {
        this.oDocumentStepType = oDocumentStepType;
    }

    public String getsID_Order() {
        return sID_Order;
    }

    public void setsID_Order(String sID_Order) {
        this.sID_Order = sID_Order;
    }

    @Override
    public Map<String, Object> getTaskLocalVariables() {
        return taskLocalVariables;
    }

    public void setTaskLocalVariables(Map<String, Object> taskLocalVariables) {
        this.taskLocalVariables = taskLocalVariables;
    }

    @Override
    public Map<String, Object> getProcessVariables() {
        return processVariables;
    }

    public void setProcessVariables(Map<String, Object> processVariables) {
        this.processVariables = processVariables;
    }

    public String getsCreateTime() {
        return sCreateTime;
    }

    public void setsCreateTime(String sCreateTime) {
        this.sCreateTime = sCreateTime;
    }

    public String getsProcessName() {
        return sProcessName;
    }

    public void setsProcessName(String sProcessName) {
        this.sProcessName = sProcessName;
    }

    public Long getnDayPlan() {
        return nDayPlan;
    }

    public void setnDayPlan(Long nDayPlan) {
        this.nDayPlan = nDayPlan;
    }

    public Boolean getbUrgent() {
        return bUrgent;
    }

    public void setbUrgent(Boolean bUrgent) {
        this.bUrgent = bUrgent;
    }

    public String getsStatus() {
        return sStatus;
    }

    public void setsStatus(String sStatus) {
        this.sStatus = sStatus;
    }

    public Map<String, Object> getmAdditionalTaskInfo() {
        return mAdditionalTaskInfo;
    }

    public void setmAdditionalTaskInfo(Map<String, Object> mAdditionalTaskInfo) {
        this.mAdditionalTaskInfo = mAdditionalTaskInfo;
    }
}
