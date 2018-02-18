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
import org.igov.model.document.DocumentStatutory;
import org.igov.model.subject.SubjectContact;
import org.igov.model.subject.organ.SubjectOrgan;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class ObjectStockPaperMortgageBond extends AbstractEntity {

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

    @JsonProperty(value = "oDictionary_TypeMortgageBond")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_TypeMortgageBond")
    private Dictionary oDictionary_TypeMortgageBond;

    @JsonProperty(value = "oDictionary_TypeIssuanceMortgageBond")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_TypeIssuanceMortgageBond")
    private Dictionary oDictionary_TypeIssuanceMortgageBond;
  
    @JsonProperty(value = "nPaidStateFee")
    @Column
    private Long nPaidStateFee;

    @JsonProperty(value = "sMortgageCoverageSize")
    @Column
    private String sMortgageCoverageSize;

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

    @JsonProperty(value = "sDateEndTurnover")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateEndTurnover;

    @JsonProperty(value = "sDateBeginRepayment")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateBeginRepayment;

    @JsonProperty(value = "sDateEndRepayment")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateEndRepayment;

    @JsonProperty(value = "oActionEvent_Issue")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_Issue")
    private ActionEvent oActionEvent_Issue;

    @JsonProperty(value = "sDateRegistarationProspectus")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateRegistarationProspectus;

    @JsonProperty(value = "bFictitious")
    @Column
    private boolean bFictitious;

    @JsonProperty(value = "oActionEvent_Fictitious")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_Fictitious")
    private ActionEvent oActionEvent_Fictitious;

    @JsonProperty(value = "oSubjectOrgan_Depositary")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_Depositary")
    private SubjectOrgan oSubjectOrgan_Depositary;

    @JsonProperty(value = "oSubjectOrgan_Manager")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_Manager")
    private SubjectOrgan oSubjectOrgan_Manager;

    @JsonProperty(value = "oSubjectOrgan_ServingInstitution")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_ServingInstitution")
    private SubjectOrgan oSubjectOrgan_ServingInstitution;

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

    public Dictionary getoDictionary_TypeMortgageBond() {
        return oDictionary_TypeMortgageBond;
    }

    public void setoDictionary_TypeMortgageBond(Dictionary oDictionary_TypeMortgageBond) {
        this.oDictionary_TypeMortgageBond = oDictionary_TypeMortgageBond;
    }

    public Dictionary getoDictionary_TypeIssuanceMortgageBond() {
        return oDictionary_TypeIssuanceMortgageBond;
    }

    public void setoDictionary_TypeIssuanceMortgageBond(Dictionary oDictionary_TypeIssuanceMortgageBond) {
        this.oDictionary_TypeIssuanceMortgageBond = oDictionary_TypeIssuanceMortgageBond;
    }

    public Long getnPaidStateFee() {
        return nPaidStateFee;
    }

    public void setnPaidStateFee(Long nPaidStateFee) {
        this.nPaidStateFee = nPaidStateFee;
    }

    public String getsMortgageCoverageSize() {
        return sMortgageCoverageSize;
    }

    public void setsMortgageCoverageSize(String sMortgageCoverageSize) {
        this.sMortgageCoverageSize = sMortgageCoverageSize;
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

    public DateTime getsDateEndTurnover() {
        return sDateEndTurnover;
    }

    public void setsDateEndTurnover(DateTime sDateEndTurnover) {
        this.sDateEndTurnover = sDateEndTurnover;
    }

    public DateTime getsDateBeginRepayment() {
        return sDateBeginRepayment;
    }

    public void setsDateBeginRepayment(DateTime sDateBeginRepayment) {
        this.sDateBeginRepayment = sDateBeginRepayment;
    }

    public DateTime getsDateEndRepayment() {
        return sDateEndRepayment;
    }

    public void setsDateEndRepayment(DateTime sDateEndRepayment) {
        this.sDateEndRepayment = sDateEndRepayment;
    }

    public ActionEvent getoActionEvent_Issue() {
        return oActionEvent_Issue;
    }

    public void setoActionEvent_Issue(ActionEvent oActionEvent_Issue) {
        this.oActionEvent_Issue = oActionEvent_Issue;
    }

    public DateTime getsDateRegistarationProspectus() {
        return sDateRegistarationProspectus;
    }

    public void setsDateRegistarationProspectus(DateTime sDateRegistarationProspectus) {
        this.sDateRegistarationProspectus = sDateRegistarationProspectus;
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

    public SubjectOrgan getoSubjectOrgan_Depositary() {
        return oSubjectOrgan_Depositary;
    }

    public void setoSubjectOrgan_Depositary(SubjectOrgan oSubjectOrgan_Depositary) {
        this.oSubjectOrgan_Depositary = oSubjectOrgan_Depositary;
    }

    public SubjectOrgan getoSubjectOrgan_Manager() {
        return oSubjectOrgan_Manager;
    }

    public void setoSubjectOrgan_Manager(SubjectOrgan oSubjectOrgan_Manager) {
        this.oSubjectOrgan_Manager = oSubjectOrgan_Manager;
    }

    public SubjectOrgan getoSubjectOrgan_ServingInstitution() {
        return oSubjectOrgan_ServingInstitution;
    }

    public void setoSubjectOrgan_ServingInstitution(SubjectOrgan oSubjectOrgan_ServingInstitution) {
        this.oSubjectOrgan_ServingInstitution = oSubjectOrgan_ServingInstitution;
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
