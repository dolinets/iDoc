package org.igov.model.relation;

import org.igov.model.core.AbstractEntity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.igov.model.subject.Subject;

/**
 *
 * @author Kovilin
 */
@javax.persistence.Entity
@ApiModel(description="Справочник источников")
public class ObjectItem extends AbstractEntity{
    
    /*@JsonProperty(value = "nID_Subject_Source")
    @Column(name = "nID_Subject_Source", nullable = true)
    private Long nID_Subject_Source;*/
    
    @JsonProperty(value = "oSubject_Source")
    @ManyToOne(targetEntity = Subject.class)
    @JoinColumn(name="nID_Subject_Source", nullable = true, updatable = false)
    @ApiModelProperty(value = "Уникальный номер-ИД источника", required = false)
    private Subject oSubject_Source;
    
    @JsonProperty(value = "sID_Private_Source")
    @Column(name = "sID_Private_Source", length = 255, nullable = true)
    @ApiModelProperty(value = "Уникальная строка-ИД источника ", required = false)
    private String sID_Private_Source;

    @JsonProperty(value = "sName")
    @Column(name = "sName", length = 5000, nullable = false)
    @ApiModelProperty(value = "Название источника ", required = true)
    private String sName;

    public Subject getoSubject_Source() {
        return oSubject_Source;
    }

    public void setoSubject_Source(Subject oSubject_Source) {
        this.oSubject_Source = oSubject_Source;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }
    
   

    public String getsID_Private_Source() {
        return sID_Private_Source;
    }
    
    /*public Long getnID_Subject_Source() {
        return nID_Subject_Source;
    }
    
    public void setnID_Subject_Source(Long nID_Subject_Source) {
        this.nID_Subject_Source = nID_Subject_Source;
    }*/

    public void setsID_Private_Source(String sID_Private_Source) {
        this.sID_Private_Source = sID_Private_Source;
    }
    
}
