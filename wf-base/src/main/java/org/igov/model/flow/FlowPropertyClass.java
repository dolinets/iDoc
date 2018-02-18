package org.igov.model.flow;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.persistence.Column;
import org.igov.model.core.NamedEntity;

/**
 * Handler Class for FlowProperty
 * <p/>
 * User: goodg_000
 * Date: 14.06.2015
 * Time: 15:11
 */
@javax.persistence.Entity
@ApiModel(description="Классы потоков электронных очередей")
public class FlowPropertyClass extends NamedEntity {

    /**
     * Fully qualified class name. I.e. package.className
     */
    @Column
    @ApiModelProperty(value = "Путь к обработчику", required = true)
    private String sPath;

    /**
     * Optional bean name of bean of corresponding class. Allows to use same class with different bean configurations.
     */
    @Column
    @ApiModelProperty(value = "Название обработчика", required = true)
    private String sBeanName;

    public String getsPath() {
        return sPath;
    }

    public void setsPath(String sPath) {
        this.sPath = sPath;
    }

    public String getsBeanName() {
        return sBeanName;
    }

    public void setsBeanName(String sBeanName) {
        this.sBeanName = sBeanName;
    }
}
