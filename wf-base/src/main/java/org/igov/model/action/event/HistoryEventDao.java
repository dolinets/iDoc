package org.igov.model.action.event;

import org.igov.model.action.vo.HistoryEventVO;
import org.igov.model.core.EntityDao;

import java.io.IOException;
import java.util.List;

public interface HistoryEventDao extends EntityDao<Long, HistoryEvent> {

    public HistoryEvent getHistoryEvent(Long id);

    public List<HistoryEvent> getHistoryEvents(Long nID_Subject, Long nID_HistoryEvent_Service, boolean bGrouped);

    public Long setHistoryEvent(Long nID_Subject, Long nID_HistoryEventType,
            String sEventName_Custom, String sMessage, Long nID_HistoryEvent_Service, Long nID_Document, String sSubjectInfo) throws IOException;

    HistoryEventVO getHistoryEventsByProcessId(String snID_process, Integer nSize, Integer nStart);
}
