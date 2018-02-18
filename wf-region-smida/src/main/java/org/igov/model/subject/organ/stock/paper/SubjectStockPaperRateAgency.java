package org.igov.model.subject.organ.stock.paper;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.igov.model.core.AbstractEntity;
import org.igov.model.dictionary.Dictionary;
import org.igov.model.subject.SubjectContact;
import org.igov.model.subject.SubjectHuman;
import org.igov.model.subject.SubjectOperatorBank;
import org.igov.model.subject.organ.SubjectOrgan;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class SubjectStockPaperRateAgency extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oSubjectOrgan_RateAgency")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_RateAgency")
    private SubjectOrgan oSubjectOrgan_RateAgency;

    @JsonProperty(value = "oDictionary_KVED")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_KVED")
    private Dictionary oDictionary_KVED;

    @JsonProperty(value = "oSubjectOperatorBank_FON")
    @ManyToOne(targetEntity = SubjectOperatorBank.class)
    @JoinColumn(name = "nID_SubjectOperatorBank_FON")
    private SubjectOperatorBank oSubjectOperatorBank_FON;

    @JsonProperty(value = "sInfoParticipation")
    @Column
    private String sInfoParticipation;
    
    @JsonProperty(value = "sOfficials")
    @Column
    private String sOfficials;
    
    @JsonProperty(value = "sOwners")
    @Column
    private String sOwners;

    @JsonProperty(value = "oSubjectHuman_RateAgencyHead")
    @ManyToOne(targetEntity = SubjectHuman.class)
    @JoinColumn(name = "nID_SubjectHuman_RateAgencyHead")
    private SubjectHuman oSubjectHuman_RateAgencyHead;
    
    @JsonProperty(value = "oSubjectHuman_RateAgencyAccountant")
    @ManyToOne(targetEntity = SubjectHuman.class)
    @JoinColumn(name = "nID_SubjectHuman_RateAgencyAccountant")
    private SubjectHuman oSubjectHuman_RateAgencyAccountant;

    @JsonProperty(value = "nCountEmployee")
    @Column
    private Long nCountEmployee;
    
    @JsonProperty(value = "bLawEnforcement")
    @Column
    private boolean bLawEnforcement;
    
    @JsonProperty(value = "sNumberDocument")
    @Column
    private String sNumberDocument;
    
    @JsonProperty(value = "sBusinessEntities")
    @Column
    private String sBusinessEntities;

    public SubjectOrgan getoSubjectOrgan_RateAgency() {
        return oSubjectOrgan_RateAgency;
    }

    public void setoSubjectOrgan_RateAgency(SubjectOrgan oSubjectOrgan_RateAgency) {
        this.oSubjectOrgan_RateAgency = oSubjectOrgan_RateAgency;
    }

    public Dictionary getoDictionary_KVED() {
        return oDictionary_KVED;
    }

    public void setoDictionary_KVED(Dictionary oDictionary_KVED) {
        this.oDictionary_KVED = oDictionary_KVED;
    }

    public SubjectOperatorBank getoSubjectOperatorBank_FON() {
        return oSubjectOperatorBank_FON;
    }

    public void setoSubjectOperatorBank_FON(SubjectOperatorBank oSubjectOperatorBank_FON) {
        this.oSubjectOperatorBank_FON = oSubjectOperatorBank_FON;
    }

    public String getsInfoParticipation() {
        return sInfoParticipation;
    }

    public void setsInfoParticipation(String sInfoParticipation) {
        this.sInfoParticipation = sInfoParticipation;
    }

    public String getsOfficials() {
        return sOfficials;
    }

    public void setsOfficials(String sOfficials) {
        this.sOfficials = sOfficials;
    }

    public String getsOwners() {
        return sOwners;
    }

    public void setsOwners(String sOwners) {
        this.sOwners = sOwners;
    }

    public SubjectHuman getoSubjectHuman_RateAgencyHead() {
        return oSubjectHuman_RateAgencyHead;
    }

    public void setoSubjectHuman_RateAgencyHead(SubjectHuman oSubjectHuman_RateAgencyHead) {
        this.oSubjectHuman_RateAgencyHead = oSubjectHuman_RateAgencyHead;
    }

    public SubjectHuman getoSubjectHuman_RateAgencyAccountant() {
        return oSubjectHuman_RateAgencyAccountant;
    }

    public void setoSubjectHuman_RateAgencyAccountant(SubjectHuman oSubjectHuman_RateAgencyAccountant) {
        this.oSubjectHuman_RateAgencyAccountant = oSubjectHuman_RateAgencyAccountant;
    }

    public Long getnCountEmployee() {
        return nCountEmployee;
    }

    public void setnCountEmployee(Long nCountEmployee) {
        this.nCountEmployee = nCountEmployee;
    }

    public boolean isbLawEnforcement() {
        return bLawEnforcement;
    }

    public void setbLawEnforcement(boolean bLawEnforcement) {
        this.bLawEnforcement = bLawEnforcement;
    }

    public String getsNumberDocument() {
        return sNumberDocument;
    }

    public void setsNumberDocument(String sNumberDocument) {
        this.sNumberDocument = sNumberDocument;
    }

    public String getsBusinessEntities() {
        return sBusinessEntities;
    }

    public void setsBusinessEntities(String sBusinessEntities) {
        this.sBusinessEntities = sBusinessEntities;
    }
    
    
    
}
