package org.igov.model.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Abstract Entity with name.
 * <p/>
 * User: goodg_000
 * Date: 14.06.2015
 * Time: 15:07
 */
@MappedSuperclass
public abstract class NamedEntity extends AbstractEntity {

    @JsonProperty(value = "sName")
    @Column(name = "sName", nullable = false)
    @ApiModelProperty(value = "Название соответствующего объекта", required = true)

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
