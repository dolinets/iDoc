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
public class DocumentTree extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oDocumentParent")
    @ManyToOne(targetEntity = Document.class)
    @JoinColumn(name = "nID_Document_Parent ", nullable = false, updatable = false)
    private Document oDocumentParent;
    
    @JsonProperty(value = "oDocumentChild")
    @ManyToOne(targetEntity = Document.class)
    @JoinColumn(name = "nID_Document_Child  ", nullable = false, updatable = false)
    private Document oDocumentChild;

    public Document getoDocumentParent() {
        return oDocumentParent;
    }

    public void setoDocumentParent(Document oDocumentParent) {
        this.oDocumentParent = oDocumentParent;
    }

    public Document getoDocumentChild() {
        return oDocumentChild;
    }

    public void setoDocumentChild(Document oDocumentChild) {
        this.oDocumentChild = oDocumentChild;
    }

    @Override
    public String toString() {
        return "DocumentTree{" + "oDocumentParent=" + oDocumentParent + ", oDocumentChild=" + oDocumentChild + '}';
    }

}
