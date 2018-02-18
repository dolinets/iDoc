package org.igov.model.document;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.igov.model.core.AbstractEntity;

/**
 *
 * @author alex
 */
@javax.persistence.Entity
public class DocumentStatutoryState_Type extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "oDocumentType")
    @ManyToOne(targetEntity = DocumentType.class)
    @JoinColumn(name = "nID_DocumentType")
    private DocumentType oDocumentType;

    @JsonProperty(value = "oDocumentStatutoryState")
    @ManyToOne(targetEntity = DocumentStatutoryState.class)
    @JoinColumn(name = "nID_DocumentStatutoryState")
    private DocumentStatutoryState oDocumentStatutoryState;

    @JsonProperty(value = "oDocument_Last")
    @OneToOne(targetEntity = Document.class)
    @JoinColumn(name = "nID_Document_Last")
    private Document oDocument_Last;

    public DocumentType getoDocumentType() {
        return oDocumentType;
    }

    public void setoDocumentType(DocumentType oDocumentType) {
        this.oDocumentType = oDocumentType;
    }

    public DocumentStatutoryState getoDocumentStatutoryState() {
        return oDocumentStatutoryState;
    }

    public void setoDocumentStatutoryState(DocumentStatutoryState oDocumentStatutoryState) {
        this.oDocumentStatutoryState = oDocumentStatutoryState;
    }

    public Document getoDocument_Last() {
        return oDocument_Last;
    }

    public void setoDocument_Last(Document oDocument_Last) {
        this.oDocument_Last = oDocument_Last;
    }
}
