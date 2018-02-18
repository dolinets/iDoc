package org.igov.model.flow;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import java.util.List;
import org.igov.model.core.NamedEntity;

/**
 * Flow (stored in regional server) related to ServiceData (stored in central
 * server).
 * <p/>
 * User: goodg_000 Date: 14.06.2015 Time: 15:03
 */
@javax.persistence.Entity
@ApiModel(description="Потоки электронных очередей")
public class Flow extends NamedEntity { 

    /**
     * One-to-one soft reference to ServiceData which is stored in central but
     * not present in regional server.
     */
    @Column
    @ApiModelProperty(value = "Уникальный номер-ИД конфигурации/настройки сервиса для области/города", required = true)
    private Long nID_ServiceData;

    /**
     * Many-to-one soft reference to SubjectOrganDepartment which is stored in
     * central but not present in regional server.
     */
    @Column
    @ApiModelProperty(value = "Уникальный номер-ИД организационной структуры", required = true)
    private Long nID_SubjectOrganDepartment;

    /**
     * ID of business process definition without version.
     */
    @Column
    @ApiModelProperty(value = "Уникальная строка-ИД  бизнес-процесса в котором будет использоваться данная электронная очередь", required = true)
    private String sID_BP;
    
    @Column
    @ApiModelProperty(value = "Название группы, в которой определены исключения календарных дат из автогенерации", required = true)
    private String sGroup;
    
    @Column
    @ApiModelProperty(value = "Количество календарных дней для которых будет осуществляться автогенерации очереди. Если значение указано NULL, то слоты очереди не автогенерируются", required = true)
    private Long nCountAutoGenerate;

    @OneToMany(mappedBy = "oFlow_ServiceData", cascade = CascadeType.ALL, orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<FlowProperty> flowProperties;

    public Long getnID_ServiceData() {
        return nID_ServiceData;
    }

    public void setnID_ServiceData(Long nID_ServiceData) {
        this.nID_ServiceData = nID_ServiceData;
    }

    public Long getnID_SubjectOrganDepartment() {
        return nID_SubjectOrganDepartment;
    }

    public void setnID_SubjectOrganDepartment(Long nID_SubjectOrganDepartment) {
        this.nID_SubjectOrganDepartment = nID_SubjectOrganDepartment;
    }

    public String getsID_BP() {
        return sID_BP;
    }

    public void setsID_BP(String sID_BP) {
        this.sID_BP = sID_BP;
    }

    public Long getnCountAutoGenerate() {
        return nCountAutoGenerate;
    }

    public void setnCountAutoGenerate(Long nCountAutoGenerate) {
        this.nCountAutoGenerate = nCountAutoGenerate;
    }

    public List<FlowProperty> getFlowProperties() {
        return flowProperties;
    }

    public void setFlowProperties(List<FlowProperty> flowProperties) {
        this.flowProperties = flowProperties;
    }

    public String getsGroup() {
        return sGroup;
    }

    public void setsGroup(String sGroup) {
        this.sGroup = sGroup;
    }
    
}
