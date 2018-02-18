package org.igov.model.process.processLink;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.Type;

import org.igov.model.core.AbstractEntity;
import org.igov.model.document.DocumentStepType;
import org.igov.model.server.Server;

import org.igov.util.JSON.JsonDateDeserializer;
import org.igov.util.JSON.JsonDateSerializer;

import org.joda.time.DateTime;

/**
 *
 * @author idenysenko
 */
@javax.persistence.Entity
public class ProcessLink extends AbstractEntity {
    
    @JsonProperty(value = "oServer")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_Server")
    private Server oServer;
    
    @JsonProperty(value = "oProcessLinkType")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_ProcessLink_Type")
    private ProcessLink_Type oProcessLinkType; 
    
    @JsonProperty(value = "sSubType")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_ProcessLink_SubType")
    private ProcessLink_SubType oProcessLinkSubType;
    
    @JsonProperty(value = "sID_Group_Activiti ")
    @Column
    private String sID_Group_Activiti ;
    
    @JsonProperty(value = "sLogin")
    @Column
    private String sLogin;
    
    @JsonProperty(value = "sTaskName")
    @Column
    private String sTaskName;
    
    @JsonProperty(value = "sProcessName")
    @Column
    private String sProcessName;
  
    @JsonProperty(value = "bUrgent")
    @Column
    private Boolean bUrgent;
    
    @JsonProperty(value = "sProcessDateCreate")
    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sProcessDateCreate;

    @JsonProperty(value = "sProcessDateModify")
    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sProcessDateModify;

    @JsonProperty(value = "sTaskDateCreate")
    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sTaskDateCreate;

    @JsonProperty(value = "sTaskDateModify")
    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sTaskDateModify;
    
    @JsonProperty(value = "snID_Process_Activiti")
    @Column
    private String snID_Process_Activiti;
    
    @JsonProperty(value = "snID_Task")
    @Column
    private String snID_Task;
    
    @JsonProperty(value = "oDocumentStepType")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_DocumentStepType")
    private DocumentStepType oDocumentStepType;
  
    @JsonProperty(value = "sStatus")
    @Column
    private String sStatus;

    public Server getoServer() {
        return oServer;
    }

    public void setoServer(Server oServer) {
        this.oServer = oServer;
    }

    public DocumentStepType getoDocumentStepType() {
        return oDocumentStepType;
    }

    public void setoDocumentStepType(DocumentStepType oDocumentStepType) {
        this.oDocumentStepType = oDocumentStepType;
    }

    public ProcessLink_Type getoProcessLinkType() {
        return oProcessLinkType;
    }

    public void setoProcessLinkType(ProcessLink_Type oProcessLinkType) {
        this.oProcessLinkType = oProcessLinkType;
    }

    public ProcessLink_SubType getoProcessLinkSubType() {
        return oProcessLinkSubType;
    }

    public void setoProcessLinkSubType(ProcessLink_SubType oProcessLinkSubType) {
        this.oProcessLinkSubType = oProcessLinkSubType;
    }

    public String getsID_Group_Activiti() {
        return sID_Group_Activiti;
    }

    public void setsID_Group_Activiti(String sID_Group_Activiti) {
        this.sID_Group_Activiti = sID_Group_Activiti;
    }

    public String getsLogin() {
        return sLogin;
    }

    public void setsLogin(String sLogin) {
        this.sLogin = sLogin;
    }

    public String getsTaskName() {
        return sTaskName;
    }

    public void setsTaskName(String sTaskName) {
        this.sTaskName = sTaskName;
    }

    public String getsProcessName() {
        return sProcessName;
    }

    public void setsProcessName(String sProcessName) {
        this.sProcessName = sProcessName;
    }

    public DateTime getsProcessDateCreate() {
        return sProcessDateCreate;
    }

    public void setsProcessDateCreate(DateTime sProcessDateCreate) {
        this.sProcessDateCreate = sProcessDateCreate;
    }

    public DateTime getsProcessDateModify() {
        return sProcessDateModify;
    }

    public void setsProcessDateModify(DateTime sProcessDateModify) {
        this.sProcessDateModify = sProcessDateModify;
    }

    public DateTime getsTaskDateCreate() {
        return sTaskDateCreate;
    }

    public void setsTaskDateCreate(DateTime sTaskDateCreate) {
        this.sTaskDateCreate = sTaskDateCreate;
    }

    public DateTime getsTaskDateModify() {
        return sTaskDateModify;
    }

    public void setsTaskDateModify(DateTime sTaskDateModify) {
        this.sTaskDateModify = sTaskDateModify;
    }

    public String getSnID_Process_Activiti() {
        return snID_Process_Activiti;
    }

    public void setSnID_Process_Activiti(String snID_Process_Activiti) {
        this.snID_Process_Activiti = snID_Process_Activiti;
    }

    public String getSnID_Task() {
        return snID_Task;
    }

    public void setSnID_Task(String snID_Task) {
        this.snID_Task = snID_Task;
    }

    public String getsStatus() {
        return sStatus;
    }

    public void setsStatus(String sStatus) {
        this.sStatus = sStatus;
    }

    public Boolean getbUrgent() {
        return bUrgent;
    }

    public void setbUrgent(Boolean bUrgent) {
        this.bUrgent = bUrgent;
    }
    
    @Override
    public String toString() {
        return "ProcessLink{" + "id=" + getId()
                + ", oServer=" + oServer
                + ", oProcessLinkType=" + oProcessLinkType
                + ", oProcessLinkSubType=" + oProcessLinkSubType
                + ", sID_Group_Activiti=" + sID_Group_Activiti 
                + ", sLogin=" + sLogin
                + ", sTaskName=" + sTaskName
                + ", sProcessName=" + sProcessName
                + ", sProcessDateCreate=" + sProcessDateCreate
                + ", sProcessDateModify=" + sProcessDateModify
                + ", sTaskDateCreate=" + sTaskDateCreate
                + ", sTaskDateModify=" + sTaskDateModify
                + ", snID_Process_Activiti=" + snID_Process_Activiti
                + ", snID_Task=" + snID_Task
                + ", oDocumentStepType=" + oDocumentStepType
                + ", sStatus=" + sStatus
                + ", bUrgent=" + bUrgent
                + '}';
    }
}
