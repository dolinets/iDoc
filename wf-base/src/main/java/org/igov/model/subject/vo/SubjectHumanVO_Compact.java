package org.igov.model.subject.vo;

import org.igov.model.subject.SubjectGroup;
import org.igov.model.subject.SubjectHuman;

import java.util.List;
import java.util.Map;

public class SubjectHumanVO_Compact {

    private SubjectHuman oSubjectHuman;
    private String sLogin;
    private boolean bHead;

    public SubjectHumanVO_Compact() {

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

    public boolean isbHead() {
        return bHead;
    }

    public void setbHead(boolean bHead) {
        this.bHead = bHead;
    }
}
