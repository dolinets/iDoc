package org.igov.model.subject.organ.stock.paper;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.igov.model.core.AbstractEntity;
import org.igov.model.subject.organ.SubjectOrgan;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class SubjectStockPaperEmitente extends AbstractEntity {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "oSubjectOrgan_Emitente")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_SubjectOrgan_Emitente")
    private SubjectOrgan oSubjectOrgan_Emitente;

    @JsonProperty(value = "bStoppedMakingChangesDS")
    @Column
    private Boolean bStoppedMakingChangesDS;

    @JsonProperty(value = "bAbsentFromLocation")
    @Column
    private Boolean bAbsentFromLocation;
    
    @JsonProperty(value = "bFictitious")
    @Column
    private Boolean bFictitious;
    
    @JsonProperty(value = "bStoppedTrading")
    @Column
    private Boolean bStoppedTrading;
    
    @JsonProperty(value = "bManipulatingPrices")
    @Column
    private Boolean bManipulatingPrices;

    public SubjectOrgan getoSubjectOrgan_Emitente() {
        return oSubjectOrgan_Emitente;
    }

    public void setoSubjectOrgan_Emitente(SubjectOrgan oSubjectOrgan_Emitente) {
        this.oSubjectOrgan_Emitente = oSubjectOrgan_Emitente;
    }

    public Boolean getbStoppedMakingChangesDS() {
        return bStoppedMakingChangesDS;
    }

    public void setbStoppedMakingChangesDS(Boolean bStoppedMakingChangesDS) {
        this.bStoppedMakingChangesDS = bStoppedMakingChangesDS;
    }

    public Boolean getbAbsentFromLocation() {
        return bAbsentFromLocation;
    }

    public void setbAbsentFromLocation(Boolean bAbsentFromLocation) {
        this.bAbsentFromLocation = bAbsentFromLocation;
    }

    public Boolean getbFictitious() {
        return bFictitious;
    }

    public void setbFictitious(Boolean bFictitious) {
        this.bFictitious = bFictitious;
    }

    public Boolean getbStoppedTrading() {
        return bStoppedTrading;
    }

    public void setbStoppedTrading(Boolean bStoppedTrading) {
        this.bStoppedTrading = bStoppedTrading;
    }

    public Boolean getbManipulatingPrices() {
        return bManipulatingPrices;
    }

    public void setbManipulatingPrices(Boolean bManipulatingPrices) {
        this.bManipulatingPrices = bManipulatingPrices;
    }

    @Override
    public String toString() {
        return "SubjectStockPaperEmitente{" + "oSubjectOrgan_Emitente=" + oSubjectOrgan_Emitente + ", bStoppedMakingChangesDS=" + bStoppedMakingChangesDS + ", bAbsentFromLocation=" + bAbsentFromLocation + ", bFictitious=" + bFictitious + ", bStoppedTrading=" + bStoppedTrading + ", bManipulatingPrices=" + bManipulatingPrices + '}';
    }

}
