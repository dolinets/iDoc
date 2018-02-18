package org.igov.model.action.stock;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.igov.model.action.event.ActionEvent;
import org.igov.model.core.AbstractEntity;
import org.igov.model.object.ObjectStockPaperMortgageCertificate;
import org.igov.model.subject.organ.SubjectOrgan;

/**
 *
 * @author AleksandrK
 */
@javax.persistence.Entity
public class ActionStockPaperMortgageCertificate extends AbstractEntity {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "oActionEvent")
    @OneToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent")
    private ActionEvent oActionEvent;
    
    @JsonProperty(value = "oObjectStockPaperMortgageCertificate")
    @OneToOne(targetEntity = ObjectStockPaperMortgageCertificate.class)
    @JoinColumn(name = "nID_ObjectStockPaperMortgageCertificate")
    private ObjectStockPaperMortgageCertificate oObjectStockPaperMortgageCertificate;
    
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

    public ObjectStockPaperMortgageCertificate getoObjectStockPaperMortgageCertificate() {
        return oObjectStockPaperMortgageCertificate;
    }

    public void setoObjectStockPaperMortgageCertificate(ObjectStockPaperMortgageCertificate oObjectStockPaperMortgageCertificate) {
        this.oObjectStockPaperMortgageCertificate = oObjectStockPaperMortgageCertificate;
    }

    public SubjectOrgan getoSubjectOrgan() {
        return oSubjectOrgan;
    }

    public void setoSubjectOrgan(SubjectOrgan oSubjectOrgan) {
        this.oSubjectOrgan = oSubjectOrgan;
    }
    
    
    
}
