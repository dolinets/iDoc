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
public class PropertyStringShort extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "sValue")
    @Column
    private String sValue;

    @JsonProperty(value = "oPropertyValue")
    @OneToOne(targetEntity = PropertyValue.class)
    @JoinColumn(name = "nID_PropertyValue")
    private PropertyValue oPropertyValue;

    public String getsValue() {
        return sValue;
    }

    public void setsValue(String sValue) {
        this.sValue = sValue;
    }

    public PropertyValue getoPropertyValue() {
        return oPropertyValue;
    }

    public void setoPropertyValue(PropertyValue oPropertyValue) {
        this.oPropertyValue = oPropertyValue;
    }
    
    

}
