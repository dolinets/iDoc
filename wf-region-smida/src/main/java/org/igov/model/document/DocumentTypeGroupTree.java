package org.igov.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.igov.model.core.AbstractEntity;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class DocumentTypeGroupTree extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "oDocumentType")
    @ManyToOne(targetEntity = DocumentType.class)
    @JoinColumn(name = "nID_DocumentType")
    private DocumentType oDocumentType;
    
    @JsonProperty(value = "oDocumentTypeGroup")
    @ManyToOne(targetEntity = DocumentTypeGroup.class)
    @JoinColumn(name = "nID_DocumentTypeGroup")
    private DocumentTypeGroup oDocumentTypeGroup;
    
    @JsonProperty(value = "oDocumentTypeGroup_Parent")
    @ManyToOne(targetEntity = DocumentTypeGroup.class)
    @JoinColumn(name = "nID_DocumentTypeGroup_Parent")
    private DocumentTypeGroup oDocumentTypeGroup_Parent;

    public DocumentType getoDocumentType() {
        return oDocumentType;
    }

    public void setoDocumentType(DocumentType oDocumentType) {
        this.oDocumentType = oDocumentType;
    }

    public DocumentTypeGroup getoDocumentTypeGroup() {
        return oDocumentTypeGroup;
    }

    public void setoDocumentTypeGroup(DocumentTypeGroup oDocumentTypeGroup) {
        this.oDocumentTypeGroup = oDocumentTypeGroup;
    }

    public DocumentTypeGroup getoDocumentTypeGroup_Parent() {
        return oDocumentTypeGroup_Parent;
    }

    public void setoDocumentTypeGroup_Parent(DocumentTypeGroup oDocumentTypeGroup_Parent) {
        this.oDocumentTypeGroup_Parent = oDocumentTypeGroup_Parent;
    }

}
