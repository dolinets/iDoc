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
public class ObjectStockPaperShare extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oSubjectOrgan_Emitente")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_Emitente")
    private SubjectOrgan oSubjectOrgan_Emitente;

    @JsonProperty(value = "sSeries")
    @Column
    private String sSeries;

    @JsonProperty(value = "nSizeCharterCapitalIssuance")
    @Column
    private Long nSizeCharterCapitalIssuance;

    @JsonProperty(value = "nCommonDenomination")
    @Column
    private Long nCommonDenomination;

    @JsonProperty(value = "nQuantity")
    @Column
    private Long nQuantity;

    @JsonProperty(value = "nDenomination")
    @Column
    private Long nDenomination;

    @JsonProperty(value = "oDictionary_FormExistenceShare")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_FormExistenceShare")
    private Dictionary oDictionary_FormExistenceShare;

    @JsonProperty(value = "oDictionary_TypeShare")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_TypeShare")
    private Dictionary oDictionary_TypeShare;
    
    @JsonProperty(value = "oDictionary_ReasonRegisteringIssuanceShare")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_ReasonRegisteringIssuanceShare")
    private Dictionary oDictionary_ReasonRegisteringIssuanceShare;

    @JsonProperty(value = "bReorganization")
    @Column
    private boolean bReorganization;

    @JsonProperty(value = "oSubjectOrgan_EmitenteReorganization")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_EmitenteReorganization")
    private SubjectOrgan oSubjectOrgan_EmitenteReorganization;

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

    @JsonProperty(value = "oActionEvent_Issue")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_Issue")
    private ActionEvent oActionEvent_Issue;

    @JsonProperty(value = "oDocumentStatutory_Certificate")
    @ManyToOne(targetEntity = DocumentStatutory.class)
    @JoinColumn(name = "nID_DocumentStatutory_Certificate")
    private DocumentStatutory oDocumentStatutory_Certificate;

    @JsonProperty(value = "bFictitious")
    @Column(nullable = true)
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

    @JsonProperty(value = "nQuantitySharesIPO")
    @Column
    private Long nQuantitySharesIPO;

    @JsonProperty(value = "oActionEvent_Terminate")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_Terminate")
    private ActionEvent oActionEvent_Terminate;

    @JsonProperty("bShareKIF")
    @Column
    private boolean bShareKIF;

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

    public Long getnSizeCharterCapitalIssuance() {
        return nSizeCharterCapitalIssuance;
    }

    public void setnSizeCharterCapitalIssuance(Long nSizeCharterCapitalIssuance) {
        this.nSizeCharterCapitalIssuance = nSizeCharterCapitalIssuance;
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

    public Dictionary getoDictionary_FormExistenceShare() {
        return oDictionary_FormExistenceShare;
    }

    public void setoDictionary_FormExistenceShare(Dictionary oDictionary_FormExistenceShare) {
        this.oDictionary_FormExistenceShare = oDictionary_FormExistenceShare;
    }

    public Dictionary getoDictionary_TypeShare() {
        return oDictionary_TypeShare;
    }

    public void setoDictionary_TypeShare(Dictionary oDictionary_TypeShare) {
        this.oDictionary_TypeShare = oDictionary_TypeShare;
    }

    public Dictionary getoDictionary_ReasonRegisteringIssuanceShare() {
        return oDictionary_ReasonRegisteringIssuanceShare;
    }

    public void setoDictionary_ReasonRegisteringIssuanceShare(Dictionary oDictionary_ReasonRegisteringIssuanceShare) {
        this.oDictionary_ReasonRegisteringIssuanceShare = oDictionary_ReasonRegisteringIssuanceShare;
    }

    public boolean isbReorganization() {
        return bReorganization;
    }

    public void setbReorganization(boolean bReorganization) {
        this.bReorganization = bReorganization;
    }

    public SubjectOrgan getoSubjectOrgan_EmitenteReorganization() {
        return oSubjectOrgan_EmitenteReorganization;
    }

    public void setoSubjectOrgan_EmitenteReorganization(SubjectOrgan oSubjectOrgan_EmitenteReorganization) {
        this.oSubjectOrgan_EmitenteReorganization = oSubjectOrgan_EmitenteReorganization;
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

    public Long getnQuantitySharesIPO() {
        return nQuantitySharesIPO;
    }

    public void setnQuantitySharesIPO(Long nQuantitySharesIPO) {
        this.nQuantitySharesIPO = nQuantitySharesIPO;
    }

    public ActionEvent getoActionEvent_Terminate() {
        return oActionEvent_Terminate;
    }

    public void setoActionEvent_Terminate(ActionEvent oActionEvent_Terminate) {
        this.oActionEvent_Terminate = oActionEvent_Terminate;
    }

    public boolean isbShareKIF() {
        return bShareKIF;
    }

    public void setbShareKIF(boolean bShareKIF) {
        this.bShareKIF = bShareKIF;
    }
}
