/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.igov.model.core.NamedEntity;

import javax.persistence.Entity;

/**
 *
 * @author olga
 */
@Entity
@ApiModel(description="Тип шага документа")
public class DocumentStepType extends NamedEntity{
    
    @JsonProperty(value = "sNote")
    @ApiModelProperty(value = "Описание")
    private String sNote;
    
    @JsonProperty(value = "sSing")
    @ApiModelProperty(value = "Строка-подпись(???)")
    private String sSing;
    
    @JsonProperty(value = "bFolder")
    @ApiModelProperty(value = "Флаг папки(???)")
    private Boolean bFolder;
    
    public String getsNote() {
        return sNote;
    }

    public void setsNote(String sNote) {
        this.sNote = sNote;
    }
    
    public String getsSing() {
        return sSing;
    }

    public void setsSing(String sSing) {
        this.sSing = sSing;
    }

    public Boolean getbFolder() {
        return bFolder;
    }

    public void setbFolder(Boolean bFolder) {
        this.bFolder = bFolder;
    }

}
