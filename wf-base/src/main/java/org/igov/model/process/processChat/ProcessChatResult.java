package org.igov.model.process.processChat;

import com.fasterxml.jackson.annotation.JsonRootName;
import java.io.Serializable;
import java.util.List;

@JsonRootName(value = "aProcessChatResult")
public class ProcessChatResult implements Serializable{
    
    private List<ProcessChat> aProcessChat;

    public List<ProcessChat> getaProcessChat() {
        return aProcessChat;
    }

    public void setaProcessChat(List<ProcessChat> aProcessChat) {
        this.aProcessChat = aProcessChat;
    }

}
