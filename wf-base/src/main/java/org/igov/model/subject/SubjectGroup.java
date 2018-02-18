/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.model.subject;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.Transient;

import org.igov.model.core.NamedEntity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 *
 * @author olga
 */
@javax.persistence.Entity
//@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@ApiModel(description="Группа субьекта (человека или организации)")
public class SubjectGroup extends NamedEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final static Gson oGson = new Gson();
    @JsonProperty(value = "sID_Group_Activiti")
    @Column
    @ApiModelProperty(value = "Строка-ИД группы пользователя (Activiti)", required = true)
    private String sID_Group_Activiti;

    @JsonProperty(value = "sChain")
    @Column
    @ApiModelProperty(value = "Строка-Группа цепочки (код компании субъекта-организации)", required = true)
    private String sChain;

    @JsonProperty(value = "aUser")
    @Transient
    @ApiModelProperty(value = "Массив пользователей)")
    private List<SubjectUser> aUser;

    @JsonProperty(value = "aSubjectGroupChilds")
    @Transient
    @ApiModelProperty(value = "Массив групп")
    private List<SubjectGroup> aSubjectGroup;
    
    @JsonProperty(value = "sName_SubjectGroupCompany")    
    @Transient
    @ApiModelProperty(value = "Название группы организации субьекта")
    private String sSubjectGroup_Company;
    
    @JsonProperty(value = "oSubject")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_Subject")
    @ApiModelProperty(value = "Номер-ИД субъекта, абстрактной корневой сущности (на что 'вяжется' запись)", required = true)
    private Subject oSubject;
    
    @JsonProperty(value = "oSubjectHumanPositionCustom")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_SubjectHumanPositionCustom")
    @ApiModelProperty(value = "Должность субъекта", required = true)
    private SubjectHumanPositionCustom oSubjectHumanPositionCustom;
    
    public String getsSubjectGroup_Company() {
        return sSubjectGroup_Company;
    }
    
    public void setsSubjectGroup_Company(String sSubjectGroup_Company) {
        this.sSubjectGroup_Company = sSubjectGroup_Company;
    }       

    public List<SubjectUser> getaUser() {
        return aUser;
    }   

    public void setaUser(List<SubjectUser> aUser) {
        this.aUser = aUser;
    }

    public List<SubjectGroup> getaSubjectGroup() {
        return aSubjectGroup;
    }

    public void setaSubjectGroup(List<SubjectGroup> aSubjectGroup) {
        this.aSubjectGroup = aSubjectGroup;
    }

    public String getsID_Group_Activiti() {
        return sID_Group_Activiti;
    }

    public void setsID_Group_Activiti(String sID_Group_Activiti) {
        this.sID_Group_Activiti = sID_Group_Activiti;
    }

    public String getsChain() {
        return sChain;
    }

    public void setsChain(String sChain) {
        this.sChain = sChain;
    }

    public SubjectHumanPositionCustom getoSubjectHumanPositionCustom() {
        return oSubjectHumanPositionCustom;
    }

    public void setoSubjectHumanPositionCustom(SubjectHumanPositionCustom oSubjectHumanPositionCustom) {
        this.oSubjectHumanPositionCustom = oSubjectHumanPositionCustom;
    }

    public Subject getoSubject() {
		return oSubject;
	}

	public void setoSubject(Subject oSubject) {
		this.oSubject = oSubject;
	}

	@Override
    public String toString() {
        return "SubjectGroup [sID_Group_Activiti=" + sID_Group_Activiti + ", sChain=" + sChain + ", getName()="
                + getName() + ", nID=" + getId() + ", nID_SubjectHumanPositionCustom=" + oSubjectHumanPositionCustom.getId() + "]";
    }
    
    public String toJSONString() {
	return oGson.toJson(this);
    }
}
