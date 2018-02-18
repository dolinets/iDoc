package org.igov.model.action.vo;

import org.igov.model.relation.AttributeObject;

/**
 *
 * @author Kovylin
 */
public class Attribute_VO {
    
    public Attribute_VO() {
    
    }
    
    private AttributeObject oAttributeObject;
    
    private String sValue;

    public AttributeObject getoAttributeObject() {
        return oAttributeObject;
    }

    public String getsValue() {
        return sValue;
    }

    public void setoAttributeObject(AttributeObject oAttributeObject) {
        this.oAttributeObject = oAttributeObject;
    }

    public void setsValue(String sValue) {
        this.sValue = sValue;
    }
}
