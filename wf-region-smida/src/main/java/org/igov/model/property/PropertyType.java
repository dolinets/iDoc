package org.igov.model.property;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.igov.model.core.AbstractEntity;
import org.igov.model.dictionary.DictionaryType;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class PropertyType extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "oDictonaryType")
    @ManyToOne(targetEntity = DictionaryType.class)
    @JoinColumn(name = "nID_DictonaryType")
    private DictionaryType oDictonaryType;
    
    @JsonProperty(value = "oPropertyValueType")
    @ManyToOne(targetEntity = PropertyValueType.class)
    @JoinColumn(name = "nID_PropertyValueType")
    private PropertyValueType oPropertyValueType;
    
    @JsonProperty(value = "sName")
    @Column
    private String sName;

    public DictionaryType getoDictonaryType() {
        return oDictonaryType;
    }

    public void setoDictonaryType(DictionaryType oDictonaryType) {
        this.oDictonaryType = oDictonaryType;
    }

    public PropertyValueType getoPropertyValueType() {
        return oPropertyValueType;
    }

    public void setoPropertyValueType(PropertyValueType oPropertyValueType) {
        this.oPropertyValueType = oPropertyValueType;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }
    
    
    
}
