package org.igov.model.process.processChat;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import org.igov.model.core.AbstractEntity;

@javax.persistence.Entity
public class ProcessChat extends AbstractEntity {
    
    private static final long serialVersionUID = 1L;
    
    @JsonProperty(value = "nID_Process_Activiti")
    @Column
    private Long nID_Process_Activiti;
    
    @JsonProperty(value = "sKeyGroup")
    @Column
    private String sKeyGroup;
    
    @JsonProperty(value = "aProcessChatMessage")
    @OneToMany(cascade = CascadeType.ALL)
    @Transient
    private List<ProcessChatMessage> aProcessChatMessage;

    public Long getnID_Process_Activiti() {
        return nID_Process_Activiti;
    }

    public void setnID_Process_Activiti(Long nID_Process_Activiti) {
        this.nID_Process_Activiti = nID_Process_Activiti;
    }

    public String getsKeyGroup() {
        return sKeyGroup;
    }

    public void setsKeyGroup(String sKeyGroup) {
        this.sKeyGroup = sKeyGroup;
    }

    public List<ProcessChatMessage> getaProcessChatMessage() {
        return aProcessChatMessage;
    }

    public void setaProcessChatMessage(List<ProcessChatMessage> aProcessChatMessage) {
        this.aProcessChatMessage = aProcessChatMessage;
    }

    @Override
    public String toString() {
        return "ProcessChat{" + "nID_Process_Activiti=" + nID_Process_Activiti + ", sKeyGroup=" + sKeyGroup + ", aProcessChatMessage=" + aProcessChatMessage + '}';
    }

}
