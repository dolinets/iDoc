package org.igov.model.action.stock;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.igov.model.action.event.ActionEvent;
import org.igov.model.core.AbstractEntity;
import org.igov.model.object.ObjectStockPaperInvestmentCertificate;
import org.igov.model.subject.organ.SubjectOrgan;

/**
 *
 * @author AleksandrK
 */
@javax.persistence.Entity
public class ActionStockPaperInvestmentCertificate extends AbstractEntity {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "oActionEvent")
    @OneToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent")
    private ActionEvent oActionEvent;
    
    @JsonProperty(value = "oObjectStockPaperInvestmentCertificate")
    @OneToOne(targetEntity = ObjectStockPaperInvestmentCertificate.class)
    @JoinColumn(name = "nID_ObjectStockPaperInvestmentCertificate")
    private ObjectStockPaperInvestmentCertificate oObjectStockPaperInvestmentCertificate;
    
    @JsonProperty(value = "oSubjectOrgan")
    @OneToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan")
    private SubjectOrgan oSubjectOrgan;

    public ActionEvent getoActionEvent() {
        return oActionEvent;
    }

    public void setoActionEvent(ActionEvent oActionEvent) {
        this.oActionEvent = oActionEvent;
    }

    public ObjectStockPaperInvestmentCertificate getoObjectStockPaperInvestmentCertificate() {
        return oObjectStockPaperInvestmentCertificate;
    }

    public void setoObjectStockPaperInvestmentCertificate(ObjectStockPaperInvestmentCertificate oObjectStockPaperInvestmentCertificate) {
        this.oObjectStockPaperInvestmentCertificate = oObjectStockPaperInvestmentCertificate;
    }

    public SubjectOrgan getoSubjectOrgan() {
        return oSubjectOrgan;
    }

    public void setoSubjectOrgan(SubjectOrgan oSubjectOrgan) {
        this.oSubjectOrgan = oSubjectOrgan;
    }
    
    
}
