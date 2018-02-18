package org.igov.model.subject;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.Gson;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.igov.model.core.NamedEntity;
import org.igov.model.server.Server;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Component
@EntityListeners(SubjectHumanListener.class)
@javax.persistence.Entity
@AttributeOverrides({
    @AttributeOverride(name = "name",
            column = @Column(name = "sName", nullable = true))})
//@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@ApiModel(description="Субьект - человек (физлицо)")
public class SubjectHuman extends NamedEntity {

    private final static Gson oGson = new Gson();

    @ManyToMany(targetEntity=SubjectHumanRole.class, mappedBy = "aSubjectHuman")
    @ApiModelProperty(value = "Массив ролей сотрудника(???)", required = true)
    @JsonManagedReference
    private List<SubjectHumanRole> aSubjectHumanRole = new ArrayList<>();

    @JsonProperty(value = "oSubject")
    @OneToOne
    @Cascade({CascadeType.SAVE_UPDATE})
    @JoinColumn(name = "nID_Subject", nullable = false)
    @ApiModelProperty(value = "Номер-ИД субъекта, абстрактной корневой сущности (на что 'вяжется' запись)", required = true)
    private Subject oSubject;

    @JsonProperty(value = "sINN")
    @Column(name = "sINN")
    @ApiModelProperty(value = "Код-ИНН человека, уникальный", required = true)
    private String sINN;

    @JsonProperty(value = "sSB")
    @Column(name = "sSB")
    @ApiModelProperty(value = "(???)", required = false)
    private String sSB;

    @JsonProperty(value = "sPassportSeria")
    @Column(name = "sPassportSeria")
    @ApiModelProperty(value = "Серия паспорта", required = true)
    private String sPassportSeria;

    @JsonProperty(value = "sPassportNumber")
    @Column(name = "sPassportNumber")
    @ApiModelProperty(value = "Номер паспорта", required = true)
    private String sPassportNumber;

    @JsonProperty(value = "sFamily")
    @Column(name = "sFamily")
    @ApiModelProperty(value = "Фамилия", required = true)
    private String sFamily;

    @JsonProperty(value = "sSurname")
    @Column(name = "sSurname")
    @ApiModelProperty(value = "Отчество", required = true)
    private String sSurname;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "nID_SubjectHumanIdType", nullable = false)
    @ApiModelProperty(value = "Тип человека(???)", required = true)
    private SubjectHumanIdType subjectHumanIdType = SubjectHumanIdType.INN;

    @JsonProperty(value = "oDefaultEmail")
    @ManyToOne
    @JoinColumn(name = "nID_SubjectContact_DefaultEmail")
    @Cascade({CascadeType.SAVE_UPDATE})
    @ApiModelProperty(value = "Контакт-почта по умолчанию", required = true)
    private SubjectContact defaultEmail;

    @JsonProperty(value = "oDefaultPhone")
    @ManyToOne
    @JoinColumn(name = "nID_SubjectContact_DefaultPhone")
    @Cascade({CascadeType.SAVE_UPDATE})
    @ApiModelProperty(value = "Контакт-телефон по умолчанию", required = true)
    private SubjectContact defaultPhone;

    @JsonProperty(value = "oSex")
    @Enumerated(EnumType.ORDINAL)
    @ApiModelProperty(value = "Пол", required = true)
    private SubjectHumanSex nID_Sex;
    
    @JsonProperty(value = "oServer")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_Server")
    @ApiModelProperty(value = "Номер-ИД сервера сотрудника (для групп предприятий/холдингов)", required = true)
    private Server oServer;

    @JsonProperty("sDateBirth")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column(name = "sDateBirth")
    @ApiModelProperty(value = "Дата рождения", required = false)
    private DateTime sDateBirth;
    
    @JsonProperty(value = "sTabel")
    @Column(name = "sTabel")
    @ApiModelProperty(value = "Табельный номер", required = false)
    private String sTabel;
    
    private transient List<SubjectContact> aContact;

    public Subject getoSubject() {
        return oSubject;
    }

    public void setoSubject(Subject oSubject) {
        this.oSubject = oSubject;
    }

    public String getsSB() {
        return sSB;
    }

    public void setsSB(String sSB) {
        this.sSB = sSB;
    }

    public String getsINN() {
        return sINN;
    }

    public void setsINN(String sINN) {
        this.sINN = sINN;
    }

    public String getsPassportSeria() {
        return sPassportSeria;
    }

    public void setsPassportSeria(String sPassportSeria) {
        this.sPassportSeria = sPassportSeria;
    }

    public String getsPassportNumber() {
        return sPassportNumber;
    }

    public void setsPassportNumber(String sPassportNumber) {
        this.sPassportNumber = sPassportNumber;
    }

    public String getsFamily() {
        return sFamily;
    }

    public void setsFamily(String sFamily) {
        this.sFamily = sFamily;
    }

    public String getsSurname() {
        return sSurname;
    }

    public void setsSurname(String sSurname) {
        this.sSurname = sSurname;
    }

    public SubjectHumanIdType getSubjectHumanIdType() {
        return subjectHumanIdType;
    }

    public void setSubjectHumanIdType(SubjectHumanIdType subjectHumanIdType) {
        this.subjectHumanIdType = subjectHumanIdType;
    }

    public SubjectContact getDefaultEmail() {
        return defaultEmail;
    }

    public void setDefaultEmail(SubjectContact defaultEmail) {
        this.defaultEmail = defaultEmail;
    }

    public SubjectContact getDefaultPhone() {
        return defaultPhone;
    }

    public void setDefaultPhone(SubjectContact defaultPhone) {
        this.defaultPhone = defaultPhone;
    }

    public List<SubjectContact> getaContact() {
        return aContact;
    }

    public void setaContact(List<SubjectContact> aContact) {
        this.aContact = aContact;
    }

	public SubjectHumanSex getnID_Sex() {
		return nID_Sex;
	}

	public void setnID_Sex(SubjectHumanSex nID_Sex) {
		this.nID_Sex = nID_Sex;
	}

    public List<SubjectHumanRole> getaSubjectHumanRole() {
        return aSubjectHumanRole;
    }

    public void setaSubjectHumanRole(List<SubjectHumanRole> aSubjectHumanRole) {
        this.aSubjectHumanRole = aSubjectHumanRole;
    }

    public Server getoServer() {
        return oServer;
    }

    public void setoServer(Server oServer) {
        this.oServer = oServer;
    }

    public DateTime getsDateBirth() {
        return sDateBirth;
    }

    public void setsDateBirth(DateTime sDateBirth) {
        this.sDateBirth = sDateBirth;
    }
    
    public String getsTabel() {
        return sTabel;
    }

    public void setsTabel(String sTabel) {
        this.sTabel = sTabel;
    }

    public static String getSubjectId(SubjectHumanIdType subjectHumanIdType, String sCode_Subject) {
        String res = sCode_Subject;
        if (subjectHumanIdType != SubjectHumanIdType.INN) {
            res = String.format("_%s;%s", subjectHumanIdType.getId(), sCode_Subject);
        }
        return res;
    }

    public static NewSubjectHuman getNewSubjectHuman(SubjectHuman subjectHuman) {
        NewSubjectHuman newSubjectHuman = new NewSubjectHuman();
        newSubjectHuman.setsSurname(subjectHuman.getsSurname());
        newSubjectHuman.setsFamily(subjectHuman.getsFamily());
        newSubjectHuman.setsINN(subjectHuman.getsINN());
        newSubjectHuman.setsSB(subjectHuman.getsSB());
        newSubjectHuman.setsPassportSeria(subjectHuman.getsPassportSeria());
        newSubjectHuman.setsPassportNumber(subjectHuman.getsPassportNumber());
        newSubjectHuman.setDefaultEmail(SubjectContact
                .getNewSubjectContact(subjectHuman.getDefaultEmail()));
        newSubjectHuman.setDefaultPhone(SubjectContact
                .getNewSubjectContact(subjectHuman.getDefaultPhone()));
        return newSubjectHuman;
    }

    @Override
    public String toString() {
        return "SubjectHuman{"
                + "oSubject=" + oSubject 
                + ", sName=" + getName()
                + ", sSurname=" + sSurname
                + ", oServer=" + oServer
                +", defaultEmail=" + defaultEmail
                +", defaultPhone=" + defaultPhone
                +", sTabel=" + sTabel
                + '}';
    }

    public String toJSONString() {
	return oGson.toJson(this);
    }
    
}
