package org.igov.model.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import org.hibernate.annotations.Type;
import org.igov.model.core.AbstractEntity;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class PropertyDate extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "oValue")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime oValue;

    @JsonProperty(value = "oPropertyValue")
    @OneToOne(targetEntity = PropertyValue.class)
    @JoinColumn(name = "nID_PropertyValue")
    private PropertyValue oPropertyValue;

    public DateTime getoValue() {
        return oValue;
    }

    public void setoValue(DateTime oValue) {
        this.oValue = oValue;
    }

    public PropertyValue getoPropertyValue() {
        return oPropertyValue;
    }

    public void setoPropertyValue(PropertyValue oPropertyValue) {
        this.oPropertyValue = oPropertyValue;
    }
    
}
