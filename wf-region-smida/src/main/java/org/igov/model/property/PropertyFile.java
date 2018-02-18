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
public class PropertyFile extends AbstractEntity {

    private static final long serialVersionUID = 1L;    
    
    @JsonProperty(value = "sID_Data")
    @Column
    private String sID_Data;
    
    @JsonProperty(value = "oPropertyValue")
    @OneToOne(targetEntity = PropertyValue.class)
    @JoinColumn(name = "nID_PropertyValue")
    private PropertyValue oPropertyValue;
    
    @JsonProperty(value = "sFileName")
    @Column
    private String sFileName;
    
    @JsonProperty(value = "sExtName")
    @Column
    private String sExtName;
    
    @JsonProperty(value = "sContentType")
    @Column
    private String sContentType;

    public String getsID_Data() {
        return sID_Data;
    }

    public void setsID_Data(String sID_Data) {
        this.sID_Data = sID_Data;
    }

    public PropertyValue getoPropertyValue() {
        return oPropertyValue;
    }

    public void setoPropertyValue(PropertyValue oPropertyValue) {
        this.oPropertyValue = oPropertyValue;
    }

    public String getsFileName() {
        return sFileName;
    }

    public void setsFileName(String sFileName) {
        this.sFileName = sFileName;
    }

    public String getsExtName() {
        return sExtName;
    }

    public void setsExtName(String sExtName) {
        this.sExtName = sExtName;
    }

    public String getsContentType() {
        return sContentType;
    }

    public void setsContentType(String sContentType) {
        this.sContentType = sContentType;
    }
    
    
}
