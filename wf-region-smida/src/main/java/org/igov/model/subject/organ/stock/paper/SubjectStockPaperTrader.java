package org.igov.model.subject.organ.stock.paper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import org.hibernate.annotations.Type;
import org.igov.model.action.event.ActionEventAudit;
import org.igov.model.core.AbstractEntity;
import org.igov.model.document.DocumentStatutory;
import org.igov.model.object.place.Country;
import org.igov.model.object.place.Place;
import org.igov.model.subject.SubjectContact;
import org.igov.model.subject.SubjectHuman;
import org.igov.model.subject.organ.SubjectOrgan;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class SubjectStockPaperTrader extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oSubjectOrgan_Trader")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_Trader")
    private SubjectOrgan oSubjectOrgan_Trader;

    @JsonProperty(value = "nSizeCharterCapitalSince")
    @Column
    private Long nSizeCharterCapitalSince;

    @JsonProperty(value = "nSizeCharterCapitalForeign")
    @Column
    private Long nSizeCharterCapitalForeign;

    @JsonProperty(value = "oCountry_Trader")
    @ManyToOne(targetEntity = Country.class)
    @JoinColumn(name = "nID_Country_Trader")
    private Country oCountry_Trader;

    @JsonProperty(value = "oSubjectOrgan_Auditor")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_Auditor")
    private SubjectOrgan oSubjectOrgan_Auditor;

    @JsonProperty(value = "oActionEventAudit")
    @ManyToOne(targetEntity = ActionEventAudit.class)
    @JoinColumn(name = "nID_ActionEventAudit")
    private ActionEventAudit oActionEventAudit;

    @JsonProperty(value = "bBrokerageLicense")
    @Column
    private boolean bBrokerageLicense;

    @JsonProperty(value = "bDealershipLicense")
    @Column
    private boolean bDealershipLicense;

    @JsonProperty(value = "bUnderwritingLicense")
    @Column
    private boolean bUnderwritingLicense ;

    @JsonProperty(value = "bManagmentLicense")
    @Column
    private boolean bManagmentLicense ;

    @JsonProperty(value = "oPlace_Trader")
    @ManyToOne(targetEntity = Place.class)
    @JoinColumn(name = "nID_Place_Trader")
    private Place oPlace_Trader;

    @JsonProperty(value = "bNonBankingFlag")
    @Column
    private boolean bNonBankingFlag;
    
    @JsonProperty(value = "bTOVFlag")
    @Column
    private boolean bTOVFlag;
    
    @JsonProperty(value = "bBankruptFlag")
    @Column
    private boolean bBankruptFlag;

    @JsonProperty(value = "sCurator")
    @Column
    private String sCurator;

    @JsonProperty(value = "nAmountRent")
    @Column
    private Long nAmountRent;

    @JsonProperty(value = "sDateRentTill")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateRentTill;

    @JsonProperty(value = "sRent27")
    @Column
    private String sRent27;
    
    @JsonProperty(value = "sBranches")
    @Column
    private String sBranches;

    @JsonProperty(value = "sFolder")
    @Column
    private String sFolder;

    @JsonProperty(value = "oSubjectHuman_TraderHead")
    @ManyToOne(targetEntity = SubjectHuman.class)
    @JoinColumn(name = "nID_SubjectHuman_TraderHead")
    private SubjectHuman oSubjectHuman_TraderHead;
    
    @JsonProperty(value = "oSubjectHuman_TraderAccountant")
    @ManyToOne(targetEntity = SubjectHuman.class)
    @JoinColumn(name = "nID_SubjectHuman_TraderAccountant")
    private SubjectHuman oSubjectHuman_TraderAccountant;

    public SubjectOrgan getoSubjectOrgan_Trader() {
        return oSubjectOrgan_Trader;
    }

    public void setoSubjectOrgan_Trader(SubjectOrgan oSubjectOrgan_Trader) {
        this.oSubjectOrgan_Trader = oSubjectOrgan_Trader;
    }

    public Long getnSizeCharterCapitalSince() {
        return nSizeCharterCapitalSince;
    }

    public void setnSizeCharterCapitalSince(Long nSizeCharterCapitalSince) {
        this.nSizeCharterCapitalSince = nSizeCharterCapitalSince;
    }

    public Long getnSizeCharterCapitalForeign() {
        return nSizeCharterCapitalForeign;
    }

    public void setnSizeCharterCapitalForeign(Long nSizeCharterCapitalForeign) {
        this.nSizeCharterCapitalForeign = nSizeCharterCapitalForeign;
    }

    public Country getoCountry_Trader() {
        return oCountry_Trader;
    }

    public void setoCountry_Trader(Country oCountry_Trader) {
        this.oCountry_Trader = oCountry_Trader;
    }

    public SubjectOrgan getoSubjectOrgan_Auditor() {
        return oSubjectOrgan_Auditor;
    }

    public void setoSubjectOrgan_Auditor(SubjectOrgan oSubjectOrgan_Auditor) {
        this.oSubjectOrgan_Auditor = oSubjectOrgan_Auditor;
    }

    public ActionEventAudit getoActionEventAudit() {
        return oActionEventAudit;
    }

    public void setoActionEventAudit(ActionEventAudit oActionEventAudit) {
        this.oActionEventAudit = oActionEventAudit;
    }

    public boolean isbBrokerageLicense() {
        return bBrokerageLicense;
    }

    public void setbBrokerageLicense(boolean bBrokerageLicense) {
        this.bBrokerageLicense = bBrokerageLicense;
    }

    public boolean isbDealershipLicense() {
        return bDealershipLicense;
    }

    public void setbDealershipLicense(boolean bDealershipLicense) {
        this.bDealershipLicense = bDealershipLicense;
    }

    public boolean isbUnderwritingLicense() {
        return bUnderwritingLicense;
    }

    public void setbUnderwritingLicense(boolean bUnderwritingLicense) {
        this.bUnderwritingLicense = bUnderwritingLicense;
    }

    public boolean isbManagmentLicense() {
        return bManagmentLicense;
    }

    public void setbManagmentLicense(boolean bManagmentLicense) {
        this.bManagmentLicense = bManagmentLicense;
    }

    public Place getoPlace_Trader() {
        return oPlace_Trader;
    }

    public void setoPlace_Trader(Place oPlace_Trader) {
        this.oPlace_Trader = oPlace_Trader;
    }

    public boolean isbNonBankingFlag() {
        return bNonBankingFlag;
    }

    public void setbNonBankingFlag(boolean bNonBankingFlag) {
        this.bNonBankingFlag = bNonBankingFlag;
    }

    public boolean isbTOVFlag() {
        return bTOVFlag;
    }

    public void setbTOVFlag(boolean bTOVFlag) {
        this.bTOVFlag = bTOVFlag;
    }

    public boolean isbBankruptFlag() {
        return bBankruptFlag;
    }

    public void setbBankruptFlag(boolean bBankruptFlag) {
        this.bBankruptFlag = bBankruptFlag;
    }

    public String getsCurator() {
        return sCurator;
    }

    public void setsCurator(String sCurator) {
        this.sCurator = sCurator;
    }

    public Long getnAmountRent() {
        return nAmountRent;
    }

    public void setnAmountRent(Long nAmountRent) {
        this.nAmountRent = nAmountRent;
    }

    public DateTime getsDateRentTill() {
        return sDateRentTill;
    }

    public void setsDateRentTill(DateTime sDateRentTill) {
        this.sDateRentTill = sDateRentTill;
    }

    public String getsRent27() {
        return sRent27;
    }

    public void setsRent27(String sRent27) {
        this.sRent27 = sRent27;
    }

    public String getsBranches() {
        return sBranches;
    }

    public void setsBranches(String sBranches) {
        this.sBranches = sBranches;
    }

    public String getsFolder() {
        return sFolder;
    }

    public void setsFolder(String sFolder) {
        this.sFolder = sFolder;
    }

    public SubjectHuman getoSubjectHuman_TraderHead() {
        return oSubjectHuman_TraderHead;
    }

    public void setoSubjectHuman_TraderHead(SubjectHuman oSubjectHuman_TraderHead) {
        this.oSubjectHuman_TraderHead = oSubjectHuman_TraderHead;
    }

    public SubjectHuman getoSubjectHuman_TraderAccountant() {
        return oSubjectHuman_TraderAccountant;
    }

    public void setoSubjectHuman_TraderAccountant(SubjectHuman oSubjectHuman_TraderAccountant) {
        this.oSubjectHuman_TraderAccountant = oSubjectHuman_TraderAccountant;
    }
    
    
}
