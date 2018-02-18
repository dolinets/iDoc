package org.igov.model.subject.organ;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.igov.model.core.NamedEntity;
import org.igov.model.subject.NewSubjectOrgan;
import org.igov.model.subject.Subject;
import org.igov.model.subject.SubjectContact;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.Gson;
import org.hibernate.annotations.Type;
import org.igov.model.object.place.Country;
import org.igov.model.object.place.Place;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;

@javax.persistence.Entity
@AttributeOverrides({ @AttributeOverride(name = "name",
        column = @Column(name = "sName", nullable = true)) })
//@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@ApiModel(description="Субьект - организация (юрлицо)")
public class SubjectOrgan extends NamedEntity {
    private final static Gson oGson = new Gson();
    @JsonProperty(value = "oSubject")
   // @Lazy(true)
    @OneToOne
    @Cascade({ CascadeType.SAVE_UPDATE })
    @JoinColumn(name = "nID_Subject", nullable = false)
    @ApiModelProperty(value = "Номер-ИД субъекта, абстрактной корневой сущности (на что 'вяжется' запись)", required = false)
    private Subject oSubject;

    @JsonProperty(value = "sOKPO")
    @Column(name = "sOKPO", nullable = false)
    @ApiModelProperty(value = "Код-ОКПО организации, уникальный", required = false)
    private String sOKPO;

    @JsonProperty(value = "sFormPrivacy")
    @Column(name = "sFormPrivacy", nullable = true)
    @ApiModelProperty(value = "Форма собственности", required = false)
    private String sFormPrivacy;

    @JsonProperty(value = "sNameFull")
    @Column(name = "sNameFull", nullable = true)
    @ApiModelProperty(value = "Полное название", required = false)
    private String sNameFull;
    
    @JsonProperty(value = "nSizeCharterCapital")
    @Column(name = "nSizeCharterCapital", nullable = true)
    @ApiModelProperty(value = "Число размера капитала (???)", required = false)
    private Double nSizeCharterCapital;
    
    @JsonProperty(value = "sSeriesRegistrationEDR")
    @Column(name = "sSeriesRegistrationEDR", nullable = true)
    @ApiModelProperty(value = "Серия регистрации в едином государственном реестре", required = false)
    private String sSeriesRegistrationEDR;
    
    @JsonProperty(value = "sNumberRegistrationEDR")
    @Column(name = "sNumberRegistrationEDR", nullable = true)
    @ApiModelProperty(value = "Номер регистрации в едином государственном реестре", required = false)
    private String sNumberRegistrationEDR;
    
    @JsonProperty(value = "sDateRegistrationEDR")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    @ApiModelProperty(value = "Дата регистрации в едином государственном реестре", required = false)
    private DateTime sDateRegistrationEDR;
    
    @JsonProperty(value = "sOKPOForeign")
    @Column(name = "sOKPOForeign", nullable = true)
    @ApiModelProperty(value = "(???)", required = false)
    private String sOKPOForeign;
    
    @JsonProperty(value = "oSubjectOrganType")
    @OneToOne
    @Cascade({CascadeType.SAVE_UPDATE})
    @JoinColumn(name = "nID_SubjectOrganType", nullable = true)
    @ApiModelProperty(value = "Тип субьекта-организации", required = false)
    private SubjectOrganType oSubjectOrganType;
    
    @JsonProperty(value = "oCountry")
    @OneToOne
    @Cascade({CascadeType.SAVE_UPDATE})
    @JoinColumn(name = "nID_Country", nullable = true)
    @ApiModelProperty(value = "Страна", required = false)
    private Country oCountry;
    
    @JsonProperty(value = "oPlace")
    @OneToOne
    @Cascade({CascadeType.SAVE_UPDATE})
    @JoinColumn(name = "nID_Place", nullable = true)
    @ApiModelProperty(value = "Населенный пункт", required = false)
    private Place oPlace;
    
    @JsonProperty(value = "bBank")
    @Column(name = "bBank", nullable = true)
    @ApiModelProperty(value = "(Признак банковского учреждения)", required = false)
    private Boolean bBank;
    
    private transient List<SubjectContact> aContact;

    public static NewSubjectOrgan getNewSubjectOrgan(SubjectOrgan subjectOrgan){
        NewSubjectOrgan newSubjectOrgan = new NewSubjectOrgan();
        newSubjectOrgan.setId(subjectOrgan.getId());
        newSubjectOrgan.setName(subjectOrgan.getName());
        if(subjectOrgan.getaContact()!=null){
        newSubjectOrgan.setaContact(SubjectContact.getNewSubjectContact(subjectOrgan.getaContact()));}
        return newSubjectOrgan;
    }

    public Subject getoSubject() {
        return oSubject;
    }

    public void setoSubject(Subject oSubject) {
        this.oSubject = oSubject;
    }

    public String getsOKPO() {
        return sOKPO;
    }

    public void setsOKPO(String sOKPO) {
        this.sOKPO = sOKPO;
    }

    public String getsFormPrivacy() {
        return sFormPrivacy;
    }

    public void setsFormPrivacy(String sFormPrivacy) {
        this.sFormPrivacy = sFormPrivacy;
    }

    public String getsNameFull() {
        return sNameFull;
    }

    public void setsNameFull(String sNameFull) {
        this.sNameFull = sNameFull;
    }

    public List<SubjectContact> getaContact() {
        return aContact;
    }

    public void setaContact(List<SubjectContact> aContact) {
        this.aContact = aContact;
    }

    public Double getnSizeCharterCapital() {
        return nSizeCharterCapital;
    }

    public void setnSizeCharterCapital(Double nSizeCharterCapital) {
        this.nSizeCharterCapital = nSizeCharterCapital;
    }

    public String getsSeriesRegistrationEDR() {
        return sSeriesRegistrationEDR;
    }

    public void setsSeriesRegistrationEDR(String sSeriesRegistrationEDR) {
        this.sSeriesRegistrationEDR = sSeriesRegistrationEDR;
    }

    public String getsNumberRegistrationEDR() {
        return sNumberRegistrationEDR;
    }

    public void setsNumberRegistrationEDR(String sNumberRegistrationEDR) {
        this.sNumberRegistrationEDR = sNumberRegistrationEDR;
    }

    public DateTime getsDateRegistrationEDR() {
        return sDateRegistrationEDR;
    }

    public void setsDateRegistrationEDR(DateTime sDateRegistrationEDR) {
        this.sDateRegistrationEDR = sDateRegistrationEDR;
    }

    public String getsOKPOForeign() {
        return sOKPOForeign;
    }

    public void setsOKPOForeign(String sOKPOForeign) {
        this.sOKPOForeign = sOKPOForeign;
    }

    public SubjectOrganType getoSubjectOrganType() {
        return oSubjectOrganType;
    }

    public void setoSubjectOrganType(SubjectOrganType oSubjectOrganType) {
        this.oSubjectOrganType = oSubjectOrganType;
    }

    public Country getoCountry() {
        return oCountry;
    }

    public void setoCountry(Country oCountry) {
        this.oCountry = oCountry;
    }

    public Place getoPlace() {
        return oPlace;
    }

    public void setoPlace(Place oPlace) {
        this.oPlace = oPlace;
    }

    public Boolean getbBank() {
        return bBank;
    }

    public void setbBank(Boolean bBank) {
        this.bBank = bBank;
    }
    
    
    
    public String toJSONString() {
	return oGson.toJson(this);
    }
    

}
