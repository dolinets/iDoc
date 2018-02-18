package org.igov.model.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.igov.model.core.AbstractEntity;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class Property extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "oPropertyType")
    @ManyToOne(targetEntity = PropertyType.class)
    @JoinColumn(name = "nID_PropertyType")
    private PropertyType oPropertyType;
    
    @JsonProperty(value = "oPropertyGroup")
    @ManyToOne(targetEntity = PropertyGroup.class)
    @JoinColumn(name = "nID_PropertyGroup")
    private PropertyGroup oPropertyGroup;
    
    @JsonProperty(value = "bRequred")
    @Column
    private boolean bRequred;

    public PropertyType getoPropertyType() {
        return oPropertyType;
    }

    public void setoPropertyType(PropertyType oPropertyType) {
        this.oPropertyType = oPropertyType;
    }

    public PropertyGroup getoPropertyGroup() {
        return oPropertyGroup;
    }

    public void setoPropertyGroup(PropertyGroup oPropertyGroup) {
        this.oPropertyGroup = oPropertyGroup;
    }

    public boolean isbRequred() {
        return bRequred;
    }

    public void setbRequred(boolean bRequred) {
        this.bRequred = bRequred;
    }

    
}
