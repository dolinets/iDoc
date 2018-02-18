package org.igov.model.action.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.hibernate.annotations.Type;
import org.igov.model.core.AbstractEntity;
import org.igov.model.subject.organ.SubjectOrgan;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class ActionEventEnforcementRequirements extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oActionEvent")
    @OneToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent")
    private ActionEvent oActionEvent;

    @JsonProperty(value = "sTypeOffender")
    @Column
    private String sTypeOffender;

    @JsonProperty(value = "sFile")
    @Column
    private String sFile;

    @JsonProperty(value = "sAgents")
    @Column
    private String sAgents;

    @JsonProperty(value = "bConfidentiality")
    @Column
    private boolean bConfidentiality;

    @JsonProperty(value = "oSubjectOrgan_EnforcementER")
    @OneToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_EnforcementER")
    private SubjectOrgan oSubjectOrgan_EnforcementER;

    @JsonProperty(value = "sStatutoryActs")
    @Column
    private String sStatutoryActs;

    @JsonProperty(value = "sDateExecution")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateExecution;
    
    @JsonProperty(value = "sDateNoticeExecution")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateNoticeExecution;

    public ActionEvent getoActionEvent() {
        return oActionEvent;
    }

    public void setoActionEvent(ActionEvent oActionEvent) {
        this.oActionEvent = oActionEvent;
    }

    public String getsTypeOffender() {
        return sTypeOffender;
    }

    public void setsTypeOffender(String sTypeOffender) {
        this.sTypeOffender = sTypeOffender;
    }

    public String getsFile() {
        return sFile;
    }

    public void setsFile(String sFile) {
        this.sFile = sFile;
    }

    public String getsAgents() {
        return sAgents;
    }

    public void setsAgents(String sAgents) {
        this.sAgents = sAgents;
    }

    public boolean isbConfidentiality() {
        return bConfidentiality;
    }

    public void setbConfidentiality(boolean bConfidentiality) {
        this.bConfidentiality = bConfidentiality;
    }

    public SubjectOrgan getoSubjectOrgan_EnforcementER() {
        return oSubjectOrgan_EnforcementER;
    }

    public void setoSubjectOrgan_EnforcementER(SubjectOrgan oSubjectOrgan_EnforcementER) {
        this.oSubjectOrgan_EnforcementER = oSubjectOrgan_EnforcementER;
    }

    public String getsStatutoryActs() {
        return sStatutoryActs;
    }

    public void setsStatutoryActs(String sStatutoryActs) {
        this.sStatutoryActs = sStatutoryActs;
    }

    public DateTime getsDateExecution() {
        return sDateExecution;
    }

    public void setsDateExecution(DateTime sDateExecution) {
        this.sDateExecution = sDateExecution;
    }

    public DateTime getsDateNoticeExecution() {
        return sDateNoticeExecution;
    }

    public void setsDateNoticeExecution(DateTime sDateNoticeExecution) {
        this.sDateNoticeExecution = sDateNoticeExecution;
    }

    
}
