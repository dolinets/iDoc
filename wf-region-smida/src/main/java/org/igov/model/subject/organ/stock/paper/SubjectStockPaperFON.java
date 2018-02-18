package org.igov.model.subject.organ.stock.paper;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.igov.model.core.AbstractEntity;
import org.igov.model.document.TermType;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class SubjectStockPaperFON extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "oSubjectStockPaperFinancialInstitution")
    @ManyToOne(targetEntity = SubjectStockPaperFinancialInstitution.class)
    @JoinColumn(name = "nID_SubjectStockPaperFinancialInstitution")
    private SubjectStockPaperFinancialInstitution oSubjectStockPaperFinancialInstitution;
    
    @JsonProperty(value = "sName")
    @Column
    private String sName;

    @JsonProperty(value = "oTermType")
    @ManyToOne(targetEntity = TermType.class)
    @JoinColumn(name = "nID_TermType")
    private TermType oTermType;
    
    @JsonProperty(value = "nTermCount")
    @Column
    private Long nTermCount;

    public SubjectStockPaperFinancialInstitution getoSubjectStockPaperFinancialInstitution() {
        return oSubjectStockPaperFinancialInstitution;
    }

    public void setoSubjectStockPaperFinancialInstitution(SubjectStockPaperFinancialInstitution oSubjectStockPaperFinancialInstitution) {
        this.oSubjectStockPaperFinancialInstitution = oSubjectStockPaperFinancialInstitution;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
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

    
}
