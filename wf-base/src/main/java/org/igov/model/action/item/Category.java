package org.igov.model.action.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.List;

/**
 * User: goodg_000 Date: 04.05.2015 Time: 22:10
 */
@javax.persistence.Entity
@ApiModel(description="Категории, которым предоставляется услуга (Громадянам, Організаціям...)")
public class Category extends org.igov.model.core.NamedEntity {

    @JsonProperty(value = "sID")
    @Column(name = "sID", nullable = false)
    @ApiModelProperty(value = "Уникальная строка-ИД категории", required = true)
    private String code;

    @JsonProperty(value = "nOrder")
    @Column(name = "nOrder", nullable = false)
    @ApiModelProperty(value = "Порядок сортировки для категорий", required = true)
    private Integer order;

    @JsonProperty(value = "aSubcategory")
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    @OrderBy("order asc")
    private List<Subcategory> subcategories;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public List<Subcategory> getSubcategories() {
        return subcategories;
    }

    public void setSubcategories(List<Subcategory> subcategories) {
        this.subcategories = subcategories;
    }
}
