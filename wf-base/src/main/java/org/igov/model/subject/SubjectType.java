package org.igov.model.subject;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.igov.model.core.AbstractEntity;

import javax.persistence.Column;

@javax.persistence.Entity
public class SubjectType extends AbstractEntity{

    @JsonProperty(value = "sName")
    @Column
    private String sName;

    @JsonProperty(value = "sDescription")
    @Column
    private String sDescription;

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getsDescription() {
        return sDescription;
    }

    public void setsDescription(String sDescription) {
        this.sDescription = sDescription;
    }
}
