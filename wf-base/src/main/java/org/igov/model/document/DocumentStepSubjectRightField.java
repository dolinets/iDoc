package org.igov.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.igov.model.core.AbstractEntity;

import javax.persistence.Entity;

@Entity
@ApiModel(description="Поле права субьекта-подписанта шага документа")
public class DocumentStepSubjectRightField extends AbstractEntity {

    /* Field-backup - do not delete @JsonIgnore
    @ManyToOne(targetEntity = DocumentStepSubjectRight.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "nID_DocumentStepSubjectRight")
    @ApiModelProperty(value = "Право субьекта-подписанта шага документа (на что 'вяжется' запись)", required = true)
    private DocumentStepSubjectRight documentStepSubjectRight;*/

    @JsonProperty(value = "sMask_FieldID")
    @ApiModelProperty(value = "Строка-маска выборки полей шага документа (стандартныве правила  символов: *-любое значение, ?-любой символ)", required = true)
    private String sMask_FieldID;

    @JsonProperty(value = "bWrite")
    @ApiModelProperty(value = "Флаг права записи в поле (true-право записи/false-только право чтения/null-скрыто(???))", required = false)
    private Boolean bWrite;

    /* Field-backup - do not delete public DocumentStepSubjectRight getDocumentStepSubjectRight() {
        return documentStepSubjectRight;
    }

    public void setDocumentStepSubjectRight(DocumentStepSubjectRight documentStepSubjectRight) {
        this.documentStepSubjectRight = documentStepSubjectRight;
    }*/

    public String getsMask_FieldID() {
        return sMask_FieldID;
    }

    public void setsMask_FieldID(String sMask_FieldID) {
        this.sMask_FieldID = sMask_FieldID;
    }

    public Boolean getbWrite() {
        return bWrite;
    }

    public void setbWrite(Boolean bWrite) {
        this.bWrite = bWrite;
    }

    @Override
    public String toString() {
        return "DocumentStepSubjectRightField{" +
                "id=" + getId() + ", " +
                //"documentStepSubjectRight=" + documentStepSubjectRight +
                ", sMask_FieldID='" + sMask_FieldID + '\'' +
                ", bWrite=" + bWrite +
                '}';
    }
}