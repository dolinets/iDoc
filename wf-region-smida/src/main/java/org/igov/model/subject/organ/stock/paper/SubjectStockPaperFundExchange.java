package org.igov.model.subject.organ.stock.paper;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.igov.model.core.AbstractEntity;
import org.igov.model.document.DocumentStatutory;
import org.igov.model.subject.SubjectContact;
import org.igov.model.subject.SubjectHuman;
import org.igov.model.subject.organ.SubjectOrgan;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class SubjectStockPaperFundExchange extends AbstractEntity{
    
    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oSubjectOrgan_FundExchange")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_FundExchange")
    private SubjectOrgan oSubjectOrgan_FundExchange;

    @JsonProperty(value = "oDocumentStatutory_FundExchange")
    @ManyToOne(targetEntity = DocumentStatutory.class)
    @JoinColumn(name = "nID_DocumentStatutory_FundExchange")
    private DocumentStatutory oDocumentStatutory_FundExchange;

    @JsonProperty(value = "oSubjectHuman_FundExchangeHead")
    @ManyToOne(targetEntity = SubjectHuman.class)
    @JoinColumn(name = "nID_SubjectHuman_FundExchangeHead")
    private SubjectHuman oSubjectHuman_FundExchangeHead;

    public SubjectOrgan getoSubjectOrgan_FundExchange() {
        return oSubjectOrgan_FundExchange;
    }

    public void setoSubjectOrgan_FundExchange(SubjectOrgan oSubjectOrgan_FundExchange) {
        this.oSubjectOrgan_FundExchange = oSubjectOrgan_FundExchange;
    }

    public DocumentStatutory getoDocumentStatutory_FundExchange() {
        return oDocumentStatutory_FundExchange;
    }

    public void setoDocumentStatutory_FundExchange(DocumentStatutory oDocumentStatutory_FundExchange) {
        this.oDocumentStatutory_FundExchange = oDocumentStatutory_FundExchange;
    }

    public SubjectHuman getoSubjectHuman_FundExchangeHead() {
        return oSubjectHuman_FundExchangeHead;
    }

    public void setoSubjectHuman_FundExchangeHead(SubjectHuman oSubjectHuman_FundExchangeHead) {
        this.oSubjectHuman_FundExchangeHead = oSubjectHuman_FundExchangeHead;
    }

    
}
