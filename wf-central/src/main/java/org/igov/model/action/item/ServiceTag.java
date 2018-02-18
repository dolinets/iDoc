package org.igov.model.action.item;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.igov.model.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import static org.igov.io.fs.FileSystemData.getSmartPathFileContent_ActionItem;

/**
 * Tag of Service
 * User: goodg_000
 * Date: 19.06.2016
 * Time: 20:27
 */
@javax.persistence.Entity
@ApiModel(description="Теги для сервисов")
public class ServiceTag extends AbstractEntity {

    @Column
    @ApiModelProperty(value = "Уникальная строка-ИД тега", required = true)
    private String sID;

    @Column
    @ApiModelProperty(value = "Название тега на украинском языке", required = true)
    private String sName_UA;

    @Column
    @ApiModelProperty(value = "Название тега на русском языке", required = true)
    private String sName_RU;
    
    @Column
    @ApiModelProperty(value = "Определяет порядок сортировки. Чем меньше значение, тем более приоритетный тег ", required = true)
    private Long nOrder;
            
    @Column
    @ApiModelProperty(value = "Содержит относительный путь к картинке, связанной с тегом", required = true)
    private String sLinkImage;
    
    @Column
    @ApiModelProperty(value = "Признак наличия описания тега. При наличии описания атрибут принимает значение [*]. При этом должен быть добавлен файл с описанием nID.html по пути /i/wf-central/src/main/resources/patterns/services/Tag", required = true)
    private String sNote;

    @Column
    @ApiModelProperty(value = "Уникальный номер-ИД объекта административно-территориального устройства Украины (сущность Place)", required = true)
    private Long nID_Place;

    @ManyToOne(targetEntity = ServiceTagType.class)
    @JoinColumn(name="nID_ServiceTagType", nullable = false, updatable = false)
    @ApiModelProperty(value = "Уникальный номер-ИД типа тега (сущность ServiceTagType)", required = true)
    private ServiceTagType serviceTagType;

    public String getsID() {
        return sID;
    }
    public void setsID(String sID) {
        this.sID = sID;
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

    public ServiceTagType getServiceTagType() {
        return serviceTagType;
    }
    public void setServiceTagType(ServiceTagType serviceTagType) {
        this.serviceTagType = serviceTagType;
    }

    public Long getnOrder() {
        return nOrder;
    }

    public void setnOrder(Long nOrder) {
        this.nOrder = nOrder;
    }

    public String getsLinkImage() {
        return sLinkImage;
    }

    public void setsLinkImage(String sLinkImage) {
        this.sLinkImage = sLinkImage;
    }

    public String getsNote() {
        //return sNote;
        return getSmartPathFileContent_ActionItem(sNote, "Tag", getId());
    }
    
    public void setsNote(String sNote) {
        this.sNote = sNote;
    }

    public Long getnID_Place() {
        return nID_Place;
    }

    public void setnID_Place(Long nID_Place) {
        this.nID_Place = nID_Place;
    }
}
