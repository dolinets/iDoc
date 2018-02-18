package org.igov.model.subject;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.persistence.Column;
import org.igov.model.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author alex
 */
@javax.persistence.Entity
@ApiModel(description="Статус субьекта(???)")
public class SubjectStatus extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "sName")
    @Column
    @ApiModelProperty(value = "Название")
    private String sName;

    @ApiModelProperty(value = "Описание")
    @JsonProperty(value = "sNote")
    @Column
    private String sNote;

    @JsonProperty(value = "oSubjectType")
    @ManyToOne(targetEntity = SubjectType.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_SubjectType")
    private SubjectType oSubjectType;

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getsNote() {
        return sNote;
    }

    public void setsNote(String sNote) {
        this.sNote = sNote;
    }

    public SubjectType getoSubjectType() {
        return oSubjectType;
    }

    public void setoSubjectType(SubjectType oSubjectType) {
        this.oSubjectType = oSubjectType;
    }
}
