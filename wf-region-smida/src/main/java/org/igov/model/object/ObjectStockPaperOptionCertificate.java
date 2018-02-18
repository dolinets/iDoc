package org.igov.model.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.Type;
import org.igov.model.action.event.ActionEvent;
import org.igov.model.core.AbstractEntity;
import org.igov.model.dictionary.Dictionary;
import org.igov.model.document.Document;
import org.igov.model.document.TermType;
import org.igov.model.subject.organ.SubjectOrgan;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class ObjectStockPaperOptionCertificate extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "oSubjectOrgan_Emitente")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_Emitente")
    private SubjectOrgan oSubjectOrgan_Emitente;
    
    @JsonProperty(value = "sSeries")
    @Column
    private String sSeries;

    @JsonProperty(value = "nCommonDenomination")
    @Column
    private Long nCommonDenomination;
    
    @JsonProperty(value = "nQuantity")
    @Column
    private Long nQuantity;
    
    @JsonProperty(value = "nDenomination")
    @Column
    private Long nDenomination;

    @JsonProperty(value = "oDictionary_TypeOptionCertificate")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_TypeOptionCertificate")
    private Dictionary oDictionary_TypeOptionCertificate;
    
    @JsonProperty(value = "oDictionary_VarietyOptionCertificate")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_VarietyOptionCertificate")
    private Dictionary oDictionary_VarietyOptionCertificate;

    @JsonProperty(value = "sDateBeginIPO")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateBeginIPO;
    
    @JsonProperty(value = "sDateEndIPO")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateEndIPO;

    @JsonProperty(value = "oTermType")
    @ManyToOne(targetEntity = TermType.class)
    @JoinColumn(name = "nID_TermType")
    private TermType oTermType;

    @JsonProperty(value = "nTermCount")
    @Column
    private Long nTermCount;
    
    @JsonProperty(value = "sNumberCertificate")
    @Column
    private String sNumberCertificate;

    @JsonProperty(value = "sDateIssue")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateIssue;

    @JsonProperty(value = "oActionEvent_Issue")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_Issue")
    private ActionEvent oActionEvent_Issue;

    @JsonProperty(value = "bFictitious")
    @Column
    private boolean bFictitious;

    @JsonProperty(value = "oActionEvent_Fictitious")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_Fictitious")
    private ActionEvent oActionEvent_Fictitious;

    @JsonProperty(value = "oActionEvent_Change")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_Change")
    private ActionEvent oActionEvent_Change;
    
    @JsonProperty(value = "oActionEvent_Report")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_Report")
    private ActionEvent oActionEvent_Report;
    
    @JsonProperty(value = "oActionEvent_Terminate")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_Terminate")
    private ActionEvent oActionEvent_Terminate;

    public SubjectOrgan getoSubjectOrgan_Emitente() {
        return oSubjectOrgan_Emitente;
    }

    public void setoSubjectOrgan_Emitente(SubjectOrgan oSubjectOrgan_Emitente) {
        this.oSubjectOrgan_Emitente = oSubjectOrgan_Emitente;
    }

    public String getsSeries() {
        return sSeries;
    }

    public void setsSeries(String sSeries) {
        this.sSeries = sSeries;
    }

    public Long getnCommonDenomination() {
        return nCommonDenomination;
    }

    public void setnCommonDenomination(Long nCommonDenomination) {
        this.nCommonDenomination = nCommonDenomination;
    }

    public Long getnQuantity() {
        return nQuantity;
    }

    public void setnQuantity(Long nQuantity) {
        this.nQuantity = nQuantity;
    }

    public Long getnDenomination() {
        return nDenomination;
    }

    public void setnDenomination(Long nDenomination) {
        this.nDenomination = nDenomination;
    }

    public Dictionary getoDictionary_TypeOptionCertificate() {
        return oDictionary_TypeOptionCertificate;
    }

    public void setoDictionary_TypeOptionCertificate(Dictionary oDictionary_TypeOptionCertificate) {
        this.oDictionary_TypeOptionCertificate = oDictionary_TypeOptionCertificate;
    }

    public Dictionary getoDictionary_VarietyOptionCertificate() {
        return oDictionary_VarietyOptionCertificate;
    }

    public void setoDictionary_VarietyOptionCertificate(Dictionary oDictionary_VarietyOptionCertificate) {
        this.oDictionary_VarietyOptionCertificate = oDictionary_VarietyOptionCertificate;
    }

    public DateTime getsDateBeginIPO() {
        return sDateBeginIPO;
    }

    public void setsDateBeginIPO(DateTime sDateBeginIPO) {
        this.sDateBeginIPO = sDateBeginIPO;
    }

    public DateTime getsDateEndIPO() {
        return sDateEndIPO;
    }

    public void setsDateEndIPO(DateTime sDateEndIPO) {
        this.sDateEndIPO = sDateEndIPO;
    }

    public TermType getoTermType() {
        return oTermType;
    }

    public void setoTermType(TermType oTermType) {
        this.oTermType = oTermType;
    }

    public Long getnTermCount() {
        return nTermCount;
    }

    public void setnTermCount(Long nTermCount) {
        this.nTermCount = nTermCount;
    }

    public String getsNumberCertificate() {
        return sNumberCertificate;
    }

    public void setsNumberCertificate(String sNumberCertificate) {
        this.sNumberCertificate = sNumberCertificate;
    }

    public DateTime getsDateIssue() {
        return sDateIssue;
    }

    public void setsDateIssue(DateTime sDateIssue) {
        this.sDateIssue = sDateIssue;
    }

    public ActionEvent getoActionEvent_Issue() {
        return oActionEvent_Issue;
    }

    public void setoActionEvent_Issue(ActionEvent oActionEvent_Issue) {
        this.oActionEvent_Issue = oActionEvent_Issue;
    }

    public boolean isbFictitious() {
        return bFictitious;
    }

    public void setbFictitious(boolean bFictitious) {
        this.bFictitious = bFictitious;
    }

    public ActionEvent getoActionEvent_Fictitious() {
        return oActionEvent_Fictitious;
    }

    public void setoActionEvent_Fictitious(ActionEvent oActionEvent_Fictitious) {
        this.oActionEvent_Fictitious = oActionEvent_Fictitious;
    }

    public ActionEvent getoActionEvent_Change() {
        return oActionEvent_Change;
    }

    public void setoActionEvent_Change(ActionEvent oActionEvent_Change) {
        this.oActionEvent_Change = oActionEvent_Change;
    }

    public ActionEvent getoActionEvent_Report() {
        return oActionEvent_Report;
    }

    public void setoActionEvent_Report(ActionEvent oActionEvent_Report) {
        this.oActionEvent_Report = oActionEvent_Report;
    }

    public ActionEvent getoActionEvent_Terminate() {
        return oActionEvent_Terminate;
    }

    public void setoActionEvent_Terminate(ActionEvent oActionEvent_Terminate) {
        this.oActionEvent_Terminate = oActionEvent_Terminate;
    }

}
