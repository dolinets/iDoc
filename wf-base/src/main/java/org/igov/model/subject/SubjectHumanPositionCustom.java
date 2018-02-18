/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.model.subject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Entity;
import org.igov.model.core.NamedEntity;

/**
 *
 * @author olga
 */
@Entity
@ApiModel(description="Должности субъекта")
public class SubjectHumanPositionCustom extends NamedEntity{
    private final static Gson oGson = new Gson();
    @JsonProperty(value = "sNote")
    @ApiModelProperty(value = "Описание", required = true)
    private String sNote;

    public String getsNote() {
        return sNote;
    }

    public void setsNote(String sNote) {
        this.sNote = sNote;
    }
    
    public String toJSONString() {
	return oGson.toJson(this);
    }
}
