package org.igov.model.core;

/**
 *
 * @author alex
 */
public enum EntityItem {
    TEST_VALUE(1L,"Base");

    private final Long nID;
    private final String sClass;

    private EntityItem(Long nID, String sClass) {
        this.nID = nID;
        this.sClass = sClass;
    }

    public Long nID() {
        return nID;
    }

    public String sClass() {
        return sClass;
    }
}
