package org.igov.model.action.stock;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.igov.model.action.event.ActionEvent;
import org.igov.model.core.AbstractEntity;
import org.igov.model.object.ObjectStockPaperOptionCertificate;
import org.igov.model.subject.organ.SubjectOrgan;

/**
 *
 * @author AleksandrK
 */
@javax.persistence.Entity
public class ActionStockPaperOptionCertificate extends AbstractEntity {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "oActionEvent")
    @OneToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent")
    private ActionEvent oActionEvent;
    
    @JsonProperty(value = "oObjectStockPaperOptionCertificate")
    @OneToOne(targetEntity = ObjectStockPaperOptionCertificate.class)
    @JoinColumn(name = "nID_ObjectStockPaperOptionCertificate")
    private ObjectStockPaperOptionCertificate oObjectStockPaperOptionCertificate;
    
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

    public ObjectStockPaperOptionCertificate getoObjectStockPaperOptionCertificate() {
        return oObjectStockPaperOptionCertificate;
    }

    public void setoObjectStockPaperOptionCertificate(ObjectStockPaperOptionCertificate oObjectStockPaperOptionCertificate) {
        this.oObjectStockPaperOptionCertificate = oObjectStockPaperOptionCertificate;
    }

    public SubjectOrgan getoSubjectOrgan() {
        return oSubjectOrgan;
    }

    public void setoSubjectOrgan(SubjectOrgan oSubjectOrgan) {
        this.oSubjectOrgan = oSubjectOrgan;
    }
    
    
}
