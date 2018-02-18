package org.igov.model.process.processLink;

import org.igov.model.core.EntityDao;

import java.util.List;

/**
 *
 * @author idenysenko
 */
public interface ProcessLinkDao extends EntityDao<Long, ProcessLink> {

    List<ProcessLink> getProcessLinks(String sID_Group_Activiti, String sType, String sSubType);

    ProcessLink findProcessLink(String snID_Task, String sID_Group_Activiti, Long nID_Server);

    List<ProcessLink> getDeletedProcessLinks(String sID_Group_Activiti);

    List<ProcessLink> getProcessLinksByProcessAndGroup(String snID_Process_Activiti, String sID_Group_Activiti);
}
