package org.igov.model.document.access;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.sf.brunneng.jom.annotations.Identifier;
import org.hibernate.annotations.Type;
import org.igov.model.core.AbstractEntity;
import org.joda.time.DateTime;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "DocumentAccess")
@ApiModel(description="Объекты административно-территориального устройства Украины (города, села и т.д.)")
public class DocumentAccess extends AbstractEntity {

    @Column
    @ApiModelProperty(value = "Уникальный номер-ИД документа", required = true)
    private Long nID_Document;

    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    @ApiModelProperty(value = "Дата создания", required = true)
    private DateTime sDateCreate;

    @Column
    @ApiModelProperty(value = "Код MS", required = true)
    private Long nMS;

    @Column
    @ApiModelProperty(value = "ФИО", required = true)
    private String sFIO;

    @Column
    @ApiModelProperty(value = "Описание назначения", required = true)
    private String sTarget;

    @Column
    @ApiModelProperty(value = "Номер телефона", required = true)
    private String sTelephone;

    @Column
    @ApiModelProperty(value = "E-mail", required = true)
    private String sMail;

    @Column
    @ApiModelProperty(value = "Описание секрета", required = true)
    private String sSecret;

    @Column
    @ApiModelProperty(value = "Вопрос", required = true)
    private String sAnswer;

    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    @ApiModelProperty(value = "Дата истечения срока действия", required = true)
    private DateTime sDateAnswerExpire;

    @Column
    @ApiModelProperty(value = "Код", required = true)
    private String sCode;

    @Column
    @ApiModelProperty(value = "Тип кода", required = true)
    private String sCodeType;

    @Identifier
    public Long getID_Document() {
        return nID_Document;
    }

    public void setID_Document(Long nID_Document) {
        this.nID_Document = nID_Document;
    }

    public DateTime getDateCreate() {
        return sDateCreate;
    }

    public void setDateCreate(DateTime sDateCreate) {
        this.sDateCreate = sDateCreate;
    }

    public Long getMS() {
        return nMS;
    }

    public void setMS(Long n) {
        this.nMS = n;
    }

    public String getFIO() {
        return sFIO;
    }

    public void setFIO(String sFIO) {
        this.sFIO = sFIO;
    }

    public String getTarget() {
        return sTarget;
    }

    public void setTarget(String sTarget) {
        this.sTarget = sTarget;
    }

    public String getTelephone() {
        return sTelephone;
    }

    public void setTelephone(String sTelephone) {
        this.sTelephone = sTelephone;
    }

    public String getMail() {
        return sMail;
    }

    public void setMail(String sMail) {
        this.sMail = sMail;
    }

    public String getSecret() {
        return sSecret;
    }

    public void setSecret(String sSecret) {
        this.sSecret = sSecret;
    }

    public String getAnswer() {
        return sAnswer;
    }

    public void setAnswer(String s) {
        this.sAnswer = s;
    }

    public DateTime getDateAnswerExpire() {
        return sDateAnswerExpire;
    }

    public void setDateAnswerExpire(DateTime sDateAnswerExpire) {
        this.sDateAnswerExpire = sDateAnswerExpire;
    }

    public String getsCode() {
        return sCode;
    }

    public void setsCode(String sCode) {
        this.sCode = sCode;
    }

    public String getsCodeType() {
        return sCodeType;
    }

    public void setsCodeType(String sCodeType) {
        this.sCodeType = sCodeType;
    }

    @Override
    public String toString() {
        return "{\n" + "nID:" + getId() + "\nnID_Document:" + nID_Document + "sDateCreate:" + sDateCreate +
                "\nnMS:" + nMS + "\nsFIO:" + sFIO + "\nsTarget:" + sTarget + "\nsTelephone:" + sTelephone +
                "\nsMail:" + sMail + "\nsSecret:" + sSecret + "\nsAnswer:" + sAnswer + "" +
                "\nsDateAnswerExpire:" + sDateAnswerExpire +
                "\nsCode:" + sCode + "\nsCodeType:" + sCodeType + "}";
    }
}