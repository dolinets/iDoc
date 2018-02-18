package org.igov.model.process.processChat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProcessChatMessageParentNode implements Serializable {

    private ProcessChatMessage group;

    private List<ProcessChatMessage> children = new ArrayList<>();

    public ProcessChatMessageParentNode() {
    }

    public void addChild(ProcessChatMessage child) {
        children.add(child);
    }

    public ProcessChatMessage getGroup() {
        return group;
    }

    public void setGroup(ProcessChatMessage group) {
        this.group = group;
    }

    public List<ProcessChatMessage> getChildren() {
        return children;
    }

    public void setChildren(List<ProcessChatMessage> children) {
        this.children = children;
    }

}
