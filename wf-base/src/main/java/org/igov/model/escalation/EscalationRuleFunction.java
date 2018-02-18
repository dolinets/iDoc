package org.igov.model.escalation;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;
import org.igov.model.core.NamedEntity;

@javax.persistence.Entity
@ApiModel(description="Функции для правил эскалации")
public class EscalationRuleFunction extends NamedEntity {

    /**
     * строка бина-обработчика
     */
    @JsonProperty(value = "sBeanHandler")
    @Column
    @ApiModelProperty(value = "Обработчик функции для правил эскалации", required = true)
    private String sBeanHandler;

    //    @OneToMany(mappedBy = "nID_EscalationRuleFunction")
    //    private List<EscalationRule> aEscalationRule = new ArrayList<>();

    public String getsBeanHandler() {
        return sBeanHandler;
    }

    public void setsBeanHandler(String sBeanHandler) {
        this.sBeanHandler = sBeanHandler;
    }
}
