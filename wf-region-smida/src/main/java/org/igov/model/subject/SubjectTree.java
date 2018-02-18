package org.igov.model.subject;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.igov.model.core.AbstractEntity;
import org.igov.model.document.Document;

/**
 *
 * @author alex 
 */
@javax.persistence.Entity
public class SubjectTree extends AbstractEntity{
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "oSubjectParent")
    @ManyToOne(targetEntity = Subject.class)
    @JoinColumn(name = "nID_Subject_Parent", updatable = false)
    private Subject oSubjectParent;
    
    @JsonProperty(value = "oSubjectChild")
    @ManyToOne(targetEntity = Subject.class)
    @JoinColumn(name = "nID_Subject_Child", updatable = false)
    private Subject oSubjectChild;
    
    @JsonProperty(value = "oSubjectTreeType")
    @ManyToOne(targetEntity = SubjectTreeType.class)
    @JoinColumn(name = "nID_SubjectTreeType")
    private SubjectTreeType oSubjectTreeType;

    @JsonProperty(value = "oDocument")
    @ManyToOne(targetEntity = Document.class)
    @JoinColumn(name = "nID_Document")
    private Document oDocument;

    public Subject getoSubjectParent() {
        return oSubjectParent;
    }

    public void setoSubjectParent(Subject oSubjectParent) {
        this.oSubjectParent = oSubjectParent;
    }

    public Subject getoSubjectChild() {
        return oSubjectChild;
    }

    public void setoSubjectChild(Subject oSubjectChild) {
        this.oSubjectChild = oSubjectChild;
    }

    public SubjectTreeType getoSubjectTreeType() {
        return oSubjectTreeType;
    }

    public void setoSubjectTreeType(SubjectTreeType oSubjectTreeType) {
        this.oSubjectTreeType = oSubjectTreeType;
    }

    public Document getoDocument() {
        return oDocument;
    }

    public void setoDocument(Document oDocument) {
        this.oDocument = oDocument;
    }
        
    
}
