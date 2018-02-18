package org.igov.model.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import org.igov.model.core.AbstractEntity;
import org.igov.util.JSON.JsonDateDeserializer;
import org.igov.util.JSON.JsonDateSerializer;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.lang.invoke.MethodHandles;
import java.util.List;

@Entity
@ApiModel(description = "Права субьекта-подписанта шага документа")
public class DocumentStepSubjectRight extends AbstractEntity {

    private static final transient Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER,
            cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "nID_DocumentStep")
    @ApiModelProperty(value = "Шаг документа (на что 'вяжется' запись)", required = true)
    private DocumentStep documentStep;

    @JsonProperty(value = "sKey_GroupPostfix")
    @ApiModelProperty(value = "Строка-ключ группы пользователя", required = true)
    private String sKey_GroupPostfix;

    @ApiModelProperty(value = "Название(роль) подписанта", required = true)
    @JsonProperty(value = "sName")
    private String sName;

    @ApiModelProperty(value = "Флаг права подписи (true-право подписи/false-только подтверждение уведомления/null-только просмотр)", required = false)
    @JsonProperty(value = "bWrite")
    private Boolean bWrite;

    @ApiModelProperty(value = "Флаг требования наложение ЭЦП (true-да/false-нет/null-нет)", required = false)
    @JsonProperty(value = "bNeedECP")
    private Boolean bNeedECP;

    @ApiModelProperty(value = "Логин подписавшего (null-не подписан)", required = false)
    @JsonProperty(value = "sLogin")
    private String sLogin;

    @ApiModelProperty(value = "Строка-ИД поля(???)")
    @JsonProperty(value = "sID_Field")
    private String sID_Field;

    @ApiModelProperty(value = "Строка-ИД поля с типом file для размещения там наложенного ЭЦП")
    @JsonProperty(value = "sID_File_ForSign")
    private String sID_File_ForSign;

    @ApiModelProperty(value = "Флаг экстренности (true-экстренный,false и null - не экстренный)", required = false)
    @JsonProperty(value = "bUrgent")
    private Boolean bUrgent;

    @JsonProperty(value = "oDocumentStepSubjectSignType")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_DocumentStepSubjectSignType")
    @ApiModelProperty(value = "Тип подписи")
    private DocumentStepSubjectSignType oDocumentStepSubjectSignType;

    @ApiModelProperty(value = "Дата подписания (null-не подписано)", required = false)
    @JsonProperty(value = "sDate")
    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @Type(type = DATETIME_TYPE)
    private DateTime sDate;

    @ApiModelProperty(value = "Дата подписания ЭЦП (null-не подписано)", required = false)
    @JsonProperty(value = "sDateECP")
    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @Type(type = DATETIME_TYPE)
    private DateTime sDateECP;

    /* Field-backup - do not delete @OneToMany(mappedBy = "documentStepSubjectRight", cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @ApiModelProperty(value = "Массив полей права субьекта-подписанта в шаге документа)")
    private List<DocumentStepSubjectRightField> documentStepSubjectRightFields;*/

    /*@OneToMany(mappedBy = "oDocumentStepSubjectRight", cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.TRUE)
    @ApiModelProperty(value = "Массив прав субьекта-подписанта в шаге документа)")
    private List<DocumentSubjectRightPermition> aDocumentSubjectRightPermition;*/

    @ApiModelProperty(value = "Доверитель, тот кто может совершать действие над правом (помимо автора дока)", required = false)
    @JsonProperty(value = "sKey_GroupAuthor")
    @Column
    private String sKey_GroupAuthor;

    /*public List<DocumentSubjectRightPermition> getaDocumentSubjectRightPermition() {
        return aDocumentSubjectRightPermition;
    }

    public void setaDocumentSubjectRightPermition(List<DocumentSubjectRightPermition> aDocumentSubjectRightPermition) {
        this.aDocumentSubjectRightPermition = aDocumentSubjectRightPermition;
    }*/

    /* Field-backup - do not delete public List<DocumentStepSubjectRightField> getDocumentStepSubjectRightFields() {
        return documentStepSubjectRightFields;
    }

    public void setDocumentStepSubjectRightFields(List<DocumentStepSubjectRightField> documentStepSubjectRightFields) {
        this.documentStepSubjectRightFields = documentStepSubjectRightFields;
    }*/

    public DocumentStep getDocumentStep() {
        return documentStep;
    }

    public void setDocumentStep(DocumentStep documentStep) {
        this.documentStep = documentStep;
    }

    public String getsKey_GroupPostfix() {
        return sKey_GroupPostfix;
    }

    public void setsKey_GroupPostfix(String sKey_GroupPostfix) {
        this.sKey_GroupPostfix = sKey_GroupPostfix;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getsKey_GroupAuthor() {
        return sKey_GroupAuthor;
    }

    public void setsKey_GroupAuthor(String sKey_GroupAuthor) {
        this.sKey_GroupAuthor = sKey_GroupAuthor;
    }

    public Boolean getbWrite() {
        return bWrite;
    }

    public void setbWrite(Boolean bWrite) {
        this.bWrite = bWrite;
    }

    public Boolean getbNeedECP() {
        return bNeedECP;
    }

    public void setbNeedECP(Boolean bNeedECP) {
        this.bNeedECP = bNeedECP;
    }

    public String getsLogin() {
        return sLogin;
    }

    public void setsLogin(String sLogin) {
        this.sLogin = sLogin;
    }

    public String getsID_Field() {
        return sID_Field;
    }

    public void setsID_Field(String sID_Field) {
        this.sID_Field = sID_Field;
    }

    public String getsID_File_ForSign() {
        return sID_File_ForSign;
    }

    public void setsID_File_ForSign(String sID_File_ForSign) {
        this.sID_File_ForSign = sID_File_ForSign;
    }

    public DateTime getsDate() {
        return sDate;
    }

    public void setsDate(DateTime sDate) {
        this.sDate = sDate;
    }

    public DateTime getsDateECP() {
        return sDateECP;
    }

    public void setsDateECP(DateTime sDateECP) {
        this.sDateECP = sDateECP;
    }

    public DocumentStepSubjectSignType getoDocumentStepSubjectSignType() {
        return oDocumentStepSubjectSignType;
    }

    public void setoDocumentStepSubjectSignType(DocumentStepSubjectSignType oDocumentStepSubjectSignType) {
        this.oDocumentStepSubjectSignType = oDocumentStepSubjectSignType;
    }

    public Boolean getbUrgent() {
        return bUrgent;
    }

    public void setbUrgent(Boolean bUrgent) {
        this.bUrgent = bUrgent;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().configure(SerializationFeature.WRAP_ROOT_VALUE, true)
                    .writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            LOG.info(String.format("error [%s]", e.getMessage()));
        }
        return null;
    }

}