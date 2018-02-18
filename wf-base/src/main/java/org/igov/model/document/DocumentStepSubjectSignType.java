package org.igov.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.persistence.Entity;
import org.igov.model.core.AbstractEntity;


/**
 *
 * @author Oleksandr Belichenko
 */
@Entity
@ApiModel(description="Тип подписи субьекта-подписанта шага документа")
public class DocumentStepSubjectSignType extends AbstractEntity {
    
    @JsonProperty(value = "sID")
    @ApiModelProperty(value = "Строка-ИД записи, уникально", required = true)
    private String sID;
    
    @JsonProperty(value = "sName")
    @ApiModelProperty(value = "Название", required = true)
    private String sName;
    
    public String getsID() {
        return sID;
    }

    public void setsID(String sID) {
        this.sID = sID;
    }
    
    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }
    
}
