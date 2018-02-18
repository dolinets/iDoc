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
public class ObjectStockPaperBond extends AbstractEntity {

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
    
    @JsonProperty(value = "oDictionary_TypeBond")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_TypeBond")
    private Dictionary oDictionary_TypeBond; 
    
    @JsonProperty(value = "oDictionary_TypeIssuanceBond")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_TypeIssuanceBond")
    private Dictionary oDictionary_TypeIssuanceBond;
    
    @JsonProperty(value = "oDictionary_TypeSecuredBond")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_TypeSecuredBond")
    private Dictionary oDictionary_TypeSecuredBond;
    
    @JsonProperty(value = "oDictionary_GuarantorBond")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_GuarantorBond")
    private Dictionary oDictionary_GuarantorBond;
     
    @JsonProperty(value = "oSubjectOrgan_GuarantorBond")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_GuarantorBond")
    private SubjectOrgan oSubjectOrgan_GuarantorBond;

    @JsonProperty(value = "oDictionary_FormExistenceBond")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_FormExistenceBond")
    private Dictionary oDictionary_FormExistenceBond;
    
    @JsonProperty(value = "oDictionary_TypePlacementBond")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_TypePlacementBond")
    private Dictionary oDictionary_TypePlacementBond;
    
    @JsonProperty(value = "nPaidStateFee")
    @Column
    private Long nPaidStateFee;
    
    @JsonProperty(value = "nInterestRate")
    @Column
    private Long nInterestRate;
    
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
    
    @JsonProperty(value = "oDocumentStatutory_Certificate")
    @ManyToOne(targetEntity = DocumentStatutory.class)
    @JoinColumn(name = "nID_DocumentStatutory_Certificate")
    private DocumentStatutory oDocumentStatutory_Certificate;

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

    @JsonProperty("bLoanBonds")
    @Column
    private boolean bLoanBonds;

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

    public Dictionary getoDictionary_TypeBond() {
        return oDictionary_TypeBond;
    }

    public void setoDictionary_TypeBond(Dictionary oDictionary_TypeBond) {
        this.oDictionary_TypeBond = oDictionary_TypeBond;
    }

    public Dictionary getoDictionary_TypeIssuanceBond() {
        return oDictionary_TypeIssuanceBond;
    }

    public void setoDictionary_TypeIssuanceBond(Dictionary oDictionary_TypeIssuanceBond) {
        this.oDictionary_TypeIssuanceBond = oDictionary_TypeIssuanceBond;
    }

    public Dictionary getoDictionary_TypeSecuredBond() {
        return oDictionary_TypeSecuredBond;
    }

    public void setoDictionary_TypeSecuredBond(Dictionary oDictionary_TypeSecuredBond) {
        this.oDictionary_TypeSecuredBond = oDictionary_TypeSecuredBond;
    }

    public Dictionary getoDictionary_GuarantorBond() {
        return oDictionary_GuarantorBond;
    }

    public void setoDictionary_GuarantorBond(Dictionary oDictionary_GuarantorBond) {
        this.oDictionary_GuarantorBond = oDictionary_GuarantorBond;
    }

    public SubjectOrgan getoSubjectOrgan_GuarantorBond() {
        return oSubjectOrgan_GuarantorBond;
    }

    public void setoSubjectOrgan_GuarantorBond(SubjectOrgan oSubjectOrgan_GuarantorBond) {
        this.oSubjectOrgan_GuarantorBond = oSubjectOrgan_GuarantorBond;
    }

    public Dictionary getoDictionary_FormExistenceBond() {
        return oDictionary_FormExistenceBond;
    }

    public void setoDictionary_FormExistenceBond(Dictionary oDictionary_FormExistenceBond) {
        this.oDictionary_FormExistenceBond = oDictionary_FormExistenceBond;
    }

    public Dictionary getoDictionary_TypePlacementBond() {
        return oDictionary_TypePlacementBond;
    }

    public void setoDictionary_TypePlacementBond(Dictionary oDictionary_TypePlacementBond) {
        this.oDictionary_TypePlacementBond = oDictionary_TypePlacementBond;
    }

    public Long getnPaidStateFee() {
        return nPaidStateFee;
    }

    public void setnPaidStateFee(Long nPaidStateFee) {
        this.nPaidStateFee = nPaidStateFee;
    }

    public Long getnInterestRate() {
        return nInterestRate;
    }

    public void setnInterestRate(Long nInterestRate) {
        this.nInterestRate = nInterestRate;
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

    public DocumentStatutory getoDocumentStatutory_Certificate() {
        return oDocumentStatutory_Certificate;
    }

    public void setoDocumentStatutory_Certificate(DocumentStatutory oDocumentStatutory_Certificate) {
        this.oDocumentStatutory_Certificate = oDocumentStatutory_Certificate;
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

    public boolean isbLoanBonds() {
        return bLoanBonds;
    }

    public void setbLoanBonds(boolean bLoanBonds) {
        this.bLoanBonds = bLoanBonds;
    }
}
