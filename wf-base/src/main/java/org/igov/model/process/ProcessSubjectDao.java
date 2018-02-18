package org.igov.model.process;
import java.util.List;
import org.igov.model.core.EntityDao;
import org.joda.time.DateTime;


public interface ProcessSubjectDao extends EntityDao<Long, ProcessSubject>{
    
    ProcessSubject setProcessSubject(String snID_Process_Activiti, String sLogin, DateTime sDatePlan, 
            Long nOrder, ProcessSubjectStatus processSubjectStatus);
    
    ProcessSubject setProcessSubjectLogin(String snID_Process_Activiti, String sLogin);
    
    ProcessSubject setProcessSubjectOrder(String snID_Process_Activiti, Long nOrder);
    
    ProcessSubject setProcessSubjectStatus(String snID_Process_Activiti, ProcessSubjectStatus processSubjectStatus);
    
    ProcessSubject setProcessSubjectDatePlan(String snID_Process_Activiti, DateTime sDatePlan);
    
    ProcessSubject setProcessSubjectStatusAndReport(String snID_Process_Activiti, ProcessSubjectStatus processSubjectStatus, String sReport);
    
    ProcessSubject findByProcessActivitiId(String snID_Process_Activiti);
    
    ProcessSubject findByProcessIdLoginAndLoginRole(String snID_Process_Activiti, String sLogin, String sLoginRole);
    
    List<ProcessSubject> findByProcessActivitiIdAndLoginRole(String snID_Process_Activiti, String sLoginRole);
    
    ProcessSubject findByProcessAndLogin(String snID_Process_Activiti, String sLogin);
}