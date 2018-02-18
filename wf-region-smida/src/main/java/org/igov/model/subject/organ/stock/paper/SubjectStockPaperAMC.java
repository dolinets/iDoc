package org.igov.model.subject.organ.stock.paper;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import org.igov.model.core.AbstractEntity;
import org.igov.model.document.DocumentStatutory;
import org.igov.model.object.place.Country;
import org.igov.model.object.place.Place;
import org.igov.model.subject.SubjectContact;
import org.igov.model.subject.SubjectHuman;
import org.igov.model.subject.organ.SubjectOrgan;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class SubjectStockPaperAMC extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oSubjectOrgan_AMC")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_AMC")
    private SubjectOrgan oSubjectOrgan_AMC;

    @JsonProperty(value = "sKindActivity")
    @Column
    private String sKindActivity;
    
    @JsonProperty(value = "oDocumentStatutory_Licence")
    @ManyToOne(targetEntity = DocumentStatutory.class)
    @JoinColumn(name = "nID_DocumentStatutory_Licence")
    private DocumentStatutory oDocumentStatutory_Licence;

    @JsonProperty(value = "oSubjectStockPaperInvestmentInstitutions")
    @ManyToOne(targetEntity = SubjectStockPaperInvestmentInstitutions.class)
    @JoinColumn(name = "nID_SubjectStockPaperInvestmentInstitutions")
    private SubjectStockPaperInvestmentInstitutions oSubjectStockPaperInvestmentInstitutions;

    @JsonProperty(value = "oSubjectContact_KIF")
    @ManyToOne(targetEntity = SubjectContact.class)
    @JoinColumn(name = "nID_SubjectContact_KIF")
    private SubjectContact oSubjectContact_KIF;

    @JsonProperty(value = "oPlace_AMC")
    @ManyToOne(targetEntity = Place.class)
    @JoinColumn(name = "nID_Place_AMC")
    private Place oPlace_AMC;

    @JsonProperty(value = "oSubjectHuman_AMCHead")
    @ManyToOne(targetEntity = SubjectHuman.class)
    @JoinColumn(name = "nID_SubjectHuman_AMCHead")
    private SubjectHuman oSubjectHuman_AMCHead;

    @JsonProperty(value = "nSizeAuthorizedFund")
    @Column
    private Long nSizeAuthorizedFund;

    @JsonProperty(value = "sFirstStatutoryFund")
    @Column
    private String sFirstStatutoryFund;

    @JsonProperty(value = "sLastStatutoryFund")
    @Column
    private String sLastStatutoryFund;

    @JsonProperty(value = "sNameFounderAMC")
    @Column
    private String sNameFounderAMC;

    @JsonProperty(value = "bResident")
    @Column
    private boolean bResident;

    @JsonProperty(value = "oCountry")
    @ManyToOne(targetEntity = Country.class)
    @JoinColumn(name = "nID_Country")
    private Country oCountry;

    @JsonProperty(value = "sPartAuthorizedCapital")
    @Column
    private String sPartAuthorizedCapital;

    @JsonProperty(value = "bISIflag")
    @Column
    private boolean bISIflag;
    
    @JsonProperty(value = "bNPFflag")
    @Column
    private boolean bNPFflag;

    @JsonProperty(value = "bIIflag")
    @Column
    private boolean bIIflag;

    @JsonProperty(value = "bAPFflag")
    @Column
    private boolean bAPFflag;

    @JsonProperty(value = "bBranches")
    @Column
    private boolean bBranches;
    
    @JsonProperty(value = "aSubjectStockPaperInvestmentInstitutions")
    @OneToMany(cascade = CascadeType.ALL)
    private List<SubjectStockPaperInvestmentInstitutions> aSubjectStockPaperInvestmentInstitutions;

    public SubjectOrgan getoSubjectOrgan_AMC() {
        return oSubjectOrgan_AMC;
    }

    public void setoSubjectOrgan_AMC(SubjectOrgan oSubjectOrgan_AMC) {
        this.oSubjectOrgan_AMC = oSubjectOrgan_AMC;
    }

    public String getsKindActivity() {
        return sKindActivity;
    }

    public void setsKindActivity(String sKindActivity) {
        this.sKindActivity = sKindActivity;
    }

    public DocumentStatutory getoDocumentStatutory_Licence() {
        return oDocumentStatutory_Licence;
    }

    public void setoDocumentStatutory_Licence(DocumentStatutory oDocumentStatutory_Licence) {
        this.oDocumentStatutory_Licence = oDocumentStatutory_Licence;
    }

    public SubjectStockPaperInvestmentInstitutions getoSubjectStockPaperInvestmentInstitutions() {
        return oSubjectStockPaperInvestmentInstitutions;
    }

    public void setoSubjectStockPaperInvestmentInstitutions(SubjectStockPaperInvestmentInstitutions oSubjectStockPaperInvestmentInstitutions) {
        this.oSubjectStockPaperInvestmentInstitutions = oSubjectStockPaperInvestmentInstitutions;
    }

    public SubjectContact getoSubjectContact_KIF() {
        return oSubjectContact_KIF;
    }

    public void setoSubjectContact_KIF(SubjectContact oSubjectContact_KIF) {
        this.oSubjectContact_KIF = oSubjectContact_KIF;
    }

    public Place getoPlace_AMC() {
        return oPlace_AMC;
    }

    public void setoPlace_AMC(Place oPlace_AMC) {
        this.oPlace_AMC = oPlace_AMC;
    }

    public SubjectHuman getoSubjectHuman_AMCHead() {
        return oSubjectHuman_AMCHead;
    }

    public void setoSubjectHuman_AMCHead(SubjectHuman oSubjectHuman_AMCHead) {
        this.oSubjectHuman_AMCHead = oSubjectHuman_AMCHead;
    }

    public Long getnSizeAuthorizedFund() {
        return nSizeAuthorizedFund;
    }

    public void setnSizeAuthorizedFund(Long nSizeAuthorizedFund) {
        this.nSizeAuthorizedFund = nSizeAuthorizedFund;
    }

    public String getsFirstStatutoryFund() {
        return sFirstStatutoryFund;
    }

    public void setsFirstStatutoryFund(String sFirstStatutoryFund) {
        this.sFirstStatutoryFund = sFirstStatutoryFund;
    }

    public String getsLastStatutoryFund() {
        return sLastStatutoryFund;
    }

    public void setsLastStatutoryFund(String sLastStatutoryFund) {
        this.sLastStatutoryFund = sLastStatutoryFund;
    }

    public String getsNameFounderAMC() {
        return sNameFounderAMC;
    }

    public void setsNameFounderAMC(String sNameFounderAMC) {
        this.sNameFounderAMC = sNameFounderAMC;
    }

    public boolean isbResident() {
        return bResident;
    }

    public void setbResident(boolean bResident) {
        this.bResident = bResident;
    }

    public Country getoCountry() {
        return oCountry;
    }

    public void setoCountry(Country oCountry) {
        this.oCountry = oCountry;
    }

    public String getsPartAuthorizedCapital() {
        return sPartAuthorizedCapital;
    }

    public void setsPartAuthorizedCapital(String sPartAuthorizedCapital) {
        this.sPartAuthorizedCapital = sPartAuthorizedCapital;
    }

    public boolean isbISIflag() {
        return bISIflag;
    }

    public void setbISIflag(boolean bISIflag) {
        this.bISIflag = bISIflag;
    }

    public boolean isbNPFflag() {
        return bNPFflag;
    }

    public void setbNPFflag(boolean bNPFflag) {
        this.bNPFflag = bNPFflag;
    }

    public boolean isbIIflag() {
        return bIIflag;
    }

    public void setbIIflag(boolean bIIflag) {
        this.bIIflag = bIIflag;
    }

    public boolean isbAPFflag() {
        return bAPFflag;
    }

    public void setbAPFflag(boolean bAPFflag) {
        this.bAPFflag = bAPFflag;
    }

    public boolean isbBranches() {
        return bBranches;
    }

    public void setbBranches(boolean bBranches) {
        this.bBranches = bBranches;
    }

    public List<SubjectStockPaperInvestmentInstitutions> getaSubjectStockPaperInvestmentInstitutions() {
        return aSubjectStockPaperInvestmentInstitutions;
    }

    public void setaSubjectStockPaperInvestmentInstitutions(List<SubjectStockPaperInvestmentInstitutions> aSubjectStockPaperInvestmentInstitutions) {
        this.aSubjectStockPaperInvestmentInstitutions = aSubjectStockPaperInvestmentInstitutions;
    }

    
}
