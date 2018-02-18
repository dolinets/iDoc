package org.igov.model.registry.nssmc;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import org.igov.model.core.AbstractEntity;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class RegistryNSSMC extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "sIDRegistry")
    @Column
    private String sIDRegistry;

    @JsonProperty(value = "sName")
    @Column
    private String sName;

    public String getsIDRegistry() {
        return sIDRegistry;
    }

    public void setsIDRegistry(String sIDRegistry) {
        this.sIDRegistry = sIDRegistry;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    @Override
    public String toString() {
        return "RegistryNSSMC{" + "sIDRegistry=" + sIDRegistry + ", sName=" + sName + '}';
    }
    
}
