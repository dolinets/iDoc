package org.igov.model.subject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.persistence.Column;

import org.igov.model.core.AbstractEntity;

@javax.persistence.Entity
@ApiModel(description="Тип акаунта субъекта")
public class SubjectAccountType extends AbstractEntity {
    
    @Column
    @ApiModelProperty(value = "Строка-ИД записи, уникально", required = false)
    private String sID;
    
    @Column
    @ApiModelProperty(value = "Описание", required = false)
    private String sNote;
   

    public String getsNote() {
        return sNote;
    }

    public void setsNote(String sNote) {
        this.sNote = sNote;
    }

    public String getsID() {
	return sID;
    }

    public void setsID(String sID) {
	this.sID = sID;
    }

}
