package org.igov.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import org.igov.model.core.AbstractEntity;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class DocumentStatutoryState extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "sName")
    @Column
    private String sName;

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

}
