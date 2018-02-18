package org.igov.model.subject.message;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import org.igov.model.core.AbstractEntity;
import org.igov.model.subject.Subject;
import org.igov.model.subject.SubjectContact;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@javax.persistence.Entity
@ApiModel(description="Справочник сообщений")
public class SubjectMessage extends AbstractEntity {

    private static final long serialVersionUID = -5269544412868933212L;

    @JsonProperty(value = "sHead")
    @Column(name = "sHead", length = 200, nullable = false)
    @ApiModelProperty(value = "Заголовок сообщения", required = false)
    private String head;

    @JsonProperty(value = "sBody")
    @Column(name = "sBody", nullable = false)
    @ApiModelProperty(value = "Текст сообщения", required = false)
    private String body;

    @JsonProperty(value = "sDate")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column(name = "sDate", nullable = false)
    @ApiModelProperty(value = "Дата и время отправки сообщения", required = false)
    private DateTime date;

    
    @JsonProperty(value = "nID_Subject")
    @Column(name = "nID_Subject", nullable = false, columnDefinition = "int default 0")
    @ApiModelProperty(value = "Уникальный номер-ИД субъекта", required = false)
    private Long id_subject;
    
    //@Transient
    @JsonProperty(value = "sMail")
    @Column(name = "sMail", length = 100)
    @ApiModelProperty(value = "Адрес электронной почты(test@igov.org.ua по умолчанию)", required = false)
    private String mail;
    
    @JsonProperty(value="oMail")
    @ManyToOne
    @JoinColumn(name="nID_SubjectContact_Mail")
    @Cascade({CascadeType.SAVE_UPDATE})
    private SubjectContact oMail;

    @JsonProperty(value = "sContacts")
    @Column(name = "sContacts", length = 200)
    @ApiModelProperty(value = "Строка контактов", required = false)
    private String contacts;

    @JsonProperty(value = "sData")
    @Column(name = "sData")
    @ApiModelProperty(value = "Строка дополнительных данных", required = false)
    private String data;

    @JsonProperty(value = "oSubjectMessageType")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_SubjectMessageType", nullable = false)
    @ApiModelProperty(value = "Уникальный номер-ИД типа сообщения из справочника SubjectMessageType", required = false)
    private SubjectMessageType subjectMessageType = SubjectMessageType.DEFAULT;
    
    @JsonProperty(value = "sBody_Indirectly")
    @Column(name = "sBody_Indirectly")
    @ApiModelProperty(value = "Отзыв об услуге", required = false)
    private String sBody_Indirectly; 
    
    @JsonProperty(value = "nID_HistoryEvent_Service")
    @Column(name = "nID_HistoryEvent_Service", nullable = true)
    @ApiModelProperty(value = "Уникальный номер-ИД из справочника HistoryEvent_Service", required = false)
    private Long nID_HistoryEvent_Service;

    @JsonProperty(value = "sSubjectInfo")
    @Column(name = "sSubjectInfo")
    @ApiModelProperty(value = "Дополнительная информация о сообщении", required = false)
    private String sSubjectInfo;

    @JsonProperty(value = "oSubject")
    @JoinColumn(name = "nID_Subject", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    @ApiModelProperty(value = "Уникальный номер-ИД субъекта", required = false)
    private Subject oSubject;
    
    @JsonProperty(value = "sID_DataLink")
    @ApiModelProperty(value = "Уникальная строка-ИД(ключ) записи/файла ", required = false)
    private String sID_DataLink;
    
    @JsonProperty(value = "aSubjectMessageQuestionFields")
    @OneToMany(targetEntity = SubjectMessageQuestionField.class, mappedBy = "subjectMessage")
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<SubjectMessageQuestionField> aSubjectMessageQuestionFields;


    public List<SubjectMessageQuestionField> getaSubjectMessageQuestionFields() {
		return aSubjectMessageQuestionFields;
	}

	public void setaSubjectMessageQuestionFields(List<SubjectMessageQuestionField> aSubjectMessageQuestionFields) {
		this.aSubjectMessageQuestionFields = aSubjectMessageQuestionFields;
	}

	public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public Long getId_subject() {
        return id_subject;
    }

    public void setId_subject(Long id_subject) {
        this.id_subject = id_subject;
    }

    public String getMail() {
       return ((this.oMail != null) ? ((this.oMail.getsValue() != null) ? this.oMail.getsValue() : ""): ""); 
    }
    public void setMail(String mail) {
//        SubjectContact.getNewSubjectContact(oMail);
        this.mail = mail;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public SubjectMessageType getSubjectMessageType() {
        return subjectMessageType;
    }

    public void setSubjectMessageType(SubjectMessageType subjectMessageType) {
        this.subjectMessageType = subjectMessageType;
    }

	public String getsBody_Indirectly() {
		return sBody_Indirectly;
	}

	public void setsBody_Indirectly(String sBody_Indirectly) {
		this.sBody_Indirectly = sBody_Indirectly;
	}
    
	public Long getnID_HistoryEvent_Service() {
		return nID_HistoryEvent_Service;
	}

	public void setnID_HistoryEvent_Service(Long nID_HistoryEvent_Service) {
		this.nID_HistoryEvent_Service = nID_HistoryEvent_Service;
	}

    public SubjectContact getoMail() {
        return oMail;
    }

    public void setoMail(SubjectContact oMail) {
        this.oMail = oMail;
//        oMail.setsValue(mail);
    }

    public Subject getoSubject() {
        return oSubject;
    }

    public String getsSubjectInfo() {
        return sSubjectInfo;
    }

    public void setsSubjectInfo(String sSubjectInfo) {
        this.sSubjectInfo = sSubjectInfo;
    }

	public String getsID_DataLink() {
		return sID_DataLink;
	}

	public void setsID_DataLink(String sID_DataLink) {
		this.sID_DataLink = sID_DataLink;
	}

    @Override
    public String toString() {
        return "SubjectMessage{" +
                "head='" + head + '\'' +
                ", body='" + body + '\'' +
                ", date=" + date +
                ", id_subject=" + id_subject +
                ", mail='" + mail + '\'' +
                ", oMail=" + oMail +
                ", contacts='" + contacts + '\'' +
                ", data='" + data + '\'' +
                ", subjectMessageType=" + subjectMessageType +
                ", sBody_Indirectly='" + sBody_Indirectly + '\'' +
                ", nID_HistoryEvent_Service=" + nID_HistoryEvent_Service +
                ", sSubjectInfo='" + sSubjectInfo + '\'' +
                ", oSubject=" + oSubject +
                ", sID_DataLink='" + sID_DataLink + '\'' +
                "} " + super.toString();
    }
}
