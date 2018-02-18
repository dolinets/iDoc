package org.igov.model.subject.organ.stock.paper;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.igov.model.action.event.ActionEventAudit;
import org.igov.model.core.AbstractEntity;
import org.igov.model.document.DocumentStatutory;
import org.igov.model.document.TermType;
import org.igov.model.object.place.Place;
import org.igov.model.subject.SubjectContact;
import org.igov.model.subject.SubjectHuman;
import org.igov.model.subject.SubjectOperatorBank;
import org.igov.model.subject.organ.SubjectOrgan;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class SubjectStockPaperDepository extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oPlace_Depository")
    @OneToOne(targetEntity = Place.class)
    @JoinColumn(name = "nID_Place_Depository")
    private Place oPlace_Depository;

    @JsonProperty(value = "oSubjectOperatorBank_Depository")
    @OneToOne(targetEntity = SubjectOperatorBank.class)
    @JoinColumn(name = "nID_SubjectOperatorBank_Depository")
    private SubjectOperatorBank oSubjectOperatorBank_Depository;

    @JsonProperty(value = "bAURKD")
    @Column
    private boolean bAURKD;

    @JsonProperty(value = "bAUFT")
    @Column
    private boolean bAUFT;

    @JsonProperty(value = "oSubjectHuman_DepositoryHead")
    @OneToOne(targetEntity = SubjectHuman.class)
    @JoinColumn(name = "nID_SubjectHuman_DepositoryHead")
    private SubjectHuman oSubjectHuman_DepositoryHead;

    @JsonProperty(value = "sNumberFolder")
    @Column
    private String sNumberFolder;

    @JsonProperty(value = "oSubjectHuman_ResponsiblePerson")
    @OneToOne(targetEntity = SubjectHuman.class)
    @JoinColumn(name = "nID_SubjectHuman_ResponsiblePerson")
    private SubjectHuman oSubjectHuman_ResponsiblePerson;

    @JsonProperty(value = "bClear")
    @Column
    private boolean bClear;

    @JsonProperty(value = "oSubjectOrgan_Depository")
    @OneToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_Depository")
    private SubjectOrgan oSubjectOrgan_Depository;

    @JsonProperty(value = "oTermType_Contract")
    @OneToOne(targetEntity = TermType.class)
    @JoinColumn(name = "nID_TermType_Contract")
    private TermType oTermType_Contract;

    @JsonProperty(value = "nTermCount_Contract")
    @Column
    private Long nTermCount_Contract;

    @JsonProperty(value = "oTermType_TemporaryContract")
    @OneToOne(targetEntity = TermType.class)
    @JoinColumn(name = "nID_TermType_TemporaryContract")
    private TermType oTermType_TemporaryContract;

    @JsonProperty(value = "nTermCount_TemporaryContract")
    @Column
    private Long nTermCount_TemporaryContract;

    @JsonProperty(value = "nCountSubdivision")
    @Column
    private Long nCountSubdivision;

    @JsonProperty(value = "nBranches")
    @Column
    private Long nBranches;

    @JsonProperty(value = "oSubjectOrgan_Auditory")
    @OneToOne(targetEntity = SubjectOrgan.class)
    @JoinColumn(name = "nID_SubjectOrgan_Auditor")
    private SubjectOrgan oSubjectOrgan_Auditory;

    @JsonProperty(value = "oActionEventAudit")
    @OneToOne(targetEntity = ActionEventAudit.class)
    @JoinColumn(name = "nID_ActionEventAudit")
    private ActionEventAudit oActionEventAudit;

    @JsonProperty(value = "sKindStatut")
    @Column
    private String sKindStatut;

    @JsonProperty(value = "bLicence")
    @Column
    private boolean bLicence;

    public Place getoPlace_Depository() {
        return oPlace_Depository;
    }

    public void setoPlace_Depository(Place oPlace_Depository) {
        this.oPlace_Depository = oPlace_Depository;
    }

    public SubjectOperatorBank getoSubjectOperatorBank_Depository() {
        return oSubjectOperatorBank_Depository;
    }

    public void setoSubjectOperatorBank_Depository(SubjectOperatorBank oSubjectOperatorBank_Depository) {
        this.oSubjectOperatorBank_Depository = oSubjectOperatorBank_Depository;
    }

    public boolean isbAURKD() {
        return bAURKD;
    }

    public void setbAURKD(boolean bAURKD) {
        this.bAURKD = bAURKD;
    }

    public boolean isbAUFT() {
        return bAUFT;
    }

    public void setbAUFT(boolean bAUFT) {
        this.bAUFT = bAUFT;
    }

    public SubjectHuman getoSubjectHuman_DepositoryHead() {
        return oSubjectHuman_DepositoryHead;
    }

    public void setoSubjectHuman_DepositoryHead(SubjectHuman oSubjectHuman_DepositoryHead) {
        this.oSubjectHuman_DepositoryHead = oSubjectHuman_DepositoryHead;
    }

    public String getsNumberFolder() {
        return sNumberFolder;
    }

    public void setsNumberFolder(String sNumberFolder) {
        this.sNumberFolder = sNumberFolder;
    }

    public SubjectHuman getoSubjectHuman_ResponsiblePerson() {
        return oSubjectHuman_ResponsiblePerson;
    }

    public void setoSubjectHuman_ResponsiblePerson(SubjectHuman oSubjectHuman_ResponsiblePerson) {
        this.oSubjectHuman_ResponsiblePerson = oSubjectHuman_ResponsiblePerson;
    }

    public boolean isbClear() {
        return bClear;
    }

    public void setbClear(boolean bClear) {
        this.bClear = bClear;
    }

    public SubjectOrgan getoSubjectOrgan_Depository() {
        return oSubjectOrgan_Depository;
    }

    public void setoSubjectOrgan_Depository(SubjectOrgan oSubjectOrgan_Depository) {
        this.oSubjectOrgan_Depository = oSubjectOrgan_Depository;
    }

    public TermType getoTermType_Contract() {
        return oTermType_Contract;
    }

    public void setoTermType_Contract(TermType oTermType_Contract) {
        this.oTermType_Contract = oTermType_Contract;
    }

    public Long getnTermCount_Contract() {
        return nTermCount_Contract;
    }

    public void setnTermCount_Contract(Long nTermCount_Contract) {
        this.nTermCount_Contract = nTermCount_Contract;
    }

    public TermType getoTermType_TemporaryContract() {
        return oTermType_TemporaryContract;
    }

    public void setoTermType_TemporaryContract(TermType oTermType_TemporaryContract) {
        this.oTermType_TemporaryContract = oTermType_TemporaryContract;
    }

    public Long getnTermCount_TemporaryContract() {
        return nTermCount_TemporaryContract;
    }

    public void setnTermCount_TemporaryContract(Long nTermCount_TemporaryContract) {
        this.nTermCount_TemporaryContract = nTermCount_TemporaryContract;
    }

    public Long getnCountSubdivision() {
        return nCountSubdivision;
    }

    public void setnCountSubdivision(Long nCountSubdivision) {
        this.nCountSubdivision = nCountSubdivision;
    }

    public Long getnBranches() {
        return nBranches;
    }

    public void setnBranches(Long nBranches) {
        this.nBranches = nBranches;
    }

    public SubjectOrgan getoSubjectOrgan_Auditory() {
        return oSubjectOrgan_Auditory;
    }

    public void setoSubjectOrgan_Auditory(SubjectOrgan oSubjectOrgan_Auditory) {
        this.oSubjectOrgan_Auditory = oSubjectOrgan_Auditory;
    }

    public ActionEventAudit getoActionEventAudit() {
        return oActionEventAudit;
    }

    public void setoActionEventAudit(ActionEventAudit oActionEventAudit) {
        this.oActionEventAudit = oActionEventAudit;
    }

    public String getsKindStatut() {
        return sKindStatut;
    }

    public void setsKindStatut(String sKindStatut) {
        this.sKindStatut = sKindStatut;
    }

    public boolean isbLicence() {
        return bLicence;
    }

    public void setbLicence(boolean bLicence) {
        this.bLicence = bLicence;
    }
     
}
