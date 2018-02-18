package org.igov.model.action.vo;

import java.util.Set;

/**
 *
 * @author idenysenko
 */
public class UserDataVO {
    
    private String sFIO;
    private Set<String> asGroupsMember;

    public String getsFIO() {
        return sFIO;
    }

    public void setsFIO(String sFIO) {
        this.sFIO = sFIO;
    }

    public Set<String> getAsGroupsMember() {
        return asGroupsMember;
    }

    public void setAsGroupsMember(Set<String> asGroupsMember) {
        this.asGroupsMember = asGroupsMember;
    }
    
}
