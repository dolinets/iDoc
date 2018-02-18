package org.igov.model.subject.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.igov.model.subject.Subject;
import org.igov.model.subject.SubjectContact;
import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectGroupTree;
import org.igov.model.subject.SubjectHuman;
import org.igov.model.subject.SubjectHumanPositionCustom;
import org.igov.model.subject.organ.SubjectOrgan;

/**
 *
 * @author Kovylin
 */
public class StaffVO {

    public StaffVO(){
        
    }
    @JsonProperty(value = "oSubject")
    private Subject oSubject;
    
    @JsonProperty(value = "oSubjectHuman")
    private SubjectHuman oSubjectHuman;
    
    @JsonProperty(value = "oSubjectOrgan")
    private SubjectOrgan oSubjectOrgan;
    
    @JsonProperty(value = "oSubjectHumanPositionCuston")
    private SubjectHumanPositionCustom oSubjectHumanPositionCuston;
    
    @JsonProperty(value = "oSubjectGroup")
    private SubjectGroup oSubjectGroup;
    
    @JsonProperty(value = "aSubjectContact")
    private List<SubjectContact> aSubjectContact; 
    
    @JsonProperty(value = "aSubjectGroupTree")
    private List<SubjectGroupTree> aSubjectGroupTree;

    public Subject getoSubject() {
        return oSubject;
    }

    public SubjectHuman getoSubjectHuman() {
        return oSubjectHuman;
    }

    public SubjectOrgan getoSubjectOrgan() {
        return oSubjectOrgan;
    }

    public SubjectHumanPositionCustom getoSubjectHumanPositionCuston() {
        return oSubjectHumanPositionCuston;
    }

    public SubjectGroup getoSubjectGroup() {
        return oSubjectGroup;
    }

    public List<SubjectContact> getaSubjectContact() {
        return aSubjectContact;
    }

    public List<SubjectGroupTree> getaSubjectGroupTree() {
        return aSubjectGroupTree;
    }

    public void setoSubject(Subject oSubject) {
        this.oSubject = oSubject;
    }

    public void setoSubjectHuman(SubjectHuman oSubjectHuman) {
        this.oSubjectHuman = oSubjectHuman;
    }

    public void setoSubjectOrgan(SubjectOrgan oSubjectOrgan) {
        this.oSubjectOrgan = oSubjectOrgan;
    }

    public void setoSubjectHumanPositionCuston(SubjectHumanPositionCustom oSubjectHumanPositionCuston) {
        this.oSubjectHumanPositionCuston = oSubjectHumanPositionCuston;
    }

    public void setoSubjectGroup(SubjectGroup oSubjectGroup) {
        this.oSubjectGroup = oSubjectGroup;
    }

    public void setaSubjectContact(List<SubjectContact> aSubjectContact) {
        this.aSubjectContact = aSubjectContact;
    }

    public void setaSubjectGroupTree(List<SubjectGroupTree> aSubjectGroupTree) {
        this.aSubjectGroupTree = aSubjectGroupTree;
    }
    
}
