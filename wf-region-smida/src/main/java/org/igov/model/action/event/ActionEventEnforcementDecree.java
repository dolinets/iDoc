package org.igov.model.action.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.igov.model.core.AbstractEntity;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class ActionEventEnforcementDecree extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oActionEvent")
    @OneToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent")
    private ActionEvent oActionEvent;

    @JsonProperty(value = "sTypeOffender")
    @Column
    private String sTypeOffender;
    
    @JsonProperty(value = "sFile")
    @Column
    private String sFile;
    
    @JsonProperty(value = "sAgents")
    @Column
    private String sAgents;
    
    @JsonProperty(value = "bConfidentiality")
    @Column
    private boolean bConfidentiality;
    
    @JsonProperty(value = "sTypeLawEnforcement")
    @Column
    private String sTypeLawEnforcement;
    
    @JsonProperty(value = "sDecision")
    @Column
    private String sDecision;
    
    @JsonProperty(value = "nSum")
    @Column
    private Long nSum;
    
    @JsonProperty(value = "sVersionPublication")
    @Column
    private String sVersionPublication;
    
    @JsonProperty(value = "sPaid")
    @Column
    private String sPaid;
    
    @JsonProperty(value = "sPayment")
    @Column
    private String sPayment;
    
    @JsonProperty(value = "sTypeAppeal")
    @Column
    private String sTypeAppeal;
    
    @JsonProperty(value = "sContentAppeal")
    @Column
    private String sContentAppeal;
    
    @JsonProperty(value = "bPosted")
    @Column
    private boolean bPosted;
    
    @JsonProperty(value = "bCanceled")
    @Column
    private boolean bCanceled;
    
    @JsonProperty(value = "bLegalizationProceeds")
    @Column
    private boolean bLegalizationProceeds;

    public ActionEvent getoActionEvent() {
        return oActionEvent;
    }

    public void setoActionEvent(ActionEvent oActionEvent) {
        this.oActionEvent = oActionEvent;
    }

    public String getsTypeOffender() {
        return sTypeOffender;
    }

    public void setsTypeOffender(String sTypeOffender) {
        this.sTypeOffender = sTypeOffender;
    }

    public String getsFile() {
        return sFile;
    }

    public void setsFile(String sFile) {
        this.sFile = sFile;
    }

    public String getsAgents() {
        return sAgents;
    }

    public void setsAgents(String sAgents) {
        this.sAgents = sAgents;
    }

    public boolean isbConfidentiality() {
        return bConfidentiality;
    }

    public void setbConfidentiality(boolean bConfidentiality) {
        this.bConfidentiality = bConfidentiality;
    }

    public String getsTypeLawEnforcement() {
        return sTypeLawEnforcement;
    }

    public void setsTypeLawEnforcement(String sTypeLawEnforcement) {
        this.sTypeLawEnforcement = sTypeLawEnforcement;
    }

    public String getsDecision() {
        return sDecision;
    }

    public void setsDecision(String sDecision) {
        this.sDecision = sDecision;
    }

    public Long getnSum() {
        return nSum;
    }

    public void setnSum(Long nSum) {
        this.nSum = nSum;
    }

    public String getsVersionPublication() {
        return sVersionPublication;
    }

    public void setsVersionPublication(String sVersionPublication) {
        this.sVersionPublication = sVersionPublication;
    }

    public String getsPaid() {
        return sPaid;
    }

    public void setsPaid(String sPaid) {
        this.sPaid = sPaid;
    }

    public String getsPayment() {
        return sPayment;
    }

    public void setsPayment(String sPayment) {
        this.sPayment = sPayment;
    }

    public String getsTypeAppeal() {
        return sTypeAppeal;
    }

    public void setsTypeAppeal(String sTypeAppeal) {
        this.sTypeAppeal = sTypeAppeal;
    }

    public String getsContentAppeal() {
        return sContentAppeal;
    }

    public void setsContentAppeal(String sContentAppeal) {
        this.sContentAppeal = sContentAppeal;
    }

    public boolean isbPosted() {
        return bPosted;
    }

    public void setbPosted(boolean bPosted) {
        this.bPosted = bPosted;
    }

    public boolean isbCanceled() {
        return bCanceled;
    }

    public void setbCanceled(boolean bCanceled) {
        this.bCanceled = bCanceled;
    }

    public boolean isbLegalizationProceeds() {
        return bLegalizationProceeds;
    }

    public void setbLegalizationProceeds(boolean bLegalizationProceeds) {
        this.bLegalizationProceeds = bLegalizationProceeds;
    }
    
    
}
