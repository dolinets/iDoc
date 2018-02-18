package org.igov.model.subject.organ.stock.paper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.Type;
import org.igov.model.core.AbstractEntity;
import org.igov.model.dictionary.Dictionary;
import org.igov.model.document.TermType;
import org.igov.model.object.ObjectStockPaperInvestmentCertificate;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class SubjectStockPaperInvestmentInstitutions extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "sName")
    @Column
    private String sName;

    @JsonProperty(value = "sNameFull")
    @Column
    private String sNameFull;

    @JsonProperty(value = "oSubjectStockPaperAMC")
    @ManyToOne(targetEntity = SubjectStockPaperAMC.class)
    @JoinColumn(name = "nID_SubjectStockPaperAMC")
    private SubjectStockPaperAMC oSubjectStockPaperAMC;

    @JsonProperty(value = "oDictionary_ISIFlag")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_ISIFlag")
    private Dictionary oDictionary_ISIFlag;
    
    @JsonProperty(value = "oDictionary_ISITerm")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_ISITerm")
    private Dictionary oDictionary_ISITerm;
    
    @JsonProperty(value = "oDictionary_ISIForm")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_ISIForm")
    private Dictionary oDictionary_ISIForm;
    
    @JsonProperty(value = "oDictionary_ISIType")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_ISIType")
    private Dictionary oDictionary_ISIType;

    @JsonProperty(value = "oDictionary_SpecializedFundsType")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_SpecializedFundsType")
    private Dictionary oDictionary_SpecializedFundsType;
    
    @JsonProperty(value = "oDictionary_QualifyingFundsType")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_QualifyingFundsType")
    private Dictionary oDictionary_QualifyingFundsType;

    @JsonProperty(value = "sDateReglament")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateReglament;

    @JsonProperty(value = "oTermType")
    @ManyToOne(targetEntity = TermType.class)
    @JoinColumn(name = "nID_TermType")
    private TermType oTermType;

    @JsonProperty(value = "nTermCount")
    @Column
    private Long nTermCount;

    @JsonProperty(value = "nEDRISI")
    @Column
    private Long nEDRISI;

    @JsonProperty(value = "sDateEDRISI")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateEDRISI;

    @JsonProperty(value = "sNumberDocEDRISI")
    @Column
    private String sNumberDocEDRISI;

    @JsonProperty(value = "sDateTemporaryDocEDRISI")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateTemporaryDocEDRISI;

    @JsonProperty(value = "sNumberTemporaryDocEDRISI")
    @Column
    private String sNumberTemporaryDocEDRISI;
    
    @JsonProperty(value = "oDictionary_StageExemptionEDRISI")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_StageExemptionEDRISI")
    private Dictionary oDictionary_StageExemptionEDRISI;

    @JsonProperty(value = "sDateExemptionEDRISI")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateExemptionEDRISI;

    @JsonProperty(value = "sNumberExemptionEDRISI")
    @Column
    private String sNumberExemptionEDRISI;

    @JsonProperty(value = "sReasonExemptionEDRISI")
    @Column
    private String sReasonExemptionEDRISI;

    @JsonProperty(value = "bChangeDocEDRISI")
    @Column
    private boolean bChangeDocEDRISI;

    @JsonProperty(value = "sDateNewDocEDRISI")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateNewDocEDRISI;

    @JsonProperty(value = "sNumberNewDocEDRISI")
    @Column
    private String sNumberNewDocEDRISI;

    @JsonProperty(value = "bAchievementStandards")
    @Column
    private boolean bAchievementStandards;

    @JsonProperty(value = "sDateAchievementStandards")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateAchievementStandards;

    @JsonProperty(value = "sDateClose")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateClose;

    @JsonProperty(value = "sDateGetMessage")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateGetMessage;

    @JsonProperty(value = "oObjectStockPaperInvestmentCertificatee")
    @ManyToOne(targetEntity = ObjectStockPaperInvestmentCertificate.class)
    @JoinColumn(name = "nID_ObjectStockPaperInvestmentCertificate")
    private ObjectStockPaperInvestmentCertificate oObjectStockPaperInvestmentCertificate;

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getsNameFull() {
        return sNameFull;
    }

    public void setsNameFull(String sNameFull) {
        this.sNameFull = sNameFull;
    }

    public SubjectStockPaperAMC getoSubjectStockPaperAMC() {
        return oSubjectStockPaperAMC;
    }

    public void setoSubjectStockPaperAMC(SubjectStockPaperAMC oSubjectStockPaperAMC) {
        this.oSubjectStockPaperAMC = oSubjectStockPaperAMC;
    }

    public Dictionary getoDictionary_ISIFlag() {
        return oDictionary_ISIFlag;
    }

    public void setoDictionary_ISIFlag(Dictionary oDictionary_ISIFlag) {
        this.oDictionary_ISIFlag = oDictionary_ISIFlag;
    }

    public Dictionary getoDictionary_ISITerm() {
        return oDictionary_ISITerm;
    }

    public void setoDictionary_ISITerm(Dictionary oDictionary_ISITerm) {
        this.oDictionary_ISITerm = oDictionary_ISITerm;
    }

    public Dictionary getoDictionary_ISIForm() {
        return oDictionary_ISIForm;
    }

    public void setoDictionary_ISIForm(Dictionary oDictionary_ISIForm) {
        this.oDictionary_ISIForm = oDictionary_ISIForm;
    }

    public Dictionary getoDictionary_ISIType() {
        return oDictionary_ISIType;
    }

    public void setoDictionary_ISIType(Dictionary oDictionary_ISIType) {
        this.oDictionary_ISIType = oDictionary_ISIType;
    }

    public Dictionary getoDictionary_SpecializedFundsType() {
        return oDictionary_SpecializedFundsType;
    }

    public void setoDictionary_SpecializedFundsType(Dictionary oDictionary_SpecializedFundsType) {
        this.oDictionary_SpecializedFundsType = oDictionary_SpecializedFundsType;
    }

    public Dictionary getoDictionary_QualifyingFundsType() {
        return oDictionary_QualifyingFundsType;
    }

    public void setoDictionary_QualifyingFundsType(Dictionary oDictionary_QualifyingFundsType) {
        this.oDictionary_QualifyingFundsType = oDictionary_QualifyingFundsType;
    }

    public DateTime getsDateReglament() {
        return sDateReglament;
    }

    public void setsDateReglament(DateTime sDateReglament) {
        this.sDateReglament = sDateReglament;
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

    public Long getnEDRISI() {
        return nEDRISI;
    }

    public void setnEDRISI(Long nEDRISI) {
        this.nEDRISI = nEDRISI;
    }

    public DateTime getsDateEDRISI() {
        return sDateEDRISI;
    }

    public void setsDateEDRISI(DateTime sDateEDRISI) {
        this.sDateEDRISI = sDateEDRISI;
    }

    public String getsNumberDocEDRISI() {
        return sNumberDocEDRISI;
    }

    public void setsNumberDocEDRISI(String sNumberDocEDRISI) {
        this.sNumberDocEDRISI = sNumberDocEDRISI;
    }

    public DateTime getsDateTemporaryDocEDRISI() {
        return sDateTemporaryDocEDRISI;
    }

    public void setsDateTemporaryDocEDRISI(DateTime sDateTemporaryDocEDRISI) {
        this.sDateTemporaryDocEDRISI = sDateTemporaryDocEDRISI;
    }

    public String getsNumberTemporaryDocEDRISI() {
        return sNumberTemporaryDocEDRISI;
    }

    public void setsNumberTemporaryDocEDRISI(String sNumberTemporaryDocEDRISI) {
        this.sNumberTemporaryDocEDRISI = sNumberTemporaryDocEDRISI;
    }

    public Dictionary getoDictionary_StageExemptionEDRISI() {
        return oDictionary_StageExemptionEDRISI;
    }

    public void setoDictionary_StageExemptionEDRISI(Dictionary oDictionary_StageExemptionEDRISI) {
        this.oDictionary_StageExemptionEDRISI = oDictionary_StageExemptionEDRISI;
    }

    public DateTime getsDateExemptionEDRISI() {
        return sDateExemptionEDRISI;
    }

    public void setsDateExemptionEDRISI(DateTime sDateExemptionEDRISI) {
        this.sDateExemptionEDRISI = sDateExemptionEDRISI;
    }

    public String getsNumberExemptionEDRISI() {
        return sNumberExemptionEDRISI;
    }

    public void setsNumberExemptionEDRISI(String sNumberExemptionEDRISI) {
        this.sNumberExemptionEDRISI = sNumberExemptionEDRISI;
    }

    public String getsReasonExemptionEDRISI() {
        return sReasonExemptionEDRISI;
    }

    public void setsReasonExemptionEDRISI(String sReasonExemptionEDRISI) {
        this.sReasonExemptionEDRISI = sReasonExemptionEDRISI;
    }

    public boolean isbChangeDocEDRISI() {
        return bChangeDocEDRISI;
    }

    public void setbChangeDocEDRISI(boolean bChangeDocEDRISI) {
        this.bChangeDocEDRISI = bChangeDocEDRISI;
    }

    public DateTime getsDateNewDocEDRISI() {
        return sDateNewDocEDRISI;
    }

    public void setsDateNewDocEDRISI(DateTime sDateNewDocEDRISI) {
        this.sDateNewDocEDRISI = sDateNewDocEDRISI;
    }

    public String getsNumberNewDocEDRISI() {
        return sNumberNewDocEDRISI;
    }

    public void setsNumberNewDocEDRISI(String sNumberNewDocEDRISI) {
        this.sNumberNewDocEDRISI = sNumberNewDocEDRISI;
    }

    public boolean isbAchievementStandards() {
        return bAchievementStandards;
    }

    public void setbAchievementStandards(boolean bAchievementStandards) {
        this.bAchievementStandards = bAchievementStandards;
    }

    public DateTime getsDateAchievementStandards() {
        return sDateAchievementStandards;
    }

    public void setsDateAchievementStandards(DateTime sDateAchievementStandards) {
        this.sDateAchievementStandards = sDateAchievementStandards;
    }

    public DateTime getsDateClose() {
        return sDateClose;
    }

    public void setsDateClose(DateTime sDateClose) {
        this.sDateClose = sDateClose;
    }

    public DateTime getsDateGetMessage() {
        return sDateGetMessage;
    }

    public void setsDateGetMessage(DateTime sDateGetMessage) {
        this.sDateGetMessage = sDateGetMessage;
    }

    public ObjectStockPaperInvestmentCertificate getoObjectStockPaperInvestmentCertificate() {
        return oObjectStockPaperInvestmentCertificate;
    }

    public void setoObjectStockPaperInvestmentCertificate(ObjectStockPaperInvestmentCertificate oObjectStockPaperInvestmentCertificate) {
        this.oObjectStockPaperInvestmentCertificate = oObjectStockPaperInvestmentCertificate;
    }

    
}
