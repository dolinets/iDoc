package org.igov.model.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.igov.model.core.AbstractEntity;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class PropertyInteger extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "nValue")
    @Column
    private int nValue;

    @JsonProperty(value = "oPropertyValue")
    @OneToOne(targetEntity = PropertyValue.class)
    @JoinColumn(name = "nID_PropertyValue")
    private PropertyValue oPropertyValue;

    public int getnValue() {
        return nValue;
    }

    public void setnValue(int nValue) {
        this.nValue = nValue;
    }

    public PropertyValue getoPropertyValue() {
        return oPropertyValue;
    }

    public void setoPropertyValue(PropertyValue oPropertyValue) {
        this.oPropertyValue = oPropertyValue;
    }
    
    
    
}
