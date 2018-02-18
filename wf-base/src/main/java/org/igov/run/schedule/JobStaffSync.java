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

public class JobStaffSync extends IAutowiredSpringJob {

    private final static Logger LOG = LoggerFactory.getLogger(JobEscalation.class);
    
    @Autowired
    private ServerEntitySyncService ServerEntitySyncService;

    public void execute(JobExecutionContext context) throws JobExecutionException {

        LOG.info("In JobStaffSync - executing JOB at {} by context.getTrigger().getName()={}",
                new Date(), context.getTrigger().getName());
        
        ServerEntitySyncService.runServerEntitySync(null, null);
        
    }

}
