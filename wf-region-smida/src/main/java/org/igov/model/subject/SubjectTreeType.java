package org.igov.model.subject;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import org.igov.model.core.AbstractEntity;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class SubjectTreeType extends AbstractEntity {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "sName")
    @Column
    private String sName;
    
    @JsonProperty(value = "sNote")
    @Column
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
        return "SubjectTreeType{" + "sName=" + sName + ", sNote=" + sNote + '}';
    }

}
