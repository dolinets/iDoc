package org.igov.model.action.item;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.igov.model.access.AccessServiceRole;
import org.igov.model.core.AbstractEntity;
import org.igov.model.core.Entity;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Tag of Service
 * User: goodg_000
 * Date: 19.06.2016
 * Time: 20:27
 */
@javax.persistence.Entity
@ApiModel(description="Взаимосвязи сервисов с тегами")
public class ServiceTagLink extends AbstractEntity {

    @ManyToOne(targetEntity = Service.class)
    @JoinColumn(name="nID_Service", nullable = false, updatable = false)
    @ApiModelProperty(value = "Уникальный номер-ИД сервиса", required = true)
    private Service oService;

    @ManyToOne(targetEntity = ServiceTag.class)
    @JoinColumn(name="nID_ServiceTag", nullable = false, updatable = false)
    @ApiModelProperty(value = "Уникальный номер-ИД тега", required = true)
    private ServiceTag oServiceTag;
    

    public Service getService() {
        return oService;
    }
    public void setService(Service oService) {
        this.oService = oService;
    }
    
    public ServiceTag getServiceTag() {
        return oServiceTag;
    }
    public void setServiceTag(ServiceTag oServiceTag) {
        this.oServiceTag = oServiceTag;
    }
    
}
