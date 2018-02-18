package org.igov.model.escalation;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.igov.model.core.AbstractEntity;

@javax.persistence.Entity
@ApiModel(description="Правила генерации эскалаций")
public class EscalationRule extends AbstractEntity {

    /**
     * ИД-строка бизнеспроцесса
     */
    @JsonProperty(value = "sID_BP")         
    @Column
    @ApiModelProperty(value = "Уникальная строка-ИД бизнес-процесса", required = true)
    private String sID_BP; 

    /**
     * ИД-строка юзертаски бизнеспроцесса
     */
    @JsonProperty(value = "sID_UserTask")
    @Column
    @ApiModelProperty(value = "Уникальная строка-ИД юзертаски бизнес-процесса, для которой применяется правило", required = true)
    private String sID_UserTask;

    /**
     * строка, до 200 символов ()
     */
    @JsonProperty(value = "sCondition")
    @Column(length = 200)
    @ApiModelProperty(value = "Условие применения правила", required = true)
    private String sCondition;

    /**
     * строка-обьект, с данніми (JSON-обьект), до 500 символов
     */
    @JsonProperty(value = "soData")
    @Column(length = 500)
    @ApiModelProperty(value = "Настройка правила. Строка-обьект, с данніми (JSON-обьект), до 500 символов", required = true)
    private String soData;

    /**
     * строка файла-шаблона
     */
    @JsonProperty(value = "sPatternFile")
    @Column
    @ApiModelProperty(value = "Путь к шаблону email", required = true)
    private String sPatternFile;

    /**
     * ИД-номер функции ,при эскалации
     */
    @JsonProperty(value = "nID_EscalationRuleFunction")
    @ManyToOne(targetEntity = EscalationRuleFunction.class)//??, fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_EscalationRuleFunction")
    @ApiModelProperty(value = "Путь к шаблону email", required = true)
    private EscalationRuleFunction oEscalationRuleFunction;

    public String getsID_BP() {
        return sID_BP;
    }

    public void setsID_BP(String sID_BP) {
        this.sID_BP = sID_BP;
    }

    public String getsID_UserTask() {
        return sID_UserTask;
    }

    public void setsID_UserTask(String sID_UserTask) {
        this.sID_UserTask = sID_UserTask;
    }

    public String getsCondition() {
        return sCondition;
    }

    public void setsCondition(String sCondition) {
        this.sCondition = sCondition;
    }

    public String getSoData() {
        return soData;
    }

    public void setSoData(String soData) {
        this.soData = soData;
    }

    public String getsPatternFile() {
        return sPatternFile;
    }

    public void setsPatternFile(String sPatternFile) {
        this.sPatternFile = sPatternFile;
    }

    public EscalationRuleFunction getoEscalationRuleFunction() {
        return oEscalationRuleFunction;
    }

    public void setoEscalationRuleFunction(EscalationRuleFunction oEscalationRuleFunction) {
        this.oEscalationRuleFunction = oEscalationRuleFunction;
    }
}
