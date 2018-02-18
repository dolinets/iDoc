package org.igov.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.igov.model.core.NamedEntity;

import javax.persistence.Column;

@javax.persistence.Entity
@ApiModel(description="Тип документа")
public class DocumentType extends NamedEntity {

    @JsonProperty("bHidden")
    @Column
    @ApiModelProperty(value = "Флаг скрытости")
    private boolean bHidden;
    
    @JsonProperty("sNote")
    @Column
    @ApiModelProperty(value = "Описание")
    private String sNote;

    public boolean isbHidden() {
        return bHidden;
    }

    public void setbHidden(boolean bHidden) {
        this.bHidden = bHidden;
    }

    public String getsNote() {
        return sNote;
    }

    public void setsNote(String sNote) {
        this.sNote = sNote;
    }
    
    
}
