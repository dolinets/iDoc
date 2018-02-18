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
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class ObjectStockPaperInvestmentCertificate extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "sSeries")
    @Column
    private String sSeries;
    
    @JsonProperty(value = "oActionEvent_Issue")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_Issue")
    private ActionEvent oActionEvent_Issue;

    @JsonProperty(value = "oDocumentStatutory_Certificate")
    @ManyToOne(targetEntity = DocumentStatutory.class)
    @JoinColumn(name = "nID_DocumentStatutory_Certificate")
    private DocumentStatutory oDocumentStatutory_Certificate;

    @JsonProperty(value = "oDictionary_TypeIssuanceInvestmentCertificate")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_TypeIssuanceInvestmentCertificate")
    private Dictionary oDictionary_TypeIssuanceInvestmentCertificate; 
    
    @JsonProperty(value = "oDictionary_FormExistenceInvestmentCertificate")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_FormExistenceInvestmentCertificate")
    private Dictionary oDictionary_FormExistenceInvestmentCertificate; 
    
    @JsonProperty(value = "nQuantity")
    @Column
    private Long nQuantity;
    
    @JsonProperty(value = "sDateEndIPO")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateEndIPO;

    @JsonProperty(value = "nPaidStateFee")
    @Column
    private Long nPaidStateFee;

    @JsonProperty(value = "bFictitious")
    @Column
    private boolean bFictitious;
    
    @JsonProperty(value = "oActionEvent_Fictitious")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_Fictitious")
    private ActionEvent oActionEvent_Fictitious;

    public String getsSeries() {
        return sSeries;
    }

    public void setsSeries(String sSeries) {
        this.sSeries = sSeries;
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

    public Dictionary getoDictionary_TypeIssuanceInvestmentCertificate() {
        return oDictionary_TypeIssuanceInvestmentCertificate;
    }

    public void setoDictionary_TypeIssuanceInvestmentCertificate(Dictionary oDictionary_TypeIssuanceInvestmentCertificate) {
        this.oDictionary_TypeIssuanceInvestmentCertificate = oDictionary_TypeIssuanceInvestmentCertificate;
    }

    public Dictionary getoDictionary_FormExistenceInvestmentCertificate() {
        return oDictionary_FormExistenceInvestmentCertificate;
    }

    public void setoDictionary_FormExistenceInvestmentCertificate(Dictionary oDictionary_FormExistenceInvestmentCertificate) {
        this.oDictionary_FormExistenceInvestmentCertificate = oDictionary_FormExistenceInvestmentCertificate;
    }

    public Long getnQuantity() {
        return nQuantity;
    }

    public void setnQuantity(Long nQuantity) {
        this.nQuantity = nQuantity;
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
    
    
}
