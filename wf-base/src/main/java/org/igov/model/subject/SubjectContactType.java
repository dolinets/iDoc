package org.igov.model.subject;

import org.igov.model.core.AbstractEntity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;

/**
 * User: goodg_000
 * Date: 27.12.2015
 * Time: 13:35
 */
@javax.persistence.Entity
@ApiModel(description="Тип контакта")
public class SubjectContactType extends AbstractEntity {

    @Column
    @ApiModelProperty(value = "Название на английском", required = false)
    private String sName_EN;

    @Column
    @ApiModelProperty(value = "Название на украинском", required = false)
    private String sName_UA;

    @Column
    @ApiModelProperty(value = "Название на русском", required = false)
    private String sName_RU;

    public String getsName_EN() {
        return sName_EN;
    }

    public void setsName_EN(String sName_EN) {
        this.sName_EN = sName_EN;
    }

    public String getsName_UA() {
        return sName_UA;
    }

    public void setsName_UA(String sName_UA) {
        this.sName_UA = sName_UA;
    }

    public String getsName_RU() {
        return sName_RU;
    }

    public void setsName_RU(String sName_RU) {
        this.sName_RU = sName_RU;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SubjectContactType)) {
            return false;
        }
        final SubjectContactType contactType = (SubjectContactType) obj;
        return getsName_EN().equals(contactType.getsName_EN());
    }

    @Override
    public int hashCode() {
        return 87 * getsName_EN().hashCode() + 11;
    }
}
