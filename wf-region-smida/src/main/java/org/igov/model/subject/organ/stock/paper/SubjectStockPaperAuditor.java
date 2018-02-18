package org.igov.model.subject.organ.stock.paper;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.igov.model.action.event.ActionEvent;
import org.igov.model.core.AbstractEntity;
import org.igov.model.document.Document;
import org.igov.model.document.DocumentStatutory;
import org.igov.model.object.place.Place;
import org.igov.model.subject.SubjectContact;
import org.igov.model.subject.SubjectOperatorBank;
import org.igov.model.subject.organ.SubjectOrgan;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class SubjectStockPaperAuditor extends AbstractEntity{
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "oSubjectOrgan_Auditor")
    @OneToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_Auditor")
    private SubjectOrgan oSubjectOrgan_Auditor;

    @JsonProperty(value = "oPlace_Auditor")
    @OneToOne(targetEntity = Place.class)
    @JoinColumn(name = "nID_Place_Auditor")
    private Place oPlace_Auditor;
    
    @JsonProperty(value = "oDocumentStatutory_CertificateFirm")
    @OneToOne(targetEntity = DocumentStatutory.class)
    @JoinColumn(name = "nID_DocumentStatutory_CertificateFirm")
    private DocumentStatutory oDocumentStatutory_CertificateFirm;

    @JsonProperty(value = "oSubjectOperatorBank_Auditor")
    @OneToOne(targetEntity = SubjectOperatorBank.class)
    @JoinColumn(name = "nID_SubjectOperatorBank_Auditor")
    private SubjectOperatorBank oSubjectOperatorBank_Auditor;

    @JsonProperty(value = "sPositionCustom")
    @Column 
    private String sPositionCustom;

    @JsonProperty(value = "oDocumentStatutory_Certificate")
    @OneToOne(targetEntity = DocumentStatutory.class)
    @JoinColumn(name = "nID_DocumentStatutory_Certificate")
    private DocumentStatutory oDocumentStatutory_Certificate;

    @JsonProperty(value = "oActionEvent_Issue")
    @OneToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_Issue")
    private ActionEvent oActionEvent_Issue;

    @JsonProperty(value = "oDocument")
    @OneToOne(targetEntity = Document.class)
    @JoinColumn(name = "nID_Document")
    private Document oDocument;

    public SubjectOrgan getoSubjectOrgan_Auditor() {
        return oSubjectOrgan_Auditor;
    }

    public void setoSubjectOrgan_Auditor(SubjectOrgan oSubjectOrgan_Auditor) {
        this.oSubjectOrgan_Auditor = oSubjectOrgan_Auditor;
    }

    public Place getoPlace_Auditor() {
        return oPlace_Auditor;
    }

    public void setoPlace_Auditor(Place oPlace_Auditor) {
        this.oPlace_Auditor = oPlace_Auditor;
    }

    public DocumentStatutory getoDocumentStatutory_CertificateFirm() {
        return oDocumentStatutory_CertificateFirm;
    }

    public void setoDocumentStatutory_CertificateFirm(DocumentStatutory oDocumentStatutory_CertificateFirm) {
        this.oDocumentStatutory_CertificateFirm = oDocumentStatutory_CertificateFirm;
    }

    public SubjectOperatorBank getoSubjectOperatorBank_Auditor() {
        return oSubjectOperatorBank_Auditor;
    }

    public void setoSubjectOperatorBank_Auditor(SubjectOperatorBank oSubjectOperatorBank_Auditor) {
        this.oSubjectOperatorBank_Auditor = oSubjectOperatorBank_Auditor;
    }

    public String getsPositionCustom() {
        return sPositionCustom;
    }

    public void setsPositionCustom(String sPositionCustom) {
        this.sPositionCustom = sPositionCustom;
    }

    public DocumentStatutory getoDocumentStatutory_Certificate() {
        return oDocumentStatutory_Certificate;
    }

    public void setoDocumentStatutory_Certificate(DocumentStatutory oDocumentStatutory_Certificate) {
        this.oDocumentStatutory_Certificate = oDocumentStatutory_Certificate;
    }

    public ActionEvent getoActionEvent_Issue() {
        return oActionEvent_Issue;
    }

    public void setoActionEvent_Issue(ActionEvent oActionEvent_Issue) {
        this.oActionEvent_Issue = oActionEvent_Issue;
    }

    public Document getoDocument() {
        return oDocument;
    }

    public void setoDocument(Document oDocument) {
        this.oDocument = oDocument;
    }
    
    
    
}
