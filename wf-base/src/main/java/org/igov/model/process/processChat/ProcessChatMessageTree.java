package org.igov.model.process.processChat;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.igov.model.core.AbstractEntity;

@javax.persistence.Entity
public class ProcessChatMessageTree extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "processChatMessageChild")
    @ManyToOne(targetEntity = ProcessChatMessage.class)
    @JoinColumn(name = "nID_ProcessChatMessage_Child", nullable = false, updatable = false)
    @Cascade(CascadeType.REMOVE)
    private ProcessChatMessage processChatMessageChild;

    @JsonProperty(value = "processChatMessageParent")
    @ManyToOne(targetEntity = ProcessChatMessage.class)
    @JoinColumn(name = "nID_ProcessChatMessage_Parent", nullable = false, updatable = false)
    private ProcessChatMessage processChatMessageParent;

    public ProcessChatMessage getProcessChatMessageChild() {
        return processChatMessageChild;
    }

    public void setProcessChatMessageChild(ProcessChatMessage processChatMessageChild) {
        this.processChatMessageChild = processChatMessageChild;
    }

    public ProcessChatMessage getProcessChatMessageParent() {
        return processChatMessageParent;
    }

    public void setProcessChatMessageParent(ProcessChatMessage processChatMessageParent) {
        this.processChatMessageParent = processChatMessageParent;
    }

    @Override
    public String toString() {
        return "ProcessChatMessageTree{" + "processChatMessageChild=" + processChatMessageChild + ", processChatMessageParent=" + processChatMessageParent + '}';
    }

}
