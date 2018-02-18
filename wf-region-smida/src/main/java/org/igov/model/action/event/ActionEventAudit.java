package org.igov.model.action.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.hibernate.annotations.Type;
import org.igov.model.core.AbstractEntity;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class ActionEventAudit extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oActionEvent")
    @OneToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent")
    private ActionEvent oActionEvent;

    @JsonProperty(value = "sDateConclusion")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateConclusion;

    public ActionEvent getoActionEvent() {
        return oActionEvent;
    }

    public void setoActionEvent(ActionEvent oActionEvent) {
        this.oActionEvent = oActionEvent;
    }

    public DateTime getsDateConclusion() {
        return sDateConclusion;
    }

    public void setsDateConclusion(DateTime sDateConclusion) {
        this.sDateConclusion = sDateConclusion;
    }

    
}
