package org.igov.model.process.processLink;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import org.igov.model.core.AbstractEntity;

/**
 *
 * @author idenysenko
 */
@javax.persistence.Entity
public class ProcessLink_Type extends AbstractEntity {
    
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
        return "ProcessLink_Type{" 
                + "id=" + getId()
                + ", sName=" + sName
                + ", sNote=" + sNote
                + '}';
    }
    
}
