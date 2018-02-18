package org.igov.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import org.igov.model.core.AbstractEntity;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class TermType extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "sName")
    @Column(name = "sName")
    private String sName;

    @JsonProperty(value = "sNote")
    @Column(name = "sNote")
    private String sNote;

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getsNote() {
        return sNote;
    }

    public void setsNote(String sNote) {
        this.sNote = sNote;
    }

    @Override
    public String toString() {
        return "TermType{" + "sName=" + sName + ", sNote=" + sNote + '}';
    }

}
