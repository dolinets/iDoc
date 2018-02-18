package org.igov.model.action.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.igov.model.core.AbstractEntity;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class ActionEventType extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "sID")
    @Column
    private String sID;

    @JsonProperty(value = "oActionEventTypeGroup")
    @ManyToOne(targetEntity = ActionEventTypeGroup.class)
    @JoinColumn(name = "nID_ActionEventTypeGroup")
    private ActionEventTypeGroup oActionEventTypeGroup;

    @JsonProperty(value = "sName")
    @Column
    private String sName;

    @JsonProperty(value = "sNote")
    @Column
    private String sNote;

    public String getsID() {
        return sID;
    }

    public void setsID(String sID) {
        this.sID = sID;
    }

    public ActionEventTypeGroup getoActionEventTypeGroup() {
        return oActionEventTypeGroup;
    }

    public void setoActionEventTypeGroup(ActionEventTypeGroup oActionEventTypeGroup) {
        this.oActionEventTypeGroup = oActionEventTypeGroup;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getsNote() {
        return sNote;
    }

    public void setsNote(String sNote) {
        this.sNote = sNote;
    }

    @Override
    public String toString() {
        return "ActionEventType{" + "sID=" + sID + ", oActionEventTypeGroup=" + oActionEventTypeGroup + ", sName=" + sName + ", sNote=" + sNote + '}';
    }
    
}
