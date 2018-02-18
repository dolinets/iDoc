package org.igov.model.dictionary;

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
public class Dictionary extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "oDictionaryType")
    @ManyToOne(targetEntity = DictionaryType.class)
    @JoinColumn(name = "nID_DictionaryType")
    private DictionaryType oDictionaryType;
    
    @JsonProperty(value = "oDictionaryClass")
    @ManyToOne(targetEntity = DictionaryClass.class)
    @JoinColumn(name = "nID_DictionaryClass")
    private DictionaryClass oDictionaryClass;
    
    @JsonProperty(value = "sKey")
    @Column
    private String sKey;
    
    @JsonProperty(value = "sValue")
    @Column
    private String sValue;

    public DictionaryType getoDictionaryType() {
        return oDictionaryType;
    }

    public void setoDictionaryType(DictionaryType oDictionaryType) {
        this.oDictionaryType = oDictionaryType;
    }

    public DictionaryClass getoDictionaryClass() {
        return oDictionaryClass;
    }

    public void setoDictionaryClass(DictionaryClass oDictionaryClass) {
        this.oDictionaryClass = oDictionaryClass;
    }

    public String getsKey() {
        return sKey;
    }

    public void setsKey(String sKey) {
        this.sKey = sKey;
    }

    public String getsValue() {
        return sValue;
    }

    public void setsValue(String sValue) {
        this.sValue = sValue;
    }
    
    
    
}
