package org.igov.model.subject;

import org.igov.model.core.AbstractEntity;

import javax.persistence.Column;

@javax.persistence.Entity
public class SubjectRightRegistry extends AbstractEntity {

    @Column(nullable = false)
    private String sID_Registry;

    @Column(nullable = false)
    private String sLogin;

    public SubjectRightRegistry() {

    }

    public String getsID_Registry() {
        return sID_Registry;
    }

    public void setsID_Registry(String sID_Registry) {
        this.sID_Registry = sID_Registry;
    }

    public String getsLogin() {
        return sLogin;
    }

    public void setsLogin(String sLogin) {
        this.sLogin = sLogin;
    }

}
