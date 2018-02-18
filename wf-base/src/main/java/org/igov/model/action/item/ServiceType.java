package org.igov.model.action.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.igov.model.core.AbstractEntity;

import javax.persistence.Column;

/**
 * User: goodg_000 Date: 04.05.2015 Time: 22:58
 */
@javax.persistence.Entity
@ApiModel(description="Типы сервисов (внешняя, встроенная, авторизация...)")
public class ServiceType extends AbstractEntity {

    @JsonProperty(value = "sName")
    @Column(name = "sName", nullable = false)
    @ApiModelProperty(value = "Название типа сервиса", required = false)
    private String name;

    @JsonProperty(value = "sNote")
    @Column(name = "sNote", nullable = false)
    @ApiModelProperty(value = "Полное описание типа сервиса", required = false)
    private String note;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
