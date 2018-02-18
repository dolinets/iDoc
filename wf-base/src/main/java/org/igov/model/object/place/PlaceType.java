package org.igov.model.object.place;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.igov.model.core.NamedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author dgroup
 * @since 20.07.2015
 */
@Entity
@ApiModel(description="Типі объектов административно-территориального устройства Украины (область, район, город и т.д.)")
public class PlaceType extends NamedEntity {

    @Column(name = "nOrder")
    @JsonProperty("nOrder")
    @ApiModelProperty(value = "Уникальный номер-ИД типа населенного пункта", required = true)
    private Long order;

    @Column
    @JsonProperty
    @ApiModelProperty(value = "Логический признак территориального объединения типа область/район/регион", required = true)
    private boolean bArea; // "Площадь" (true = область/район/регион и т.д.)

    @Column
    @JsonProperty
    @ApiModelProperty(value = "Логический признак корня типа административная еденица страны (области)", required = true)
    private boolean bRoot; // "Корень" (true = административная еденица страны)

    public PlaceType() {
        // no action required
    }

    public PlaceType(Long placeTypeId, String name, Long order, Boolean area, Boolean root) {
        setId(placeTypeId);
        setName(name);
        setOrder(order);
        setbArea(area);
        setbRoot(root);
    }

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public boolean isbArea() {
        return bArea;
    }

    public void setbArea(boolean bArea) {
        this.bArea = bArea;
    }

    public boolean isbRoot() {
        return bRoot;
    }

    public void setbRoot(boolean bRoot) {
        this.bRoot = bRoot;
    }
}