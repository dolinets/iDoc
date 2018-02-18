package org.igov.model.escalation;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.persistence.Column;
import org.igov.model.core.AbstractEntity;

/**
 * @author NickVeremeichyk
 * @since 2015-12-05.
 */
@javax.persistence.Entity
@ApiModel(description="Статусы эскалаций")
public class EscalationStatus extends AbstractEntity {
    /**
     * sID - строка-ИД (уникальный)
     */
    @Column(name="sID")
    @JsonProperty(value="sID")
    @ApiModelProperty(value = "Уникальная строка-ИД статуса эскалации", required = true)
    private String nId;

    /**
     * sNote - строка-описание
     */
    @Column(name = "sNote")
    @JsonProperty(value="sNote")
    @ApiModelProperty(value = "Описание статуса эскалации", required = true)
    private String sNote;

    public String getnId() {
        return nId;
    }

    public void setnId(String nId) {
        this.nId = nId;
    }

    public String getsNote() {
        return sNote;
    }

    public void setsNote(String sNote) {
        this.sNote = sNote;
    }
}
