package org.igov.model.server.entity.sync;

import java.util.Date;
import org.igov.model.core.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.joda.time.DateTime;

/**
 *
 * @author Kovylin
 */
@javax.persistence.Entity
public class ServerEntitySync extends AbstractEntity{

    @Column
    private Long nID_Server;
    
    @Column
    private String sID_EntityRow;
    
    @Column
    private String sID_Entity;
    
    @Column
    private String sID_EntityAction;
    
    @Column
    private Date sDate;
    
    @Column
    private String sAnswer;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nID_ServerEntitySyncStatus")
    private ServerEntitySyncStatus oServerEntitySyncStatus;
    
    @Column
    private Long nTry;

    public Long getnID_Server() {
        return nID_Server;
    }

    public String getsID_EntityRow() {
        return sID_EntityRow;
    }

    public String getsID_Entity() {
        return sID_Entity;
    }

    public String getsID_EntityAction() {
        return sID_EntityAction;
    }

    public Date getsDate() {
        return sDate;
    }

    public String getsAnswer() {
        return sAnswer;
    }

    public ServerEntitySyncStatus getoServerEntitySyncStatus() {
        return oServerEntitySyncStatus;
    }

    public Long getnTry() {
        return nTry;
    }

    public void setnID_Server(Long nID_Server) {
        this.nID_Server = nID_Server;
    }

    public void setsID_EntityRow(String sID_EntityRow) {
        this.sID_EntityRow = sID_EntityRow;
    }

    public void setsID_Entity(String sID_Entity) {
        this.sID_Entity = sID_Entity;
    }

    public void setsID_EntityAction(String sID_EntityAction) {
        this.sID_EntityAction = sID_EntityAction;
    }

    public void setsDate(Date sDate) {
        this.sDate = sDate;
    }

    public void setsAnswer(String sAnswer) {
        this.sAnswer = sAnswer;
    }

    public void setoServerEntitySyncStatus(ServerEntitySyncStatus oServerEntitySyncStatus) {
        this.oServerEntitySyncStatus = oServerEntitySyncStatus;
    }

    public void setnTry(Long nTry) {
        this.nTry = nTry;
    }
    
}
