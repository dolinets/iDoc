package org.igov.model.relation;

import org.igov.model.core.AbstractEntity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;


/**
 *
 * @author Kovilin
 */
@javax.persistence.Entity
@ApiModel(description="Классы взаимосвязей")
public class RelationClass extends AbstractEntity{
    
    @JsonProperty(value = "sClass")
    @Column(name = "sClass", length = 255, nullable = false)
    @ApiModelProperty(value = "Значение класса", required = true)
    private String sClass;

    public String getsClass() {
        return sClass;
    }

    public void setsClass(String sClass) {
        this.sClass = sClass;
    }

}
