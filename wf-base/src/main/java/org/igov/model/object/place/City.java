package org.igov.model.object.place;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.igov.model.core.NamedEntity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * User: goodg_000 Date: 04.05.2015 Time: 21:50
 */
@javax.persistence.Entity
@ApiModel(description="Объекты административно-территориального устройства Украины (городов, сел и т.д.)")
public class City extends NamedEntity {

    @JsonProperty(value = "nID_Region")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_Region", insertable = false, updatable = false, nullable = false)
    @ApiModelProperty(value = "Уникальный номер-ИД региона Украины", required = true)
    private Region region;

    // ИД-строка кода классификатора КОАТУУ
    @Column
    @ApiModelProperty(value = "Код объекта административно-территориального устройства Украины (КОАТУУ)", required = true)
    private String sID_UA;

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public String getsID_UA() {
        return sID_UA;
    }

    public void setsID_UA(String sID_UA) {
        this.sID_UA = sID_UA;
    }
}
