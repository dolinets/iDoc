package org.igov.model.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.springframework.web.multipart.MultipartFile;
import org.igov.model.core.NamedEntity;
import org.igov.util.JSON.JsonDateDeserializer;
import org.igov.util.JSON.JsonDateSerializer;

import javax.persistence.*;
import org.igov.model.subject.Subject;

@javax.persistence.Entity
@ApiModel(description="Документы")
public class Document extends NamedEntity {

    @Transient
    @JsonIgnore
    private MultipartFile fileBody;

    @JsonProperty(value = "oDocumentType")
    @ManyToOne(fetch = FetchType.EAGER)
    //@Cascade({ CascadeType.SAVE_UPDATE })
    @JoinColumn(name = "nID_DocumentType", nullable = false)
    @ApiModelProperty(value = "Уникальный номер-ИД типа документа", required = true)
    private DocumentType documentType;

    @JsonProperty(value = "sID_Content")
    @Column(name = "sID_Content", nullable = false)
    @ApiModelProperty(value = "Уникальная строка-ИД контента", required = true)
    private String contentKey;

    @JsonProperty(value = "oDocumentContentType")
    @ManyToOne(fetch = FetchType.EAGER)
    //@Cascade({ CascadeType.SAVE_UPDATE })
    @JoinColumn(name = "nID_ContentType", nullable = false)
    @ApiModelProperty(value = "Уникальный номер-ИД типа контента документа", required = true)
    private DocumentContentType documentContentType;

    @JsonProperty(value = "sFile")
    @Column(name = "sFile", nullable = true)
    @ApiModelProperty(value = "название файла", required = true)
    private String file;

    @JsonProperty(value = "sDate_Upload")
    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column(name = "sDate_Upload", nullable = true)
    @ApiModelProperty(value = "Дата задгрузки", required = true)
    private DateTime date_Upload;

    @JsonProperty(value = "sContentType")
    @Column(name = "sContentType", nullable = false)
    @ApiModelProperty(value = "Описание типа контента", required = true)
    private String sContentType;

    //Todo: убрать поля, когда клиент отцепится от них
    @JsonProperty(value = "sID_Subject_Upload")
    @Column(name = "sID_Subject_Upload", nullable = false)
    @ApiModelProperty(value = "Уникальная строка-ИД предмета загрузки", required = true)
    private String sID_subject_Upload;

    @JsonProperty(value = "sSubjectName_Upload")
    @Column(name = "sSubjectName_Upload", nullable = false)
    @ApiModelProperty(value = "Название предмета загрузки", required = true)
    private String subjectName_Upload;

    @JsonProperty(value = "oSubject_Upload")
    //@ManyToOne(fetch = FetchType.EAGER)
    @ManyToOne(targetEntity = Subject.class, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "nID_Subject_Upload", nullable = true)
    @ApiModelProperty(value = "Уникальный номер-ИД предмета загрузки", required = true)
    private Subject subject_Upload;

    @JsonProperty(value = "oSubject")
    //@ManyToOne(fetch = FetchType.EAGER)
    @ManyToOne(targetEntity = Subject.class, fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "nID_Subject", nullable = true)
    @ApiModelProperty(value = "Уникальный номер-ИД предмета (сущность Subject)", required = true)
    private Subject subject;

    @JsonProperty(value = "oSignData")
    @Column(name = "oSignData", nullable = false)
    @ApiModelProperty(value = "Объект для подписания", required = true)
    private String oSignData;

    @JsonProperty(value = "oSubject_Author")
    @ManyToOne(targetEntity = Subject.class, fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "nID_Subject_Author", nullable = true)
    @ApiModelProperty(value = "Уникальный номер-ИД автора предмета", required = true)
    private Subject oSubject_Author;

    @JsonProperty(value = "sCustomNumber")
    @Column(name = "sCustomNumber", nullable = true)
    @ApiModelProperty(value = "Пользовательский номер", required = true)
    private String sCustomNumber;

    @JsonProperty(value = "sCustomSeries")
    @Column(name = "sCustomSeries", nullable = true)
    @ApiModelProperty(value = "Пользовательская серия", required = true)
    private String sCustomSeries;
    
    @JsonProperty(value = "sID_File")
    @Column(name = "sID_File", nullable = true)
    private String sID_File;
    
    @JsonProperty(value = "sDateDocument")
    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column(name = "sDateDocument", nullable = true)
    private DateTime sDateDocument;
    
    @JsonProperty(value = "sDateEdit")
    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column(name = "sDateEdit", nullable = true)
    private DateTime sDateEdit;
    
    @JsonProperty(value = "sID_Process_Activiti")
    @Column(name = "sID_Process_Activiti", nullable = false)
    private String sID_Process_Activiti;

    public MultipartFile getFileBody() {
        return fileBody;
    }

    public void setFileBody(MultipartFile fileBody) {
        this.fileBody = fileBody;
    }

    public String getoSignData() {
        return oSignData;
    }

    public void setoSignData(String soSignData) {
        this.oSignData = (soSignData == null || "".equals(soSignData.trim())) ? "{}" : soSignData;
    }

    public String getContentKey() {
        return contentKey;
    }

    public void setContentKey(String contentKey) {
        this.contentKey = contentKey;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public DateTime getDate_Upload() {
        return date_Upload;
    }

    public void setDate_Upload(DateTime date_Upload) {
        this.date_Upload = date_Upload;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public DocumentContentType getDocumentContentType() {
        return documentContentType;
    }

    public void setDocumentContentType(DocumentContentType documentContentType) {
        this.documentContentType = documentContentType;
    }

    public String getSubjectName_Upload() {
        return subjectName_Upload;
    }

    public void setSubjectName_Upload(String subjectName_Upload) {
        this.subjectName_Upload = subjectName_Upload;
    }

    public String getsID_subject_Upload() {
        return sID_subject_Upload;
    }

    public void setsID_subject_Upload(String sID_subject_Upload) {
        this.sID_subject_Upload = sID_subject_Upload;
    }

    public Subject getSubject_Upload() {
        return subject_Upload;
    }

    public void setSubject_Upload(Subject subject_Upload) {
        this.subject_Upload = subject_Upload;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public String getContentType() {
        return sContentType;
    }

    public void setContentType(String s) {
        this.sContentType = s;
    }

    public Subject getoSubject_Author() {
        return oSubject_Author;
    }

    public void setoSubject_Author(Subject oSubject_Author) {
        this.oSubject_Author = oSubject_Author;
    }

    public String getsCustomNumber() {
        return sCustomNumber;
    }

    public void setsCustomNumber(String sCustomNumber) {
        this.sCustomNumber = sCustomNumber;
    }

    public String getsCustomSeries() {
        return sCustomSeries;
    }

    public void setsCustomSeries(String sCustomSeries) {
        this.sCustomSeries = sCustomSeries;
    }

    public String getsID_File() {
        return sID_File;
    }

    public void setsID_File(String sID_File) {
        this.sID_File = sID_File;
    }

    public DateTime getsDateDocument() {
        return sDateDocument;
    }

    public void setsDateDocument(DateTime sDateDocument) {
        this.sDateDocument = sDateDocument;
    }

    public DateTime getsDateEdit() {
        return sDateEdit;
    }

    public void setsDateEdit(DateTime sDateEdit) {
        this.sDateEdit = sDateEdit;
    }

    public String getsID_Process_Activiti() {
        return sID_Process_Activiti;
    }

    public void setsID_Process_Activiti(String sID_Process_Activiti) {
        this.sID_Process_Activiti = sID_Process_Activiti;
    }

    @Override
    public String toString() {
        return "Document{" + "fileBody=" + fileBody + ", documentType=" + documentType + ", contentKey=" + contentKey + ", documentContentType=" + documentContentType + ", file=" + file + ", date_Upload=" + date_Upload + ", sContentType=" + sContentType + ", sID_subject_Upload=" + sID_subject_Upload + ", subjectName_Upload=" + subjectName_Upload + ", subject_Upload=" + subject_Upload + ", subject=" + subject + ", oSignData=" + oSignData + ", oSubject_Author=" + oSubject_Author + ", sCustomNumber=" + sCustomNumber + ", sCustomSeries=" + sCustomSeries + ", sID_File=" + sID_File + ", sDateDocument=" + sDateDocument + ", sDateEdit=" + sDateEdit + ", sID_Process_Activiti=" + sID_Process_Activiti + '}';
    }

}
