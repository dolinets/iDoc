package org.igov.run.schedule;

import org.igov.service.business.serverEntitySync.staff.UBStaffSync;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class JobUBStaffSync extends IAutowiredSpringJob {

    private final static Logger LOG = LoggerFactory.getLogger(JobUBStaffSync.class);

    @Autowired(required = false)
    private UBStaffSync ubStaffSync;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOG.info("Trigger {} executed", context.getTrigger().getName());
        if (ubStaffSync != null) {
            ubStaffSync.syncStaffAll();
        } else {
            LOG.warn("'ubStaffSync' bean is not configured, job is ignored");
        }
    }

}
