package org.igov.model.process.processChat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;
import javax.persistence.CascadeType;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import org.igov.model.core.AbstractEntity;
import org.igov.util.JSON.JsonDateTimeDeserializer;
import org.igov.util.JSON.JsonDateTimeSerializer;
import org.joda.time.DateTime;

@javax.persistence.Entity
public class ProcessChatMessage extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "oProcessChat")
    @ManyToOne(targetEntity = ProcessChat.class)
    @JoinColumn(name = "nID_ProcessChat")
    @JsonIgnoreProperties("aProcessChatMessage")
    private ProcessChat oProcessChat;

    @JsonProperty(value = "sKeyGroup_Author")
    @Column
    private String sKeyGroup_Author;

    @JsonProperty(value = "sDate")
    @JsonSerialize(using = JsonDateTimeSerializer.class)
    @JsonDeserialize(using = JsonDateTimeDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDate;

    @JsonProperty(value = "sBody")
    @Column
    private String sBody;

    @JsonProperty(value = "aProcessChatMessageChild")
    @OneToMany(mappedBy = "processChatMessageChild", cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Transient
    private List<ProcessChatMessage> aProcessChatMessageChild;
    
    @JsonProperty(value = "sFIO_Author")
    @Transient
    private String sFIO_Author;
    
    @JsonProperty(value = "sFIO_Referent")
    @Transient
    private String sFIO_Referent;
    
    @JsonProperty(value = "sLoginReferent")
    @Column
    private String sLoginReferent;
    
    public ProcessChat getoProcessChat() {
        return oProcessChat;
    }

    public void setoProcessChat(ProcessChat oProcessChat) {
        this.oProcessChat = oProcessChat;
    }

    public String getsKeyGroup_Author() {
        return sKeyGroup_Author;
    }

    public void setsKeyGroup_Author(String sKeyGroupAuthor) {
        this.sKeyGroup_Author = sKeyGroupAuthor;
    }

    public DateTime getsDate() {
        return sDate;
    }

    public void setsDate(DateTime sDate) {
        this.sDate = sDate;
    }

    public String getsBody() {
        return sBody;
    }

    public void setsBody(String sBody) {
        this.sBody = sBody;
    }
    
    public String getsFIO_Author() {
        return sFIO_Author;
    }

    public void setsFIO_Author(String sFIO_Author) {
        this.sFIO_Author = sFIO_Author;
    }
    
    public String getsFIO_Referent() {
        return sFIO_Referent;
    }

    public void setsFIO_Referent(String sFIO_Referent) {
        this.sFIO_Referent = sFIO_Referent;
    }        
    
    public String getsLoginReferent() {
        return sLoginReferent;
    }

    public void setsLoginReferent(String sLoginReferent) {
        this.sLoginReferent = sLoginReferent;
    }

    public List<ProcessChatMessage> getaProcessChatMessageChild() {
        return aProcessChatMessageChild;
    }

    public void setaProcessChatMessageChild(List<ProcessChatMessage> aProcessChatMessageChild) {
        this.aProcessChatMessageChild = aProcessChatMessageChild;
    }

    @Override
    public String toString() {
        return "ProcessChatMessage{" + "oProcessChat=" + oProcessChat + ", sKeyGroup_Author=" + sKeyGroup_Author + ", sDate=" + sDate + ", sBody=" + sBody + 
                ", sFIO_Author=" + sFIO_Author + ", sFIO_Referent=" + sFIO_Referent+ ", aProcessChatMessageChild=" + aProcessChatMessageChild + '}';
    }

    
}
