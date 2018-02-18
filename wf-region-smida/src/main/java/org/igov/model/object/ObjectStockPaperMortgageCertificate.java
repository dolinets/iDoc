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
public class ObjectStockPaperMortgageCertificate extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "oSubjectOrgan_Emitente")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_Emitente")
    private SubjectOrgan oSubjectOrgan_Emitente;

    @JsonProperty(value = "sDateDecisionRegistry")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateDecisionRegistry;
    
    @JsonProperty(value = "nSizeOwnCapital")
    @Column
    private Long nSizeOwnCapital;
    
    @JsonProperty(value = "bNonBankingFlag")
    @Column
    private boolean bNonBankingFlag;
    
    @JsonProperty(value = "sDatePermissionEmissionNonBanking")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDatePermissionEmissionNonBanking;

    @JsonProperty(value = "sNumberPermissionEmissionNonBanking")
    @Column
    private String sNumberPermissionEmissionNonBanking;
    
    @JsonProperty(value = "bSuspensionFlag")
    @Column
    private boolean bSuspensionFlag;

    @JsonProperty(value = "sDateSuspensionEmission")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateSuspensionEmission;

    @JsonProperty(value = "sNumberSuspensionEmission")
    @Column
    private String sNumberSuspensionEmission;
    
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
    
    @JsonProperty(value = "oDictionary_TypeIssuanceMortgageCertificate")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_TypeIssuanceMortgageCertificate")
    private Dictionary oDictionary_TypeIssuanceMortgageCertificate;
     
    @JsonProperty(value = "oDictionary_TypeMortgageCertificate")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_TypeMortgageCertificate")
    private Dictionary oDictionary_TypeMortgageCertificate;
    
    @JsonProperty(value = "oDictionary_TypePlacementMortgageCertificate")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_TypePlacementMortgageCertificate")
    private Dictionary oDictionary_TypePlacementMortgageCertificate;
    
    @JsonProperty(value = "oActionEvent_Issue")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_Issue")
    private ActionEvent oActionEvent_Issue;

    @JsonProperty(value = "sDateRegistarationInformation")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateRegistarationInformation;
    
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
    
    @JsonProperty(value = "nPaidStateFee")
    @Column
    private Long nPaidStateFee;
    
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

    @JsonProperty(value = "oSubjectOrgan_Depositary")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_Depositary")
    private SubjectOrgan oSubjectOrgan_Depositary;
    
    @JsonProperty(value = "oSubjectOrgan_Manager")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_Manager")
    private SubjectOrgan oSubjectOrgan_Manager;

    @JsonProperty(value = "oSubjectOrgan_MortgageManager")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_MortgageManager")
    private SubjectOrgan oSubjectOrgan_MortgageManager;

    @JsonProperty(value = "oActionEvent_Change")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_Change")
    private ActionEvent oActionEvent_Change;
    
    @JsonProperty(value = "oActionEvent_Report")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_Report")
    private ActionEvent oActionEvent_Report;

    @JsonProperty(value = "sDateOrderResults")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateOrderResults;    
    
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

    public DateTime getsDateDecisionRegistry() {
        return sDateDecisionRegistry;
    }

    public void setsDateDecisionRegistry(DateTime sDateDecisionRegistry) {
        this.sDateDecisionRegistry = sDateDecisionRegistry;
    }

    public Long getnSizeOwnCapital() {
        return nSizeOwnCapital;
    }

    public void setnSizeOwnCapital(Long nSizeOwnCapital) {
        this.nSizeOwnCapital = nSizeOwnCapital;
    }

    public boolean isbNonBankingFlag() {
        return bNonBankingFlag;
    }

    public void setbNonBankingFlag(boolean bNonBankingFlag) {
        this.bNonBankingFlag = bNonBankingFlag;
    }

    public DateTime getsDatePermissionEmissionNonBanking() {
        return sDatePermissionEmissionNonBanking;
    }

    public void setsDatePermissionEmissionNonBanking(DateTime sDatePermissionEmissionNonBanking) {
        this.sDatePermissionEmissionNonBanking = sDatePermissionEmissionNonBanking;
    }

    public String getsNumberPermissionEmissionNonBanking() {
        return sNumberPermissionEmissionNonBanking;
    }

    public void setsNumberPermissionEmissionNonBanking(String sNumberPermissionEmissionNonBanking) {
        this.sNumberPermissionEmissionNonBanking = sNumberPermissionEmissionNonBanking;
    }

    public boolean isbSuspensionFlag() {
        return bSuspensionFlag;
    }

    public void setbSuspensionFlag(boolean bSuspensionFlag) {
        this.bSuspensionFlag = bSuspensionFlag;
    }

    public DateTime getsDateSuspensionEmission() {
        return sDateSuspensionEmission;
    }

    public void setsDateSuspensionEmission(DateTime sDateSuspensionEmission) {
        this.sDateSuspensionEmission = sDateSuspensionEmission;
    }

    public String getsNumberSuspensionEmission() {
        return sNumberSuspensionEmission;
    }

    public void setsNumberSuspensionEmission(String sNumberSuspensionEmission) {
        this.sNumberSuspensionEmission = sNumberSuspensionEmission;
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

    public Dictionary getoDictionary_TypeIssuanceMortgageCertificate() {
        return oDictionary_TypeIssuanceMortgageCertificate;
    }

    public void setoDictionary_TypeIssuanceMortgageCertificate(Dictionary oDictionary_TypeIssuanceMortgageCertificate) {
        this.oDictionary_TypeIssuanceMortgageCertificate = oDictionary_TypeIssuanceMortgageCertificate;
    }

    public Dictionary getoDictionary_TypeMortgageCertificate() {
        return oDictionary_TypeMortgageCertificate;
    }

    public void setoDictionary_TypeMortgageCertificate(Dictionary oDictionary_TypeMortgageCertificate) {
        this.oDictionary_TypeMortgageCertificate = oDictionary_TypeMortgageCertificate;
    }

    public Dictionary getoDictionary_TypePlacementMortgageCertificate() {
        return oDictionary_TypePlacementMortgageCertificate;
    }

    public void setoDictionary_TypePlacementMortgageCertificate(Dictionary oDictionary_TypePlacementMortgageCertificate) {
        this.oDictionary_TypePlacementMortgageCertificate = oDictionary_TypePlacementMortgageCertificate;
    }

    public ActionEvent getoActionEvent_Issue() {
        return oActionEvent_Issue;
    }

    public void setoActionEvent_Issue(ActionEvent oActionEvent_Issue) {
        this.oActionEvent_Issue = oActionEvent_Issue;
    }

    public DateTime getsDateRegistarationInformation() {
        return sDateRegistarationInformation;
    }

    public void setsDateRegistarationInformation(DateTime sDateRegistarationInformation) {
        this.sDateRegistarationInformation = sDateRegistarationInformation;
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

    public Long getnPaidStateFee() {
        return nPaidStateFee;
    }

    public void setnPaidStateFee(Long nPaidStateFee) {
        this.nPaidStateFee = nPaidStateFee;
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

    public SubjectOrgan getoSubjectOrgan_MortgageManager() {
        return oSubjectOrgan_MortgageManager;
    }

    public void setoSubjectOrgan_MortgageManager(SubjectOrgan oSubjectOrgan_MortgageManager) {
        this.oSubjectOrgan_MortgageManager = oSubjectOrgan_MortgageManager;
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

    public DateTime getsDateOrderResults() {
        return sDateOrderResults;
    }

    public void setsDateOrderResults(DateTime sDateOrderResults) {
        this.sDateOrderResults = sDateOrderResults;
    }

    public ActionEvent getoActionEvent_Terminate() {
        return oActionEvent_Terminate;
    }

    public void setoActionEvent_Terminate(ActionEvent oActionEvent_Terminate) {
        this.oActionEvent_Terminate = oActionEvent_Terminate;
    }

    

}
