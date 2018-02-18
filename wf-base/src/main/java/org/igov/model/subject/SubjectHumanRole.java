/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.igov.model.subject;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.igov.model.core.NamedEntity;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author HS
 */
@javax.persistence.Entity
@ApiModel(description="Роль субъекта-сотрудника(???)")
public class SubjectHumanRole extends NamedEntity {

    @ManyToMany(fetch = FetchType.EAGER, targetEntity = SubjectHuman.class)
    @JoinTable(name = "SubjectHumanRole_SubjectHuman",
            joinColumns = @JoinColumn(name = "nID_SubjectHumanRole"),
            inverseJoinColumns = @JoinColumn(name = "nID_SubjectHuman"))
    @JsonBackReference
    @ApiModelProperty(value = "Массив субьектов-людей", required = true)
    private List<SubjectHuman> aSubjectHuman = new ArrayList<>();

    public List<SubjectHuman> getaSubjectHuman() {
        return aSubjectHuman;
    }

    public void setaSubjectHuman(List<SubjectHuman> aSubjectHuman) {
        this.aSubjectHuman = aSubjectHuman;
    }

}
