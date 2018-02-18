package org.igov.model.process.processLink;

import java.io.Serializable;

/**
 *
 * @author idenysenko
 */
public class ProcessLinkVO implements Serializable {
    
    private Long nID_Server;
    private String sType;
    private String sSubType;
    private String sID_Group_Activiti;
    private String sLogin;
    private String sTaskName;
    private String sProcessName;
    private String sProcessDateCreate;
    private String sProcessDateModify;
    private String sTaskDateCreate;
    private String sTaskDateModify;
    private String snID_Process_Activiti;
    private String snID_Task;
    private Long nID_DocumentStepType;
    private Boolean bUrgent;

    public ProcessLinkVO() {
    }

    public Long getnID_Server() {
        return nID_Server;
    }

    public void setnID_Server(Long nID_Server) {
        this.nID_Server = nID_Server;
    }

    public String getsType() {
        return sType;
    }

    public void setsType(String sType) {
        this.sType = sType;
    }

    public String getsSubType() {
        return sSubType;
    }

    public void setsSubType(String sSubType) {
        this.sSubType = sSubType;
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

    public String getsProcessDateCreate() {
        return sProcessDateCreate;
    }

    public void setsProcessDateCreate(String sProcessDateCreate) {
        this.sProcessDateCreate = sProcessDateCreate;
    }

    public String getsProcessDateModify() {
        return sProcessDateModify;
    }

    public void setsProcessDateModify(String sProcessDateModify) {
        this.sProcessDateModify = sProcessDateModify;
    }

    public String getsTaskDateCreate() {
        return sTaskDateCreate;
    }

    public void setsTaskDateCreate(String sTaskDateCreate) {
        this.sTaskDateCreate = sTaskDateCreate;
    }

    public String getsTaskDateModify() {
        return sTaskDateModify;
    }

    public void setsTaskDateModify(String sTaskDateModify) {
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

    public Long getnID_DocumentStepType() {
        return nID_DocumentStepType;
    }

    public void setnID_DocumentStepType(Long nID_DocumentStepType) {
        this.nID_DocumentStepType = nID_DocumentStepType;
    }

    public Boolean getbUrgent() {
        return bUrgent;
    }

    public void setbUrgent(Boolean bUrgent) {
        this.bUrgent = bUrgent;
    }

    @Override
    public String toString() {
        return "ProcessLinkVO{" 
                + "nID_Server=" + nID_Server
                + ", sType=" + sType
                + ", sSubType=" + sSubType 
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
                + ", nID_DocumentStepType=" + nID_DocumentStepType
                + ", bUrgent=" + bUrgent
                + '}';
    }
    
}
