package org.igov.model.registry.nssmc;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import org.igov.model.core.AbstractEntity;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class TableFieldPublic extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "bPublic")
    @Column
    private boolean bPublic;

    @JsonProperty(value = "sPath")
    @Column
    private String sPath;

    @JsonProperty(value = "sContext")
    @Column
    private String sContext;

    public boolean isbPublic() {
        return bPublic;
    }

    public void setbPublic(boolean bPublic) {
        this.bPublic = bPublic;
    }

    public String getsPath() {
        return sPath;
    }

    public void setsPath(String sPath) {
        this.sPath = sPath;
    }

    public String getsContext() {
        return sContext;
    }

    public void setsContext(String sContext) {
        this.sContext = sContext;
    }

    @Override
    public String toString() {
        return "TableFieldPublic{" + "bPublic=" + bPublic 
                + ", sPath=" + sPath + ", sContext=" + sContext + '}';
    }
    
    
    
}
