package org.igov.model.action.vo;

import org.igov.model.document.DocumentStepType;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author idenysenko
 */
public class TaskFilterVO implements Serializable {

    private String sLogin;
    private String sLoginAuthor;
    private String sProcessDefinitionKey;
    private String sDateType;
    private String sDateFrom;
    private String sDateTo;
    private String sLoginController;
    private String sLoginExecutor;
    private String sFind;
    private Boolean bIncludeDeleted;
    private String sFilterStatus;
    private DocumentStepType oDocumentStepType;
    private Boolean bSearchExternalTasks;
    private List<FilterFieldVO> aoFilterField;

    public TaskFilterVO() {
    }

    public TaskFilterVO(String sLogin, String sFilterStatus, Boolean bIncludeDeleted, Boolean bSearchExternalTasks) {
        this.sLogin = sLogin;
        this.bIncludeDeleted = bIncludeDeleted;
        this.sFilterStatus = sFilterStatus;
        this.bSearchExternalTasks = bSearchExternalTasks;
    }

    public String getsLogin() {
        return sLogin;
    }

    public void setsLogin(String sLogin) {
        this.sLogin = sLogin;
    }

    public String getsProcessDefinitionKey() {
        return sProcessDefinitionKey;
    }

    public void setsProcessDefinitionKey(String sProcessDefinitionKey) {
        this.sProcessDefinitionKey = sProcessDefinitionKey;
    }

    public String getsDateType() {
        return sDateType;
    }

    public void setsDateType(String sDateType) {
        this.sDateType = sDateType;
    }

    public String getsDateFrom() {
        return sDateFrom;
    }

    public void setsDateFrom(String sDateFrom) {
        this.sDateFrom = sDateFrom;
    }

    public String getsDateTo() {
        return sDateTo;
    }

    public void setsDateTo(String sDateTo) {
        this.sDateTo = sDateTo;
    }

    public String getsLoginController() {
        return sLoginController;
    }

    public void setsLoginController(String sLoginController) {
        this.sLoginController = sLoginController;
    }

    public String getsLoginExecutor() {
        return sLoginExecutor;
    }

    public void setsLoginExecutor(String sLoginExecutor) {
        this.sLoginExecutor = sLoginExecutor;
    }

    public String getsFind() {
        return sFind;
    }

    public void setsFind(String sFind) {
        this.sFind = sFind;
    }

    public Boolean getbIncludeDeleted() {
        return bIncludeDeleted;
    }

    public void setbIncludeDeleted(Boolean bIncludeDeleted) {
        this.bIncludeDeleted = bIncludeDeleted;
    }

    public String getsFilterStatus() {
        return sFilterStatus;
    }

    public void setsFilterStatus(String sFilterStatus) {
        this.sFilterStatus = sFilterStatus;
    }

    public Boolean getbSearchExternalTasks() {
        return bSearchExternalTasks;
    }

    public void setbSearchExternalTasks(Boolean bSearchExternalTasks) {
        this.bSearchExternalTasks = bSearchExternalTasks;
    }

    public List<FilterFieldVO> getAoFilterField() {
        return aoFilterField;
    }

    public void setAoFilterField(List<FilterFieldVO> aoFilterField) {
        this.aoFilterField = aoFilterField;
    }

    public String getsLoginAuthor() {
        return sLoginAuthor;
    }

    public void setsLoginAuthor(String sLoginAuthor) {
        this.sLoginAuthor = sLoginAuthor;
    }

    public DocumentStepType getoDocumentStepType() {
        return oDocumentStepType;
    }

    public void setoDocumentStepType(DocumentStepType oDocumentStepType) {
        this.oDocumentStepType = oDocumentStepType;
    }

    @Override
    public String toString() {
        return "TaskFilterVO{" +
            "sLogin='" + sLogin + '\'' +
            ", sLoginAuthor='" + sLoginAuthor + '\'' +
            ", sProcessDefinitionKey='" + sProcessDefinitionKey + '\'' +
            ", sDateType='" + sDateType + '\'' +
            ", sDateFrom='" + sDateFrom + '\'' +
            ", sDateTo='" + sDateTo + '\'' +
            ", sLoginController='" + sLoginController + '\'' +
            ", sLoginExecutor='" + sLoginExecutor + '\'' +
            ", sFind='" + sFind + '\'' +
            ", bIncludeDeleted=" + bIncludeDeleted +
            ", sFilterStatus='" + sFilterStatus + '\'' +
            ", bSearchExternalTasks=" + bSearchExternalTasks +
            ", aoFilterField=" + aoFilterField +
            '}';
    }

}
