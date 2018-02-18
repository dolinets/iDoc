package org.igov.model.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.igov.model.core.AbstractEntity;
import org.igov.model.core.EntityItem;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class PropertyValue extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oEntityItem")
    @Enumerated(EnumType.ORDINAL)
    private EntityItem oEntityItem;

    @JsonProperty(value = "oProperty")
    @ManyToOne(targetEntity = Property.class)
    @JoinColumn(name = "nID_Property")
    private Property oProperty;

    public EntityItem getoEntityItem() {
        return oEntityItem;
    }

    public void setoEntityItem(EntityItem oEntityItem) {
        this.oEntityItem = oEntityItem;
    }

    public Property getoProperty() {
        return oProperty;
    }

    public void setoProperty(Property oProperty) {
        this.oProperty = oProperty;
    }
   
}
