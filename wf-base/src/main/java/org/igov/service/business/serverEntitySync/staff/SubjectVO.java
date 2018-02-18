package org.igov.service.business.serverEntitySync.staff;

import org.igov.model.subject.*;

import java.util.List;

/**
 * Container for staff-related entities
 */
public class SubjectVO {

    List<SubjectHumanPositionCustom> aoSubjectHumanPositionCustoms;
    List<SubjectContactType> aoSubjectContactType;
    List<SubjectStatus> aoSubjectStatus;
    List<Subject> aoSubject;
    List<SubjectHuman> aoSubjectHuman;
    List<SubjectGroup> aoSubjectGroup;
    List<SubjectGroupTree> aoSubjectGroupTree;
    List<SubjectContact> aoSubjectContact;

    public List<SubjectHumanPositionCustom> getAoSubjectHumanPositionCustoms() {
        return aoSubjectHumanPositionCustoms;
    }

    public void setAoSubjectHumanPositionCustoms(List<SubjectHumanPositionCustom> aoSubjectHumanPositionCustoms) {
        this.aoSubjectHumanPositionCustoms = aoSubjectHumanPositionCustoms;
    }

    public List<SubjectContactType> getAoSubjectContactType() {
        return aoSubjectContactType;
    }

    public void setAoSubjectContactType(List<SubjectContactType> aoSubjectContactType) {
        this.aoSubjectContactType = aoSubjectContactType;
    }

    public List<SubjectStatus> getAoSubjectStatus() {
        return aoSubjectStatus;
    }

    public void setAoSubjectStatus(List<SubjectStatus> aoSubjectStatus) {
        this.aoSubjectStatus = aoSubjectStatus;
    }

    public List<Subject> getAoSubject() {
        return aoSubject;
    }

    public void setAoSubject(List<Subject> aoSubject) {
        this.aoSubject = aoSubject;
    }

    public List<SubjectHuman> getAoSubjectHuman() {
        return aoSubjectHuman;
    }

    public void setAoSubjectHuman(List<SubjectHuman> aoSubjectHuman) {
        this.aoSubjectHuman = aoSubjectHuman;
    }

    public List<SubjectGroup> getAoSubjectGroup() {
        return aoSubjectGroup;
    }

    public void setAoSubjectGroup(List<SubjectGroup> aoSubjectGroup) {
        this.aoSubjectGroup = aoSubjectGroup;
    }

    public List<SubjectGroupTree> getAoSubjectGroupTree() {
        return aoSubjectGroupTree;
    }

    public void setAoSubjectGroupTree(List<SubjectGroupTree> aoSubjectGroupTree) {
        this.aoSubjectGroupTree = aoSubjectGroupTree;
    }

    public List<SubjectContact> getAoSubjectContact() {
        return aoSubjectContact;
    }

    public void setAoSubjectContact(List<SubjectContact> aoSubjectContact) {
        this.aoSubjectContact = aoSubjectContact;
    }
}
