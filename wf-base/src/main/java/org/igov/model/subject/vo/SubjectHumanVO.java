package org.igov.model.subject.vo;

import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectHuman;

import java.util.List;
import java.util.Map;

public class SubjectHumanVO {

    private SubjectGroup oSubjectGroup;
    private SubjectGroup oSubjectGroupHead;
    private List<SubjectGroup> aSubjectGroupTreeUp;
    private List<Map<String, String>> mUserGroupMember;
    private SubjectHuman oSubjectHuman;
    private String sLogin;
    private boolean bHead;

    public SubjectHumanVO() {

    }

    public SubjectGroup getoSubjectGroup() {
        return oSubjectGroup;
    }

    public void setoSubjectGroup(SubjectGroup oSubjectGroup) {
        this.oSubjectGroup = oSubjectGroup;
    }

    public SubjectHuman getoSubjectHuman() {
        return oSubjectHuman;
    }

    public void setoSubjectHuman(SubjectHuman oSubjectHuman) {
        this.oSubjectHuman = oSubjectHuman;
    }

    public String getsLogin() {
        return sLogin;
    }

    public void setsLogin(String sLogin) {
        this.sLogin = sLogin;
    }

    public SubjectGroup getoSubjectGroupHead() {
        return oSubjectGroupHead;
    }

    public void setoSubjectGroupHead(SubjectGroup oSubjectGroupHead) {
        this.oSubjectGroupHead = oSubjectGroupHead;
    }

    public List<SubjectGroup> getaSubjectGroupTreeUp() {
        return aSubjectGroupTreeUp;
    }

    public void setaSubjectGroupTreeUp(List<SubjectGroup> aSubjectGroupTreeUp) {
        this.aSubjectGroupTreeUp = aSubjectGroupTreeUp;
    }

    public List<Map<String, String>> getmUserGroupMember() {
        return mUserGroupMember;
    }

    public void setmUserGroupMember(List<Map<String, String>> mUserGroupMember) {
        this.mUserGroupMember = mUserGroupMember;
    }

    public boolean isbHead() {
        return bHead;
    }

    public void setbHead(boolean bHead) {
        this.bHead = bHead;
    }
}
