package org.igov.model.flow;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.igov.model.core.AbstractEntity;

/**
 * Created by Богдан on 23.10.2015.
 */

@javax.persistence.Entity
@ApiModel(description="Связь электронных очередей с сервисами и организационными структурами в рамках которых будет использована электронная очередь")
public class FlowLink extends AbstractEntity {

    @ManyToOne(targetEntity = Flow.class)
    @JoinColumn(name = "nID_Flow")
    @ApiModelProperty(value = "Уникальный номер-ИД потока электронных очередей", required = true)
    private Flow flow_ServiceData;

    @Column
    @ApiModelProperty(value = "Уникальный номер-ИД зарегистрированного сервиса (услуг)", required = true)
    private Long nID_Service;
    
    @Column
    @ApiModelProperty(value = "Уникальный номер-ИД организационной структуры в рамках которой будет использована электронная очередь", required = true)
    private Long nID_SubjectOrganDepartment;
	
    public Flow getFlow_ServiceData() {
        return flow_ServiceData;
    }

    public void setFlow_ServiceData(Flow flow_ServiceData) {
        this.flow_ServiceData = flow_ServiceData;
    }

    public Long getnID_Service() {
        return nID_Service;
    }

    public void setnID_Service(Long nID_Service) {
        this.nID_Service = nID_Service;
    }

	public Long getnID_SubjectOrganDepartment() {
		return nID_SubjectOrganDepartment;
	}

	public void setnID_SubjectOrganDepartment(Long nID_SubjectOrganDepartment) {
		this.nID_SubjectOrganDepartment = nID_SubjectOrganDepartment;
	}
    }
