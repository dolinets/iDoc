package org.igov.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Column;

/**
 * @author dgroup
 * @since 28.06.15
 */
@javax.persistence.Entity
@ApiModel(description="Операторы документа для обслуживающих органов")
public class DocumentOperator_SubjectOrgan extends org.igov.model.core.NamedEntity {

    // Sorry about variables prefixes, but it's project convention.
    @JsonProperty(value = "nID_SubjectOrgan")
    @Column(nullable = false)
    @ApiModelProperty(value = "Уникальный номер-ИД обслуживающего органа", required = true)
    private Long nID_SubjectOrgan;

    /**
     * It represents a handler's full class name.
     * Each handler is an instance of
     * {@link org.igov.service.business.document.access.handler.DocumentAccessHandler}.
     **/
    @JsonProperty(value = "sHandlerClass")
    @Column(nullable = false)
    @ApiModelProperty(value = "Обработчик", required = true)
    private String sHandlerClass;

    public Long getnID_SubjectOrgan() {
        return nID_SubjectOrgan;
    }

    public void setnID_SubjectOrgan(Long nID_SubjectOrgan) {
        this.nID_SubjectOrgan = nID_SubjectOrgan;
    }

    public String getsHandlerClass() {
        return sHandlerClass;
    }

    public void setsHandlerClass(String sHandlerClass) {
        this.sHandlerClass = sHandlerClass;
    }

}