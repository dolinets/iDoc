package org.igov.model.action.launch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import org.igov.model.core.AbstractEntity;
import org.igov.model.server.Server;

import org.igov.util.JSON.JsonDateDeserializer;
import org.igov.util.JSON.JsonDateSerializer;

import org.joda.time.DateTime;

/**
 *
 * @author idenysenko
 */
@javax.persistence.Entity
public class Launch extends AbstractEntity {
    
    @JsonProperty(value = "oLaunchStatus")
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "nID_LaunchStatus")
    private LaunchStatus oLaunchStatus;
    
    @JsonProperty(value = "nID_LaunchGroup")
    @Column
    private Integer nID_LaunchGroup;
    
    @JsonProperty(value = "sDateEdit")
    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateEdit;
    
    @JsonProperty(value = "sDateLock")
    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    @Type(type = DATETIME_TYPE)
    @Column
    private DateTime sDateLock;
    
    @JsonProperty(value = "nTry")
    @Column(nullable = false)
    private Integer nTry = 0;
    
    @JsonProperty(value = "sData")
    @Column
    private String sData;
    
    @JsonProperty(value = "sDataReturn")
    @Column
    private String sDataReturn;
    
    @JsonProperty(value = "sMethod")
    @Column
    private String sMethod;
    
    @JsonProperty(value = "sReturn")
    @Column
    private String sReturn;
    
    @JsonProperty(value = "oServer")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_Server")
    private Server oServer;
    
    @JsonProperty(value = "oClient")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_Server_Client")
    private Server oClient;
    
    @JsonProperty(value = "sLoginPrincipal")
    @Column
    private String sLoginPrincipal;

    public LaunchStatus getoLaunchStatus() {
        return oLaunchStatus;
    }

    public void setoLaunchStatus(LaunchStatus oLaunchStatus) {
        this.oLaunchStatus = oLaunchStatus;
    }

    public Integer getnID_LaunchGroup() {
        return nID_LaunchGroup;
    }

    public void setnID_LaunchGroup(Integer nID_LaunchGroup) {
        this.nID_LaunchGroup = nID_LaunchGroup;
    }

    public DateTime getsDateEdit() {
        return sDateEdit;
    }

    public void setsDateEdit(DateTime sDateEdit) {
        this.sDateEdit = sDateEdit;
    }

    public DateTime getsDateLock() {
        return sDateLock;
    }

    public void setsDateLock(DateTime sDateLock) {
        this.sDateLock = sDateLock;
    }

    public Integer getnTry() {
        return nTry;
    }

    public void setnTry(Integer nTry) {
        this.nTry = nTry;
    }

    public String getsData() {
        return sData;
    }

    public void setsData(String sData) {
        this.sData = sData;
    }

    public String getsDataReturn() {
        return sDataReturn;
    }

    public void setsDataReturn(String sDataReturn) {
        this.sDataReturn = sDataReturn;
    }

    public String getsMethod() {
        return sMethod;
    }

    public void setsMethod(String sMethod) {
        this.sMethod = sMethod;
    }

    public String getsReturn() {
        return sReturn;
    }

    public void setsReturn(String sReturn) {
        this.sReturn = sReturn;
    }

    public Server getoServer() {
        return oServer;
    }

    public void setoServer(Server oServer) {
        this.oServer = oServer;
    }

    public Server getoClient() {
        return oClient;
    }

    public void setoClient(Server oClient) {
        this.oClient = oClient;
    }

    public String getsLoginPrincipal() {
        return sLoginPrincipal;
    }

    public void setsLoginPrincipal(String sLoginPrincipal) {
        this.sLoginPrincipal = sLoginPrincipal;
    }

    @Override
    public String toString() {
        return "Launch{" 
                + "oLaunchStatus=" + oLaunchStatus
                + ", nID_LaunchGroup=" + nID_LaunchGroup
                + ", sDateEdit=" + sDateEdit
                + ", sDateLock=" + sDateLock 
                + ", nTry=" + nTry
                + ", sData=" + sData
                + ", sDataReturn=" + sDataReturn
                + ", sMethod=" + sMethod
                + ", sReturn=" + sReturn
                + ", oServer=" + oServer 
                + ", oClient=" + oClient 
                + ", sLoginPrincipal=" + sLoginPrincipal
                + '}';
    }
    
}
