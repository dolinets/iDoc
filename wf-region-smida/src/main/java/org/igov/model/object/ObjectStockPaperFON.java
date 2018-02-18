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
import org.igov.model.subject.SubjectOperatorBank;
import org.igov.model.subject.organ.SubjectOrgan;
import org.igov.model.subject.organ.stock.paper.SubjectStockPaperFON;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class ObjectStockPaperFON extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "sOrganPermissionEmission")
    @Column
    private String sOrganPermissionEmission;

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
    
    @JsonProperty(value = "sDatePermissionEmission")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDatePermissionEmission;

    @JsonProperty(value = "sNumberPermissionEmission")
    @Column
    private String sNumberPermissionEmission;

    @JsonProperty(value = "sDateDecisionRegistry")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateDecisionRegistry;

    @JsonProperty(value = "nSizeOwnCapital")
    @Column
    private Long nSizeOwnCapital;

    @JsonProperty(value = "oSubjectStockPaperFON")
    @ManyToOne(targetEntity = SubjectStockPaperFON.class)
    @JoinColumn(name = "nID_SubjectStockPaperFON")
    private SubjectStockPaperFON oSubjectStockPaperFON;
    
    @JsonProperty(value = "oSubjectOperatorBank_FON")
    @ManyToOne(targetEntity = SubjectOperatorBank.class)
    @JoinColumn(name = "nID_SubjectOperatorBank_FON")
    private SubjectOperatorBank oSubjectOperatorBank_FON;
    
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
    
    @JsonProperty(value = "oDictionary_FormIssuanceFON")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_FormIssuanceFON")
    private Dictionary oDictionary_FormIssuanceFON;
    
    @JsonProperty(value = "oDictionary_TypeIssuanceFON")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_TypeIssuanceFON")
    private Dictionary oDictionary_TypeIssuanceFON;
    
    @JsonProperty(value = "oDictionary_TypePlacementFON")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_TypePlacementFON")
    private Dictionary oDictionary_TypePlacementFON;
    
    @JsonProperty(value = "nPaidStateFee")
    @Column
    private Long nPaidStateFee;
    
    @JsonProperty(value = "sInvestmentDirection")
    @Column
    private String sInvestmentDirection;

    @JsonProperty(value = "oSubjectOrgan_RealEstateDeveloper")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_RealEstateDeveloper")
    private SubjectOrgan oSubjectOrgan_RealEstateDeveloper;

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
    
    @JsonProperty(value = "oDocumentStatutory_Certificate")
    @ManyToOne(targetEntity = DocumentStatutory.class)
    @JoinColumn(name = "nID_DocumentStatutory_Certificate")
    private DocumentStatutory oDocumentStatutory_Certificate;

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

    public String getsOrganPermissionEmission() {
        return sOrganPermissionEmission;
    }

    public void setsOrganPermissionEmission(String sOrganPermissionEmission) {
        this.sOrganPermissionEmission = sOrganPermissionEmission;
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

    public DateTime getsDatePermissionEmission() {
        return sDatePermissionEmission;
    }

    public void setsDatePermissionEmission(DateTime sDatePermissionEmission) {
        this.sDatePermissionEmission = sDatePermissionEmission;
    }

    public String getsNumberPermissionEmission() {
        return sNumberPermissionEmission;
    }

    public void setsNumberPermissionEmission(String sNumberPermissionEmission) {
        this.sNumberPermissionEmission = sNumberPermissionEmission;
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

    public SubjectStockPaperFON getoSubjectStockPaperFON() {
        return oSubjectStockPaperFON;
    }

    public void setoSubjectStockPaperFON(SubjectStockPaperFON oSubjectStockPaperFON) {
        this.oSubjectStockPaperFON = oSubjectStockPaperFON;
    }

    public SubjectOperatorBank getoSubjectOperatorBank_FON() {
        return oSubjectOperatorBank_FON;
    }

    public void setoSubjectOperatorBank_FON(SubjectOperatorBank oSubjectOperatorBank_FON) {
        this.oSubjectOperatorBank_FON = oSubjectOperatorBank_FON;
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

    public Dictionary getoDictionary_FormIssuanceFON() {
        return oDictionary_FormIssuanceFON;
    }

    public void setoDictionary_FormIssuanceFON(Dictionary oDictionary_FormIssuanceFON) {
        this.oDictionary_FormIssuanceFON = oDictionary_FormIssuanceFON;
    }

    public Dictionary getoDictionary_TypeIssuanceFON() {
        return oDictionary_TypeIssuanceFON;
    }

    public void setoDictionary_TypeIssuanceFON(Dictionary oDictionary_TypeIssuanceFON) {
        this.oDictionary_TypeIssuanceFON = oDictionary_TypeIssuanceFON;
    }

    public Dictionary getoDictionary_TypePlacementFON() {
        return oDictionary_TypePlacementFON;
    }

    public void setoDictionary_TypePlacementFON(Dictionary oDictionary_TypePlacementFON) {
        this.oDictionary_TypePlacementFON = oDictionary_TypePlacementFON;
    }

    public Long getnPaidStateFee() {
        return nPaidStateFee;
    }

    public void setnPaidStateFee(Long nPaidStateFee) {
        this.nPaidStateFee = nPaidStateFee;
    }

    public String getsInvestmentDirection() {
        return sInvestmentDirection;
    }

    public void setsInvestmentDirection(String sInvestmentDirection) {
        this.sInvestmentDirection = sInvestmentDirection;
    }

    public SubjectOrgan getoSubjectOrgan_RealEstateDeveloper() {
        return oSubjectOrgan_RealEstateDeveloper;
    }

    public void setoSubjectOrgan_RealEstateDeveloper(SubjectOrgan oSubjectOrgan_RealEstateDeveloper) {
        this.oSubjectOrgan_RealEstateDeveloper = oSubjectOrgan_RealEstateDeveloper;
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

    public DocumentStatutory getoDocumentStatutory_Certificate() {
        return oDocumentStatutory_Certificate;
    }

    public void setoDocumentStatutory_Certificate(DocumentStatutory oDocumentStatutory_Certificate) {
        this.oDocumentStatutory_Certificate = oDocumentStatutory_Certificate;
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

    public ActionEvent getoActionEvent_Change() {
        return oActionEvent_Change;
    }

    public void setoActionEvent_Change(ActionEvent oActionEvent_Change) {
        this.oActionEvent_Change = oActionEvent_Change;
    }

    public SubjectOrgan getoSubjectOrgan_Depositary() {
        return oSubjectOrgan_Depositary;
    }

    public void setoSubjectOrgan_Depositary(SubjectOrgan oSubjectOrgan_Depositary) {
        this.oSubjectOrgan_Depositary = oSubjectOrgan_Depositary;
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
