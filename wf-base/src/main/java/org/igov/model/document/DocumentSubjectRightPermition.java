package org.igov.model.document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import org.igov.model.core.AbstractEntity;

/**
 *
 * @author Kovilin
 */
@Entity
@ApiModel(description="Разренения прав субьекта-подписанта шага документа(???)")
public class DocumentSubjectRightPermition extends AbstractEntity{
    
    /*@JsonProperty(value = "nID_DocumentStepSubjectRight")
    @Column
    private Long nID_DocumentStepSubjectRight;*/
    
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_DocumentStepSubjectRight")
    @ApiModelProperty(value = "Право субьекта-подписанта шага документа (на что 'вяжется' запись)", required = true)
    DocumentStepSubjectRight oDocumentStepSubjectRight;
    
    @JsonProperty(value = "PermitionType")
    @Column
    @ApiModelProperty(value = "Тип разрешений(???)")
    private String PermitionType;
    
    @JsonProperty(value = "sKeyGroupeSource")
    @Column
    @ApiModelProperty(value = "Строка-ключ группы источника(???)")
    private String sKeyGroupeSource;
    
    @JsonProperty(value = "sID_Group_Activiti")
    @Column
    @ApiModelProperty(value = "Строка-ИД группы Activiti(???)")
    private String sID_Group_Activiti;
    
    @ApiModelProperty(value = "Строка-обьекта значения(???)")
    @JsonProperty(value = "soValue")
    @Column
    private String soValue;
    
    @Transient
    @JsonIgnore
    private String sKeyGroup_Postfix;
    
    @Transient
    @JsonIgnore
    private String sKey_Step;

    public DocumentStepSubjectRight getoDocumentStepSubjectRight() {
        return oDocumentStepSubjectRight;
    }

    public void setoDocumentStepSubjectRight(DocumentStepSubjectRight oDocumentStepSubjectRight) {
        this.oDocumentStepSubjectRight = oDocumentStepSubjectRight;
    }
    
    public String getsID_Group_Activiti() {
        return sID_Group_Activiti;
    }

    public void setsID_Group_Activiti(String sID_Group_Activiti) {
        this.sID_Group_Activiti = sID_Group_Activiti;
    }

    public String getsKey_Step() {
        return sKey_Step;
    }

    public void setsKey_Step(String sKey_Step) {
        this.sKey_Step = sKey_Step;
    }

    public void setsKeyGroup_Postfix(String sKeyGroup_Postfix) {
        this.sKeyGroup_Postfix = sKeyGroup_Postfix;
    }

    public String getsKeyGroup_Postfix() {
        return sKeyGroup_Postfix;
    }
            
    /*public Long getnID_DocumentStepSubjectRight() {
        return nID_DocumentStepSubjectRight;
    }*/

    public String getPermitionType() {
        return PermitionType;
    }

    public String getsKeyGroupeSource() {
        return sKeyGroupeSource;
    }

    /*public void setnID_DocumentStepSubjectRight(Long nID_DocumentStepSubjectRight) {
        this.nID_DocumentStepSubjectRight = nID_DocumentStepSubjectRight;
    }*/

    public void setPermitionType(String PermitionType) {
        this.PermitionType = PermitionType;
    }

    public void setsKeyGroupeSource(String sKeyGroupeSource) {
        this.sKeyGroupeSource = sKeyGroupeSource;
    }

    public String getSoValue() {
        return soValue;
    }

    public void setSoValue(String soValue) {
        this.soValue = soValue;
    }
}