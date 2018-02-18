package org.igov.model.action.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import org.hibernate.annotations.Type;
import org.igov.model.core.AbstractEntity;
import org.igov.model.document.Document;
import org.igov.model.subject.organ.SubjectOrgan;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class ActionEvent extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oActionEventType")
    @ManyToOne(targetEntity = ActionEventType.class)
    @JoinColumn(name = "nID_ActionEventType")
    private ActionEventType oActionEventType;

    @JsonProperty(value = "oSubjectOrgan_Author")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_Author")
    private SubjectOrgan oSubjectOrgan_Author;

    @JsonProperty(value = "sDate")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDate;

    @JsonProperty(value = "sReason")
    @Column
    private String sReason;

    @JsonProperty(value = "oDocument")
    @OneToOne(targetEntity = Document.class)
    @JoinColumn(name = "nID_Document")
    private Document oDocument;

    public ActionEventType getoActionEventType() {
        return oActionEventType;
    }

    public void setoActionEventType(ActionEventType oActionEventType) {
        this.oActionEventType = oActionEventType;
    }

    public SubjectOrgan getoSubjectOrgan_Author() {
        return oSubjectOrgan_Author;
    }

    public void setoSubjectOrgan_Author(SubjectOrgan oSubjectOrgan_Author) {
        this.oSubjectOrgan_Author = oSubjectOrgan_Author;
    }

    public DateTime getsDate() {
        return sDate;
    }

    public void setsDate(DateTime sDate) {
        this.sDate = sDate;
    }

    public String getsReason() {
        return sReason;
    }

    public void setsReason(String sReason) {
        this.sReason = sReason;
    }

    public Document getoDocument() {
        return oDocument;
    }

    public void setoDocument(Document oDocument) {
        this.oDocument = oDocument;
    }   
    
}
