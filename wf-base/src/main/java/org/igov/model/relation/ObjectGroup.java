package org.igov.model.relation;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.persistence.CascadeType;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.igov.model.core.AbstractEntity;
import org.igov.model.subject.Subject;

/**
 *
 * @author Kovilin
 */
@javax.persistence.Entity
@ApiModel(description="Справочник товаров для различных компаний и организаций")
public class ObjectGroup extends AbstractEntity{
    
    /*@JsonProperty(value = "nID_Subject_Source")
    @Column(name = "nID_Subject_Source", nullable = true)
    private Long nID_Subject_Source;*/
    
    @JsonProperty(value = "oSubject_Source")
    @ManyToOne(targetEntity = Subject.class)
    @JoinColumn(name="nID_Subject_Source", nullable = true, updatable = false)
    @ApiModelProperty(value = "Уникальный номер-ИД предприятия для которого используется справочник", required = true)
    private Subject oSubject_Source;
    
    @JsonProperty(value = "sID_Private_Source")
    @Column(name = "sID_Private_Source", length = 255, nullable = false)
    @ApiModelProperty(value = "Уникальная строка-ИД код товара для предприятия", required = true)
    private String sID_Private_Source;
    
    @JsonProperty(value = "sName")
    @Column(name = "sName", length = 5000, nullable = false)
    @ApiModelProperty(value = "Название товара или услуги", required = true)
    private String sName;
    
    @JsonProperty(value = "aObjectGroupAttribute")
    @OneToMany(targetEntity = ObjectGroupAttribute.class, mappedBy = "oObjectGroup",
            cascade = CascadeType.ALL) 
    @JsonManagedReference
    @LazyCollection(LazyCollectionOption.FALSE)
    @ApiModelProperty(value = "Массив аттрибутов", required = true)
    private List<ObjectGroupAttribute> aObjectGroupAttribute;

    public List<ObjectGroupAttribute> getaObjectGroupAttribute() {
        return aObjectGroupAttribute;
    }

    public void setaObjectGroupAttribute(List<ObjectGroupAttribute> aObjectGroupAttribute) {
        this.aObjectGroupAttribute = aObjectGroupAttribute;
    }
    
    public void setoSubject_Source(Subject oSubject_Source) {
        this.oSubject_Source = oSubject_Source;
    }

    public Subject getoSubject_Source() {
        return oSubject_Source;
    }

    public void setsID_Private_Source(String sID_Private_Source) {
        this.sID_Private_Source = sID_Private_Source;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }
    
    /*public void setnID_Subject_Source(Long nID_Subject_Source) {
        this.nID_Subject_Source = nID_Subject_Source;
    }
    
    public Long getnID_Subject_Source() {
        return nID_Subject_Source;
    }*/

    public String getsID_Private_Source() {
        return sID_Private_Source;
    }

    public String getsName() {
        return sName;
    }
    
}
