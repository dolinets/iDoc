package org.igov.model.server.entity.sync;

import org.igov.model.core.AbstractEntity;

import javax.persistence.Column;

/**
 *
 * @author Kovylin
 */
@javax.persistence.Entity
public class ServerEntitySyncStatus extends AbstractEntity{
    
    @Column
    private String sName;

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }
}
