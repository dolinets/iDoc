package org.igov.model.action.vo;

import org.igov.model.action.event.HistoryEvent;

import java.util.List;

public class HistoryEventVO {

    private List<HistoryEvent> aoHistoryEvent;
    private Integer nTotalCount;
    private Integer nStart;
    private Integer nSize;

    public HistoryEventVO() {
    }

    public List<HistoryEvent> getAoHistoryEvent() {
        return aoHistoryEvent;
    }

    public void setAoHistoryEvent(List<HistoryEvent> aoHistoryEvent) {
        this.aoHistoryEvent = aoHistoryEvent;
    }

    public Integer getnTotalCount() {
        return nTotalCount;
    }

    public void setnTotalCount(Integer nTotalCount) {
        this.nTotalCount = nTotalCount;
    }

    public Integer getnStart() {
        return nStart;
    }

    public void setnStart(Integer nStart) {
        this.nStart = nStart;
    }

    public Integer getnSize() {
        return nSize;
    }

    public void setnSize(Integer nSize) {
        this.nSize = nSize;
    }
}
