package org.igov.model.subject.organ.stock.paper;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.igov.model.action.event.ActionEvent;
import org.igov.model.core.AbstractEntity;
import org.igov.model.document.DocumentStatutory;
import org.igov.model.subject.SubjectContact;
import org.igov.model.subject.SubjectHuman;
import org.igov.model.subject.organ.SubjectOrgan;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class SubjectStockPaperFinancialInstitution extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oSubjectOrgan_FinancialInstitution")
    @ManyToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_FinancialInstitution")
    private SubjectOrgan oSubjectOrgan_FinancialInstitution;

    @JsonProperty(value = "oSubjectHuman_FinancialInstitutionHead")
    @ManyToOne(targetEntity = SubjectHuman.class)
    @JoinColumn(name = "nID_SubjectHuman_FinancialInstitutionHead")
    private SubjectHuman oSubjectHuman_FinancialInstitutionHead;

    @JsonProperty(value = "sInternalDocInformation")
    @Column
    private String sInternalDocInformation;

    @JsonProperty(value = "sSymbol")
    @Column
    private String sSymbol;

    @JsonProperty(value = "bSRO")
    @Column
    private boolean bSRO;

    @JsonProperty(value = "oDocumentStatutory_KIF")
    @ManyToOne(targetEntity = DocumentStatutory.class)
    @JoinColumn(name = "nID_DocumentStatutory_KIF")
    private DocumentStatutory oDocumentStatutory_KIF;

    @JsonProperty(value = "oDocumentStatutory_Licence")
    @ManyToOne(targetEntity = DocumentStatutory.class)
    @JoinColumn(name = "nID_DocumentStatutory_Licence")
    private DocumentStatutory oDocumentStatutory_Licence;

    @JsonProperty(value = "oActionEvent_AddRegistry")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_AddRegistry")
    private ActionEvent oActionEvent_AddRegistry;

    @JsonProperty(value = "oActionEvent_RemoveRegistry")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_RemoveRegistry")
    private ActionEvent oActionEvent_RemoveRegistry;
    
    @JsonProperty(value = "oDocumentStatutory_Certificate")
    @ManyToOne(targetEntity = DocumentStatutory.class)
    @JoinColumn(name = "nID_DocumentStatutory_Certificate")
    private DocumentStatutory oDocumentStatutory_Certificate;

    @JsonProperty(value = "oActionEvent_Reissue")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_Reissue")
    private ActionEvent oActionEvent_Reissue;
    
    @JsonProperty(value = "oActionEvent_Duplicate")
    @ManyToOne(targetEntity = ActionEvent.class)
    @JoinColumn(name = "nID_ActionEvent_Duplicate")
    private ActionEvent oActionEvent_Duplicate;

    @JsonProperty(value = "aSubjectStockPaperFON")
    @OneToMany(cascade = CascadeType.ALL)
    private List<SubjectStockPaperFON> aSubjectStockPaperFON;

    public SubjectOrgan getoSubjectOrgan_FinancialInstitution() {
        return oSubjectOrgan_FinancialInstitution;
    }

    public void setoSubjectOrgan_FinancialInstitution(SubjectOrgan oSubjectOrgan_FinancialInstitution) {
        this.oSubjectOrgan_FinancialInstitution = oSubjectOrgan_FinancialInstitution;
    }

    public SubjectHuman getoSubjectHuman_FinancialInstitutionHead() {
        return oSubjectHuman_FinancialInstitutionHead;
    }

    public void setoSubjectHuman_FinancialInstitutionHead(SubjectHuman oSubjectHuman_FinancialInstitutionHead) {
        this.oSubjectHuman_FinancialInstitutionHead = oSubjectHuman_FinancialInstitutionHead;
    }

    public String getsInternalDocInformation() {
        return sInternalDocInformation;
    }

    public void setsInternalDocInformation(String sInternalDocInformation) {
        this.sInternalDocInformation = sInternalDocInformation;
    }

    public String getsSymbol() {
        return sSymbol;
    }

    public void setsSymbol(String sSymbol) {
        this.sSymbol = sSymbol;
    }

    public boolean isbSRO() {
        return bSRO;
    }

    public void setbSRO(boolean bSRO) {
        this.bSRO = bSRO;
    }

    public DocumentStatutory getoDocumentStatutory_KIF() {
        return oDocumentStatutory_KIF;
    }

    public void setoDocumentStatutory_KIF(DocumentStatutory oDocumentStatutory_KIF) {
        this.oDocumentStatutory_KIF = oDocumentStatutory_KIF;
    }

    public DocumentStatutory getoDocumentStatutory_Licence() {
        return oDocumentStatutory_Licence;
    }

    public void setoDocumentStatutory_Licence(DocumentStatutory oDocumentStatutory_Licence) {
        this.oDocumentStatutory_Licence = oDocumentStatutory_Licence;
    }

    public ActionEvent getoActionEvent_AddRegistry() {
        return oActionEvent_AddRegistry;
    }

    public void setoActionEvent_AddRegistry(ActionEvent oActionEvent_AddRegistry) {
        this.oActionEvent_AddRegistry = oActionEvent_AddRegistry;
    }

    public ActionEvent getoActionEvent_RemoveRegistry() {
        return oActionEvent_RemoveRegistry;
    }

    public void setoActionEvent_RemoveRegistry(ActionEvent oActionEvent_RemoveRegistry) {
        this.oActionEvent_RemoveRegistry = oActionEvent_RemoveRegistry;
    }

    public DocumentStatutory getoDocumentStatutory_Certificate() {
        return oDocumentStatutory_Certificate;
    }

    public void setoDocumentStatutory_Certificate(DocumentStatutory oDocumentStatutory_Certificate) {
        this.oDocumentStatutory_Certificate = oDocumentStatutory_Certificate;
    }

    public ActionEvent getoActionEvent_Reissue() {
        return oActionEvent_Reissue;
    }

    public void setoActionEvent_Reissue(ActionEvent oActionEvent_Reissue) {
        this.oActionEvent_Reissue = oActionEvent_Reissue;
    }

    public ActionEvent getoActionEvent_Duplicate() {
        return oActionEvent_Duplicate;
    }

    public void setoActionEvent_Duplicate(ActionEvent oActionEvent_Duplicate) {
        this.oActionEvent_Duplicate = oActionEvent_Duplicate;
    }

    public List<SubjectStockPaperFON> getaSubjectStockPaperFON() {
        return aSubjectStockPaperFON;
    }

    public void setaSubjectStockPaperFON(List<SubjectStockPaperFON> aSubjectStockPaperFON) {
        this.aSubjectStockPaperFON = aSubjectStockPaperFON;
    }
    
    
}
