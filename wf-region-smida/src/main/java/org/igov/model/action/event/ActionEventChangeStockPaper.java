package org.igov.model.action.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.igov.model.core.AbstractEntity;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class ActionEventChangeStockPaper extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oActionEvent")
    @OneToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent")
    private ActionEventType oActionEvent;

    @JsonProperty(value = "sCharacterChanges")
    @Column
    private String sCharacterChanges;

    public ActionEventType getoActionEvent() {
        return oActionEvent;
    }

    public void setoActionEvent(ActionEventType oActionEvent) {
        this.oActionEvent = oActionEvent;
    }

    public String getsCharacterChanges() {
        return sCharacterChanges;
    }

    public void setsCharacterChanges(String sCharacterChanges) {
        this.sCharacterChanges = sCharacterChanges;
    }
    
    
}
