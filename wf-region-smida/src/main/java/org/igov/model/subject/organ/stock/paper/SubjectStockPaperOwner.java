package org.igov.model.subject.organ.stock.paper;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.igov.model.core.AbstractEntity;
import org.igov.model.finance.Currency;
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
public class SubjectStockPaperOwner extends AbstractEntity {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "nQuarter")
    @Column
    private Long nQuarter;
    
    @JsonProperty(value = "nYear")
    @Column
    private Long nYear;
    
    @JsonProperty(value = "oSubjectOrgan_Depositary")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_Depositary")
    private SubjectOrgan oSubjectOrgan_Depositary;

    @JsonProperty(value = "oSubjectOrgan_Emitente")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_Emitente")
    private SubjectOrgan oSubjectOrgan_Emitente;

    @JsonProperty(value = "sForeignINNEmitente")
    @Column
    private String sForeignINNEmitente;
       
    @JsonProperty(value = "oCountry")
    @ManyToOne(targetEntity = Country.class)
    @JoinColumn(name = "nID_Country")
    private Country oCountry;

    @JsonProperty(value = "sForeignINNStockPapers")
    @Column
    private String sForeignINNStockPapers;
    
    @JsonProperty(value = "sCFICode")
    @Column
    private String sCFICode;

    @JsonProperty(value = "nDenomination")
    @Column
    private Long nDenomination;

    @JsonProperty(value = "oCurrency")
    @ManyToOne(targetEntity = Currency.class)
    @JoinColumn(name = "nID_Currency")
    private Currency oCurrency;

    @JsonProperty(value = "nCommonDenomination")
    @Column
    private Long nCommonDenomination;

    @JsonProperty(value = "nQuantity")
    @Column
    private Long nQuantity;

    @JsonProperty(value = "nPartCharterCapital")
    @Column
    private Long nPartCharterCapital;

    @JsonProperty(value = "nQuantityBlocked")
    @Column
    private Long nQuantityBlocked;

    @JsonProperty(value = "nPartBlockedCharterCapital")
    @Column
    private Long nPartBlockedCharterCapital;

    @JsonProperty(value = "sTypeOwner")
    @Column
    private String sTypeOwner;

    @JsonProperty(value = "oSubjectOrgan_Owner")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_Owner")
    private SubjectOrgan oSubjectOrgan_Owner;

    @JsonProperty(value = "oPlace_Owner")
    @ManyToOne(targetEntity = Place.class)
    @JoinColumn(name = "nID_Place_Owner")
    private Place oPlace_Owner;

    @JsonProperty(value = "oSubjectContact_OwnerNonResident")
    @ManyToOne(targetEntity = SubjectContact.class)
    @JoinColumn(name = "nID_SubjectContact_OwnerNonResident")
    private SubjectContact oSubjectContact_OwnerNonResident;

    @JsonProperty(value = "sForeignINNOwner")
    @Column
    private String sForeignINNOwner;

    @JsonProperty(value = "oSubjectHuman_Owner")
    @ManyToOne(targetEntity = SubjectHuman.class)
    @JoinColumn(name = "nID_SubjectHuman_Owner")
    private SubjectHuman oSubjectHuman_Owner;

    @JsonProperty(value = "oSubjectHuman_OwnerISI")
    @ManyToOne(targetEntity = SubjectHuman.class)
    @JoinColumn(name = "nID_SubjectHuman_OwnerISI")
    private SubjectHuman oSubjectHuman_OwnerISI;

    @JsonProperty(value = "oSubjectStockPaperFON_OwnerFON")
    @ManyToOne(targetEntity = SubjectStockPaperFON.class)
    @JoinColumn(name = "nID_SubjectStockPaperFON_OwnerFON")
    private SubjectStockPaperFON oSubjectStockPaperFON_OwnerFON;

    @JsonProperty(value = "oSubjectHuman_OwnerAMC")
    @ManyToOne(targetEntity = SubjectHuman.class)
    @JoinColumn(name = "nID_SubjectHuman_OwnerAMC")
    private SubjectHuman oSubjectHuman_OwnerAMC;

    public Long getnQuarter() {
        return nQuarter;
    }

    public void setnQuarter(Long nQuarter) {
        this.nQuarter = nQuarter;
    }

    public Long getnYear() {
        return nYear;
    }

    public void setnYear(Long nYear) {
        this.nYear = nYear;
    }

    public SubjectOrgan getoSubjectOrgan_Depositary() {
        return oSubjectOrgan_Depositary;
    }

    public void setoSubjectOrgan_Depositary(SubjectOrgan oSubjectOrgan_Depositary) {
        this.oSubjectOrgan_Depositary = oSubjectOrgan_Depositary;
    }

    public SubjectOrgan getoSubjectOrgan_Emitente() {
        return oSubjectOrgan_Emitente;
    }

    public void setoSubjectOrgan_Emitente(SubjectOrgan oSubjectOrgan_Emitente) {
        this.oSubjectOrgan_Emitente = oSubjectOrgan_Emitente;
    }

    public String getsForeignINNEmitente() {
        return sForeignINNEmitente;
    }

    public void setsForeignINNEmitente(String sForeignINNEmitente) {
        this.sForeignINNEmitente = sForeignINNEmitente;
    }

    public String getsForeignINNStockPapers() {
        return sForeignINNStockPapers;
    }

    public void setsForeignINNStockPapers(String sForeignINNStockPapers) {
        this.sForeignINNStockPapers = sForeignINNStockPapers;
    }

    public String getsCFICode() {
        return sCFICode;
    }

    public void setsCFICode(String sCFICode) {
        this.sCFICode = sCFICode;
    }

    public Long getnDenomination() {
        return nDenomination;
    }

    public void setnDenomination(Long nDenomination) {
        this.nDenomination = nDenomination;
    }

    public Currency getoCurrency() {
        return oCurrency;
    }

    public void setoCurrency(Currency oCurrency) {
        this.oCurrency = oCurrency;
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

    public Long getnPartCharterCapital() {
        return nPartCharterCapital;
    }

    public void setnPartCharterCapital(Long nPartCharterCapital) {
        this.nPartCharterCapital = nPartCharterCapital;
    }

    public Long getnQuantityBlocked() {
        return nQuantityBlocked;
    }

    public void setnQuantityBlocked(Long nQuantityBlocked) {
        this.nQuantityBlocked = nQuantityBlocked;
    }

    public Long getnPartBlockedCharterCapital() {
        return nPartBlockedCharterCapital;
    }

    public void setnPartBlockedCharterCapital(Long nPartBlockedCharterCapital) {
        this.nPartBlockedCharterCapital = nPartBlockedCharterCapital;
    }

    public String getsTypeOwner() {
        return sTypeOwner;
    }

    public void setsTypeOwner(String sTypeOwner) {
        this.sTypeOwner = sTypeOwner;
    }

    public Country getoCountry() {
        return oCountry;
    }

    public void setoCountry(Country oCountry) {
        this.oCountry = oCountry;
    }

    public SubjectOrgan getoSubjectOrgan_Owner() {
        return oSubjectOrgan_Owner;
    }

    public void setoSubjectOrgan_Owner(SubjectOrgan oSubjectOrgan_Owner) {
        this.oSubjectOrgan_Owner = oSubjectOrgan_Owner;
    }

    public Place getoPlace_Owner() {
        return oPlace_Owner;
    }

    public void setoPlace_Owner(Place oPlace_Owner) {
        this.oPlace_Owner = oPlace_Owner;
    }

    public SubjectContact getoSubjectContact_OwnerNonResident() {
        return oSubjectContact_OwnerNonResident;
    }

    public void setoSubjectContact_OwnerNonResident(SubjectContact oSubjectContact_OwnerNonResident) {
        this.oSubjectContact_OwnerNonResident = oSubjectContact_OwnerNonResident;
    }

    public String getsForeignINNOwner() {
        return sForeignINNOwner;
    }

    public void setsForeignINNOwner(String sForeignINNOwner) {
        this.sForeignINNOwner = sForeignINNOwner;
    }

    public SubjectHuman getoSubjectHuman_Owner() {
        return oSubjectHuman_Owner;
    }

    public void setoSubjectHuman_Owner(SubjectHuman oSubjectHuman_Owner) {
        this.oSubjectHuman_Owner = oSubjectHuman_Owner;
    }

    public SubjectHuman getoSubjectHuman_OwnerISI() {
        return oSubjectHuman_OwnerISI;
    }

    public void setoSubjectHuman_OwnerISI(SubjectHuman oSubjectHuman_OwnerISI) {
        this.oSubjectHuman_OwnerISI = oSubjectHuman_OwnerISI;
    }

    public SubjectStockPaperFON getoSubjectStockPaperFON_OwnerFON() {
        return oSubjectStockPaperFON_OwnerFON;
    }

    public void setoSubjectStockPaperFON_OwnerFON(SubjectStockPaperFON oSubjectStockPaperFON_OwnerFON) {
        this.oSubjectStockPaperFON_OwnerFON = oSubjectStockPaperFON_OwnerFON;
    }

    public SubjectHuman getoSubjectHuman_OwnerAMC() {
        return oSubjectHuman_OwnerAMC;
    }

    public void setoSubjectHuman_OwnerAMC(SubjectHuman oSubjectHuman_OwnerAMC) {
        this.oSubjectHuman_OwnerAMC = oSubjectHuman_OwnerAMC;
    }

    

    
}
