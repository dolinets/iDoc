package org.igov.model.subject;

import javax.persistence.Column;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.igov.model.core.AbstractEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@javax.persistence.Entity
@ApiModel(description="Аккаунты субъектов")
public class SubjectAccount extends AbstractEntity {

    @Column
    @ApiModelProperty(value = "Логин", required = true)
    private String sLogin;

    @Column
    @ApiModelProperty(value = "Описание", required = true)
    private String sNote;

    @JsonProperty(value = "subjectAccountType")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_SubjectAccountType", insertable = false, updatable = false)
    @ApiModelProperty(value = "Тип акаунта", required = true)
    private SubjectAccountType subjectAccountType;

    @JsonIgnore
    @Column(name = "nID_SubjectAccountType", nullable = true)
    @ApiModelProperty(value = "Номер-ИД типа акаунта", required = true)
    private Long nID_SubjectAccountType;
    
    @Column
    @ApiModelProperty(value = "Номер-ИД сервера, для которого действует этот акаунт", required = true)
    private Long nID_Server;

    @Column
    @ApiModelProperty(value = "Номер-ИД субьекта, с которым связывается акаунт", required = true)
    private Long nID_Subject;

    public String getsLogin() {
	return sLogin;
    }

    public void setsLogin(String sLogin) {
	this.sLogin = sLogin;
    }

    public String getsNote() {
	return sNote;
    }

    public void setsNote(String sNote) {
	this.sNote = sNote;
    }

    public Long getnID_Server() {
	return nID_Server;
    }

    public void setnID_Server(Long nID_Server) {
	this.nID_Server = nID_Server;
    }

    public SubjectAccountType getSubjectAccountType() {
	return subjectAccountType;
    }

    public void setnID_SubjectAccountType(Long nID_SubjectAccountType) {
        this.nID_SubjectAccountType = nID_SubjectAccountType;
    }

    public Long getnID_Subject() {
	return nID_Subject;
    }

    public void setnID_Subject(Long nID_Subject) {
	this.nID_Subject = nID_Subject;
    }
}
