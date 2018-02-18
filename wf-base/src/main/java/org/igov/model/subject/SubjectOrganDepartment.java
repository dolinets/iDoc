package org.igov.model.subject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.persistence.Column;
import org.igov.model.core.AbstractEntity;

/**
 * User: goodg_000
 * Date: 14.06.2015
 * Time: 14:57
 */
@javax.persistence.Entity
@ApiModel(description="Организационная структура в рамках которой будет использована электронная очередь")
public class SubjectOrganDepartment extends AbstractEntity {

    @Column
    @ApiModelProperty(value = "Название оргструктуры", required = true)
    private String sName;

    @Column
    @ApiModelProperty(value = "Уникальный номер-ИД субьекта-организации (юрлицо)", required = true)
    private Long nID_SubjectOrgan;

    @Column
    @ApiModelProperty(value = "Название группы пользователей (Activiti)", required = true)    
    private String sGroup_Activiti;

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public Long getnID_SubjectOrgan() {
        return nID_SubjectOrgan;
    }

    public void setnID_SubjectOrgan(Long nID_SubjectOrgan) {
        this.nID_SubjectOrgan = nID_SubjectOrgan;
    }

    public String getsGroup_Activiti() {
        return sGroup_Activiti;
    }

    public void setsGroup_Activiti(String sGroup_Activiti) {
        this.sGroup_Activiti = sGroup_Activiti;
    }
}
