package org.igov.model.relation;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import org.igov.model.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@javax.persistence.Entity
@ApiModel(description="Справочник атрибутов для ObjectGroup")
public class ObjectGroupAttribute extends AbstractEntity {

    @JsonProperty(value = "oObject_Group")
    @ManyToOne(targetEntity = ObjectGroup.class)
    @JsonBackReference
    @JoinColumn(name="nID_ObjectGroup", nullable = false)
    @ApiModelProperty(value = "Уникальный номер-ИД из справочника ObjectGroup ", required = true)
    private ObjectGroup oObjectGroup;

    @JsonProperty(value = "oAttribute_Object")
    @ManyToOne(targetEntity = AttributeObject.class)
    @JoinColumn(name="nID_AttributeObject", nullable = false)
    @ApiModelProperty(value = "Уникальный номер-ИД из справочника AttributeObject ", required = true)
    private AttributeObject oAttributeObject;

    @JsonProperty(value = "sValue")
    @Column(name = "sValue", nullable = false)
    @ApiModelProperty(value = "Стоимость ", required = true)
    private String sValue;

    public ObjectGroup getoObjectGroup() {
        return oObjectGroup;
    }

    public void setoObjectGroup(ObjectGroup oObjectGroup) {
        this.oObjectGroup = oObjectGroup;
    }

    public AttributeObject getoAttributeObject() {
        return oAttributeObject;
    }

    public void setoAttributeObject(AttributeObject oAttributeObject) {
        this.oAttributeObject = oAttributeObject;
    }

    public String getsValue() {
        return sValue;
    }

    public void setsValue(String sValue) {
        this.sValue = sValue;
    }
}
