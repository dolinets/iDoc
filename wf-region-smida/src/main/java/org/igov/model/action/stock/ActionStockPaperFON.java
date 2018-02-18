package org.igov.model.action.stock;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.igov.model.action.event.ActionEvent;
import org.igov.model.core.AbstractEntity;
import org.igov.model.object.ObjectStockPaperFON;
import org.igov.model.subject.organ.SubjectOrgan;

/**
 *
 * @author AleksandrK
 */
@javax.persistence.Entity
public class ActionStockPaperFON extends AbstractEntity {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "oActionEvent")
    @OneToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent")
    private ActionEvent oActionEvent;
    
    @JsonProperty(value = "oObjectStockPaperFON")
    @OneToOne(targetEntity = ObjectStockPaperFON.class)
    @JoinColumn(name = "nID_ObjectStockPaperFON")
    private ObjectStockPaperFON oObjectStockPaperFON;
    
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

    public ObjectStockPaperFON getoObjectStockPaperFON() {
        return oObjectStockPaperFON;
    }

    public void setoObjectStockPaperFON(ObjectStockPaperFON oObjectStockPaperFON) {
        this.oObjectStockPaperFON = oObjectStockPaperFON;
    }

    public SubjectOrgan getoSubjectOrgan() {
        return oSubjectOrgan;
    }

    public void setoSubjectOrgan(SubjectOrgan oSubjectOrgan) {
        this.oSubjectOrgan = oSubjectOrgan;
    }
    
    
}
