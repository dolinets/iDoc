package org.igov.model.object.place;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.igov.model.core.AbstractEntity;

import javax.persistence.Column;

/**
 * @author dgroup
 * @since 20.07.2015
 */
@javax.persistence.Entity
@ApiModel(description="Подчинение объектов административно-территориального устройства Украины (городов, сел и т.д.)")
public class PlaceTree extends AbstractEntity {

    @Column(name = "nID_Place")
    @JsonProperty("nID_Place")
    @ApiModelProperty(value = "Уникальный номер-ИД объекта административно-территориального устройства (сущность Place)", required = true)
    private Long placeId;   // ИД-номер места

    @Column(name = "nID_Place_Root")
    @JsonProperty("nID_Place_Root")
    @ApiModelProperty(value = "Уникальный номер-ИД области для объекта (сущность Place)", required = true)
    private Long rootId;    // ИД-номер места-корня

    @Column(name = "nID_Place_Area")
    @JsonProperty("nID_Place_Area")
    @ApiModelProperty(value = "Уникальный номер-ИД места-площади", required = true)
    private Long areaId;    // ИД-номер места-площади

    @Column(name = "nID_Place_Parent")
    @JsonProperty("nID_Place_Parent")
    @ApiModelProperty(value = "Уникальный номер-ИД родительского объекта административно-территориального устройства Украины (сущность Place)", required = true)
    private Long parentId;  // ИД-номер места-родителя

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public Long getRootId() {
        return rootId;
    }

    public void setRootId(Long rootId) {
        this.rootId = rootId;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}