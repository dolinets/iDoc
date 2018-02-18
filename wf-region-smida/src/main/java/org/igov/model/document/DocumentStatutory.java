package org.igov.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import org.hibernate.annotations.Type;
import org.igov.model.core.AbstractEntity;
import org.igov.model.subject.SubjectTree;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class DocumentStatutory extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oDocument")
    @OneToOne(targetEntity = Document.class)
    @JoinColumn(name = "nID_Document")
    private Document oDocument;

    @JsonProperty(value = "nID_TermType")
    @ManyToOne(targetEntity = TermType.class)
    @JoinColumn(name = "nID_TermType")
    private TermType oTermType;

    @JsonProperty(value = "nTermCount")
    @Column
    private Long nTermCount;

    @JsonProperty(value = "sDateFrom")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateFrom;

    @JsonProperty(value = "sDateTill")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateTill;

    @JsonProperty(value = "oSubjectTree_Publisher")
    @ManyToOne(targetEntity = SubjectTree.class)
    @JoinColumn(name = "nID_SubjectTree_Publisher")
    private SubjectTree oSubjectTree_Publisher;

    @JsonProperty(value = "sKey_Document_Adjustment")
    @Column
    private String sKey_Document_Adjustment;
    
    @JsonProperty(value = "sDate_Document_Adjustment")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDate_Document_Adjustment;

    @JsonProperty(value = "oDocumentStatutoryState")
    @ManyToOne(targetEntity = DocumentStatutoryState.class)
    @JoinColumn(name = "nID_DocumentStatutoryState ")
    private DocumentStatutoryState oDocumentStatutoryState ;

    public Document getoDocument() {
        return oDocument;
    }

    public void setoDocument(Document oDocument) {
        this.oDocument = oDocument;
    }

    public TermType getoTermType() {
        return oTermType;
    }

    public void setoTermType(TermType oTermType) {
        this.oTermType = oTermType;
    }

    public Long getnTermCount() {
        return nTermCount;
    }

    public void setnTermCount(Long nTermCount) {
        this.nTermCount = nTermCount;
    }

    public DateTime getsDateFrom() {
        return sDateFrom;
    }

    public void setsDateFrom(DateTime sDateFrom) {
        this.sDateFrom = sDateFrom;
    }

    public DateTime getsDateTill() {
        return sDateTill;
    }

    public void setsDateTill(DateTime sDateTill) {
        this.sDateTill = sDateTill;
    }

    public SubjectTree getoSubjectTree_Publisher() {
        return oSubjectTree_Publisher;
    }

    public void setoSubjectTree_Publisher(SubjectTree oSubjectTree_Publisher) {
        this.oSubjectTree_Publisher = oSubjectTree_Publisher;
    }

    public String getsKey_Document_Adjustment() {
        return sKey_Document_Adjustment;
    }

    public void setsKey_Document_Adjustment(String sKey_Document_Adjustment) {
        this.sKey_Document_Adjustment = sKey_Document_Adjustment;
    }

    public DateTime getsDate_Document_Adjustment() {
        return sDate_Document_Adjustment;
    }

    public void setsDate_Document_Adjustment(DateTime sDate_Document_Adjustment) {
        this.sDate_Document_Adjustment = sDate_Document_Adjustment;
    }

    public DocumentStatutoryState getoDocumentStatutoryState() {
        return oDocumentStatutoryState;
    }

    public void setoDocumentStatutoryState(DocumentStatutoryState oDocumentStatutoryState) {
        this.oDocumentStatutoryState = oDocumentStatutoryState;
    }

    @Override
    public String toString() {
        return "DocumentStatutory{" + "oDocument=" + oDocument + ", oTermType=" + oTermType + ", nTermCount=" + nTermCount + ", sDateFrom=" + sDateFrom + ", sDateTill=" + sDateTill + ", oSubjectTree_Publisher=" + oSubjectTree_Publisher + ", sKey_Document_Adjustment=" + sKey_Document_Adjustment + ", sDate_Document_Adjustment=" + sDate_Document_Adjustment + ", oDocumentStatutoryState=" + oDocumentStatutoryState + '}';
    }
    
    
}
