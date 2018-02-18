package org.igov.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.igov.model.core.AbstractEntity;

import javax.persistence.*;
import java.util.List;

@Entity
@ApiModel(description="Шаг документа")
public class DocumentStep extends AbstractEntity {

    @ApiModelProperty(value = "Строка-ключ шага", required = true)
    @JsonProperty(value = "sKey_Step")
    private String sKey_Step;

    @ApiModelProperty(value = "Порядковый номер шага", required = false)
    @JsonProperty(value = "nOrder")
    private Long nOrder;

    @ApiModelProperty(value = "Номер-ИД процесса(экземпляра БП) Activiti", required = true)
    @JsonProperty(value = "snID_Process_Activiti")
    private String snID_Process_Activiti;

    @JsonProperty(value = "aDocumentStepSubjectRight") //Todo переименовать
    @OneToMany(targetEntity = DocumentStepSubjectRight.class, mappedBy = "documentStep",
            cascade = CascadeType.ALL) 
    @LazyCollection(LazyCollectionOption.FALSE)
    @ApiModelProperty(value = "Массив прав субьектов-подписантов", required = true)
    private List<DocumentStepSubjectRight> aDocumentStepSubjectRight;
    
    @JsonProperty(value = "nID_DocumentStepType")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_DocumentStepType")
    @ApiModelProperty(value = "Типа шага")
    private DocumentStepType oDocumentStepType;

    public List<DocumentStepSubjectRight> aDocumentStepSubjectRight() {
        return aDocumentStepSubjectRight;
    }

    public void setaDocumentStepSubjectRight(List<DocumentStepSubjectRight> rights) {
        this.aDocumentStepSubjectRight = rights;
    }

    public String getsKey_Step() {
        return sKey_Step;
    }

    public void setsKey_Step(String sKey_Step) {
        this.sKey_Step = sKey_Step;
    }

    public Long getnOrder() {
        return nOrder;
    }

    public void setnOrder(Long nOrder) {
        this.nOrder = nOrder;
    }

    public String getSnID_Process_Activiti() {
        return snID_Process_Activiti;
    }

    public void setSnID_Process_Activiti(String snID_Process_Activiti) {
        this.snID_Process_Activiti = snID_Process_Activiti;
    }

    public DocumentStepType getoDocumentStepType() {
        return oDocumentStepType;
    }

    public void setoDocumentStepType(DocumentStepType oDocumentStepType) {
        this.oDocumentStepType = oDocumentStepType;
    }

    @Override
    public String toString() {
        return "DocumentStep{" +
                "id=" + getId() + ", " +
                "sKey_Step='" + sKey_Step + '\'' +
                ", nOrder=" + nOrder +
                ", snID_Process_Activiti='" + snID_Process_Activiti + '\'' +
                ", nID_DocumentStepType='"+ oDocumentStepType.getId() + "'}'";
    }
}
