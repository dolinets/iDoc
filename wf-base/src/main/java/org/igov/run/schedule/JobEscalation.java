package org.igov.run.schedule;

import org.igov.service.exception.CommonServiceException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.igov.service.business.escalation.EscalationService;
import org.activiti.engine.HistoryService;

import java.util.Date;
import org.activiti.engine.RuntimeService;
import org.igov.io.GeneralConfig;
import org.igov.service.business.serverEntitySync.ServerEntitySyncService;

public class JobEscalation extends IAutowiredSpringJob {

    private final static Logger LOG = LoggerFactory.getLogger(JobEscalation.class);
    @Autowired
    GeneralConfig oGeneralConfig;
    @Autowired
    private EscalationService escalationService;
    @Autowired
    protected RuntimeService runtimeService;
    @Autowired
    private ServerEntitySyncService oServerEntitySyncService;

    public void execute(JobExecutionContext context) throws JobExecutionException {

        LOG.info("In JobEscalation - executing JOB at {} by context.getTrigger().getName()={}",
                new Date(), context.getTrigger().getName());
        try {
            //TODO: ��� ����� �������� ����� ������� ���������!
            escalationService.runEscalationAll(context.getJobDetail().getName());
            
            if(context.getTrigger().getName().equals("oJobDetail_Escalation_Standart")){
                try{
                    byte[] backupData = oServerEntitySyncService.backupTables("StaffTables");;
                    oServerEntitySyncService.saveBackupedFilesToServer(backupData, "StaffTables", "staff_sheduler_backup", false);  
                }catch (Exception ex){
                    LOG.info("In JobEscalation - exception {}", ex.getMessage());
                }
            }
        } catch (CommonServiceException oException) {
            LOG.error("Bad: ", oException.getMessage());
            LOG.debug("FAIL:", oException);
        }
    }

}
