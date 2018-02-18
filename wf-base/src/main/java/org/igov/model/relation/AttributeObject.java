package org.igov.model.relation;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import org.igov.model.core.NamedEntity;

import javax.persistence.Column;

@javax.persistence.Entity
@ApiModel(description="Справочник атрибутов объектов")
public class AttributeObject extends NamedEntity {

    @JsonProperty(value = "sNote")
    @Column(name = "sNote")
    @ApiModelProperty(value = "Значение ", required = true)
    private String sNote;

    public String getsNote() {
        return sNote;
    }

    public void setsNote(String sNote) {
        this.sNote = sNote;
    }
}
