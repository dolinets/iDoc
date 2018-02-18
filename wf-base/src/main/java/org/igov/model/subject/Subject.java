package org.igov.model.subject;

import java.util.ArrayList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

import org.igov.model.core.AbstractEntity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import org.igov.model.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.List;

@javax.persistence.Entity
@ApiModel(description="Субъект (абстрактная сущность для людей и организаций)")
public class Subject extends AbstractEntity {
    private final static Gson oGson = new Gson();
    
    @JsonProperty(value = "sID")
    @Column(name = "sID", nullable = true)
    @ApiModelProperty(value = "Строка-ИД записи, уникально (префикс+ИНН или паспорт или другой уникальный идентификатор)", required = false)
    private String sID;
   
    
       
    @JsonProperty(value = "sLabel")
    @Column(name = "sLabel", nullable = true)
    @ApiModelProperty(value = "Полное ФИО сотрудника или полное название организации", required = true)
    private String sLabel;

    @JsonProperty(value = "sLabelShort")
    @Column(name = "sLabelShort", nullable = true)
    @ApiModelProperty(value = "Краткое ФИО сотрудника или краткое название организации", required = false)
    private String sLabelShort;
    
    @JsonProperty(value = "oSubjectStatus")
    @ManyToOne(targetEntity = SubjectStatus.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_SubjectStatus", nullable = true)
    @ApiModelProperty(value = "Статус субьекта(???)")
    private SubjectStatus oSubjectStatus;

    @JsonProperty(value = "aSubjectAccountContact")
    @JsonManagedReference
    @ApiModelProperty(value = "Массив контактов субьекта", required = false)
    private transient List<SubjectContact> aSubjectAccountContact = new ArrayList<>();

    /*    @JsonProperty(value = "oSubjectHuman")
    @OneToOne
    @Cascade({CascadeType.SAVE_UPDATE})
    @JoinColumn(name = "nID_SubjectHuman")
    private SubjectHuman oSubjectHuman;
    
    @JsonProperty(value = "oSubjectOrgan")
    @OneToOne
    @Cascade({CascadeType.SAVE_UPDATE})
    @JoinColumn(name = "nID_SubjectOrgan")
    private SubjectOrgan oSubjectOrgan;*/
 /*	public SubjectHuman getoSubjectHuman() {
		return oSubjectHuman;
	}

	public void setoSubjectHuman(SubjectHuman oSubjectHuman) {
		this.oSubjectHuman = oSubjectHuman;
	}

	public SubjectOrgan getoSubjectOrgan() {
		return oSubjectOrgan;
	}

	public void setoSubjectOrgan(SubjectOrgan oSubjectOrgan) {
		this.oSubjectOrgan = oSubjectOrgan;
	}*/
    
    public String getsID() {
        return sID;
    }

    public void setsID(String sID) {
        this.sID = sID;
    }

    public String getsLabel() {
        return sLabel;
    }

    public void setsLabel(String sLabel) {
        this.sLabel = sLabel;
    }

    public String getsLabelShort() {
        return sLabelShort;
    }

    public void setsLabelShort(String sLabelShort) {
        this.sLabelShort = sLabelShort;
    }

    public SubjectStatus getoSubjectStatus() {
        return oSubjectStatus;
    }

    public void setoSubjectStatus(SubjectStatus oSubjectStatus) {
        this.oSubjectStatus = oSubjectStatus;
    }

    public void setaSubjectAccountContact(List<SubjectContact> aSubjectAccountContact) {
        this.aSubjectAccountContact = aSubjectAccountContact;
    }

    public List<SubjectContact> getaSubjectAccountContact() {
        return aSubjectAccountContact;
    }
    
    @Override
    public String toString() {
	return toJSONString();
    }

    public String toJSONString() {
	return oGson.toJson(this);
    }

}
