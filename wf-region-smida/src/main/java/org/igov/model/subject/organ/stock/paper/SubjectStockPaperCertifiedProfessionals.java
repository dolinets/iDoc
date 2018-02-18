package org.igov.model.subject.organ.stock.paper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.Type;
import org.igov.model.core.AbstractEntity;
import org.igov.model.dictionary.Dictionary;
import org.igov.model.document.Document;
import org.igov.model.document.DocumentStatutory;
import org.igov.model.object.place.Place;
import org.igov.model.subject.SubjectHuman;
import org.igov.model.subject.organ.SubjectOrgan;
import org.igov.model.subject.organ.SubjectOrganOperatorStudy;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class SubjectStockPaperCertifiedProfessionals extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "sNumberFolder")
    @Column
    private String sNumberFolder;

    @JsonProperty(value = "sDateFolder")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateFolder;

    @JsonProperty(value = "sDateTillFolder")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateTillFolder;

    @JsonProperty(value = "oDocumentStatutory_Certificate")
    @ManyToOne(targetEntity = DocumentStatutory.class)
    @JoinColumn(name = "nID_DocumentStatutory_Certificate")
    private DocumentStatutory oDocumentStatutory_Certificate;

    @JsonProperty(value = "bDuplicate")
    @Column
    private boolean bDuplicate;

    @JsonProperty(value = "bCancellation")
    @Column
    private boolean bCancellation;

    @JsonProperty(value = "oPlace_CertifiedProfessionals")
    @ManyToOne(targetEntity = DocumentStatutory.class)
    @JoinColumn(name = "nID_Place_CertifiedProfessionals")
    private Place oPlace_CertifiedProfessionals;

    @JsonProperty(value = "sDatePayment")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDatePayment;

    @JsonProperty(value = "oSubjectHuman")
    @ManyToOne(targetEntity = SubjectHuman.class)
    @JoinColumn(name = "nID_SubjectHuman")
    private SubjectHuman oSubjectHuman;

    @JsonProperty(value = "sPosition")
    @Column
    private String sPosition;

    @JsonProperty(value = "oSubjectOrgan")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan")
    private SubjectOrgan oSubjectOrgan;

    @JsonProperty(value = "oDictionary_TypeActivity")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_TypeActivity")
    private Dictionary oDictionary_TypeActivity;
    
    @JsonProperty(value = "oDictionary_State")
    @ManyToOne(targetEntity = Dictionary.class)
    @JoinColumn(name = "nID_Dictionary_State")
    private Dictionary oDictionary_State;
    
    @JsonProperty(value = "oDocument")
    @ManyToOne(targetEntity = Document.class)
    @JoinColumn(name = "nID_Document")
    private Document oDocument;

    @JsonProperty(value = "oSubjectOrganOperatorStudy")
    @ManyToOne(targetEntity = SubjectOrganOperatorStudy.class)
    @JoinColumn(name = "nID_SubjectOrganOperatorStudy")
    private SubjectOrganOperatorStudy oSubjectOrganOperatorStudy;

    public String getsNumberFolder() {
        return sNumberFolder;
    }

    public void setsNumberFolder(String sNumberFolder) {
        this.sNumberFolder = sNumberFolder;
    }

    public DateTime getsDateFolder() {
        return sDateFolder;
    }

    public void setsDateFolder(DateTime sDateFolder) {
        this.sDateFolder = sDateFolder;
    }

    public DateTime getsDateTillFolder() {
        return sDateTillFolder;
    }

    public void setsDateTillFolder(DateTime sDateTillFolder) {
        this.sDateTillFolder = sDateTillFolder;
    }

    public DocumentStatutory getoDocumentStatutory_Certificate() {
        return oDocumentStatutory_Certificate;
    }

    public void setoDocumentStatutory_Certificate(DocumentStatutory oDocumentStatutory_Certificate) {
        this.oDocumentStatutory_Certificate = oDocumentStatutory_Certificate;
    }

    public boolean isbDuplicate() {
        return bDuplicate;
    }

    public void setbDuplicate(boolean bDuplicate) {
        this.bDuplicate = bDuplicate;
    }

    public boolean isbCancellation() {
        return bCancellation;
    }

    public void setbCancellation(boolean bCancellation) {
        this.bCancellation = bCancellation;
    }

    public Place getoPlace_CertifiedProfessionals() {
        return oPlace_CertifiedProfessionals;
    }

    public void setoPlace_CertifiedProfessionals(Place oPlace_CertifiedProfessionals) {
        this.oPlace_CertifiedProfessionals = oPlace_CertifiedProfessionals;
    }

    public DateTime getsDatePayment() {
        return sDatePayment;
    }

    public void setsDatePayment(DateTime sDatePayment) {
        this.sDatePayment = sDatePayment;
    }

    public SubjectHuman getoSubjectHuman() {
        return oSubjectHuman;
    }

    public void setoSubjectHuman(SubjectHuman oSubjectHuman) {
        this.oSubjectHuman = oSubjectHuman;
    }

    public String getsPosition() {
        return sPosition;
    }

    public void setsPosition(String sPosition) {
        this.sPosition = sPosition;
    }

    public SubjectOrgan getoSubjectOrgan() {
        return oSubjectOrgan;
    }

    public void setoSubjectOrgan(SubjectOrgan oSubjectOrgan) {
        this.oSubjectOrgan = oSubjectOrgan;
    }

    public Dictionary getoDictionary_TypeActivity() {
        return oDictionary_TypeActivity;
    }

    public void setoDictionary_TypeActivity(Dictionary oDictionary_TypeActivity) {
        this.oDictionary_TypeActivity = oDictionary_TypeActivity;
    }

    public Dictionary getoDictionary_State() {
        return oDictionary_State;
    }

    public void setoDictionary_State(Dictionary oDictionary_State) {
        this.oDictionary_State = oDictionary_State;
    }

    public Document getoDocument() {
        return oDocument;
    }

    public void setoDocument(Document oDocument) {
        this.oDocument = oDocument;
    }

    public SubjectOrganOperatorStudy getoSubjectOrganOperatorStudy() {
        return oSubjectOrganOperatorStudy;
    }

    public void setoSubjectOrganOperatorStudy(SubjectOrganOperatorStudy oSubjectOrganOperatorStudy) {
        this.oSubjectOrganOperatorStudy = oSubjectOrganOperatorStudy;
    }

   

}
