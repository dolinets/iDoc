package org.igov.run.schedule;

import org.igov.service.business.launch.LaunchService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class JobProcessLaunchers extends IAutowiredSpringJob {

    private static final Logger LOG = LoggerFactory.getLogger(JobProcessLaunchers.class);

    @Autowired private LaunchService oLaunchService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.info("Started job - {} at {}, group={} by trigger={}", jobExecutionContext.getJobDetail().getName(),
                LocalDateTime.now(), jobExecutionContext.getJobDetail().getGroup(), jobExecutionContext.getTrigger().getName());
        String sJobName = jobExecutionContext.getJobDetail().getName();
        if (sJobName.equals("oJobDetail_ProcessLaunchers_Standart")) {
            oLaunchService.processLaunchers(5, null, null, null);
        }
        if (sJobName.equals("oJobDetail_ProcessLaunchers_Night")) {
            oLaunchService.processLaunchers(7, null, null, null);
        }
    }
}
