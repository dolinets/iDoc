package org.igov.model.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.igov.model.core.AbstractEntity;

/**
 * The property of flow. Stored in regional server.
 * <p/>
 * User: goodg_000
 * Date: 14.06.2015
 * Time: 15:18
 */
@javax.persistence.Entity
@ApiModel(description="Настройка потоков электронных очередей")
public class FlowProperty extends AbstractEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @ApiModelProperty(value = "Уникальный номер-ИД потока электронной очереди", required = true)
    @JoinColumn(name = "nID_Flow")
    private Flow oFlow_ServiceData;

    @JsonProperty(value = "nID_FlowPropertyClass")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_FlowPropertyClass")
    @ApiModelProperty(value = "Уникальный номер-ИД класса потока электронных очередей", required = true)
    private FlowPropertyClass oFlowPropertyClass;

    @Column
    @ApiModelProperty(value = "Правило генерации слотов", required = true)
    private String sData;
    
    @Column(nullable = true)
    @ApiModelProperty(value = "Признак исключения", required = true)
    private Boolean bExclude;
    
    @Column(nullable = true)
    @ApiModelProperty(value = "Название правила", required = true)
    private String sName;
    
    @Column(nullable = true)
    @ApiModelProperty(value = "Диапазон времени", required = true)
    private String sRegionTime;
    
    @Column(nullable = true)
    @ApiModelProperty(value = "дни недели для применения правила", required = true)
    private String saRegionWeekDay;
    
    @Column(nullable = true)
    @ApiModelProperty(value = "Дата-время начала периода функционирования правила", required = true)
    private String sDateTimeAt;
    
    @Column(nullable = true)
    @ApiModelProperty(value = "Дата-время окончания периода функционирования правила", required = true)
    private String sDateTimeTo;
    
    @Column
    @ApiModelProperty(value = "Название группы в рамкаах которой осуществляется автогенерация слотов очереди", required = true)
    private String sGroup;
        
    @JsonProperty(value = "nLen")
    @Column(nullable = true)
    @ApiModelProperty(value = "Длительность слота", required = true)
    private Integer nLen;

    @JsonProperty(value = "sLenType")
    @Column(nullable = true)
    @ApiModelProperty(value = "Единицы измерения длительности слота", required = true)
    private String sLenType;

    public Flow getoFlow_ServiceData() {
        return oFlow_ServiceData;
    }

    public void setoFlow_ServiceData(Flow oFlow_ServiceData) {
        this.oFlow_ServiceData = oFlow_ServiceData;
    }

    public FlowPropertyClass getoFlowPropertyClass() {
        return oFlowPropertyClass;
    }

    public void setoFlowPropertyClass(FlowPropertyClass oFlowPropertyClass) {
        this.oFlowPropertyClass = oFlowPropertyClass;
    }

    public String getsData() {
        return sData;
    }

    public void setsData(String sData) {
        this.sData = sData;
    }

    public Boolean getbExclude() {
        return bExclude;
    }

    public void setbExclude(Boolean bExclude) {
        this.bExclude = bExclude;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getsRegionTime() {
        return sRegionTime;
    }

    public void setsRegionTime(String sRegionTime) {
        this.sRegionTime = sRegionTime;
    }

    public String getSaRegionWeekDay() {
        return saRegionWeekDay;
    }

    public void setSaRegionWeekDay(String saRegionWeekDay) {
        this.saRegionWeekDay = saRegionWeekDay;
    }

    @JsonProperty(value = "nLen")
    public Integer getLen() {
        return nLen;
    }

    public void setLen(Integer nLen) {
        this.nLen = nLen;
    }

    @JsonProperty(value = "sLenType")
    public String getLenType() {
        return sLenType;
    }

    public void setLenType(String sLenType) {
        this.sLenType = sLenType;
    }

    public String getsDateTimeAt() {
        return sDateTimeAt;
    }

    public void setsDateTimeAt(String sDateTimeAt) {
        this.sDateTimeAt = sDateTimeAt;
    }

    public String getsDateTimeTo() {
        return sDateTimeTo;
    }

    public void setsDateTimeTo(String sDateTimeTo) {
        this.sDateTimeTo = sDateTimeTo;
    }

    public String getsGroup() {
        return sGroup;
    }

    public void setsGroup(String sGroup) {
        this.sGroup = sGroup;
    }
    
    
}
