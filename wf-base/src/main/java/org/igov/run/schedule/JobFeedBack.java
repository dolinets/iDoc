package org.igov.run.schedule;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;

import org.igov.service.business.action.task.bp.handler.BpServiceHandler;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class JobFeedBack extends IAutowiredSpringJob {

    private final static Logger LOG = LoggerFactory.getLogger(JobFeedBack.class);

    @Autowired
    private HistoryService historyService;
    @Autowired
    private RuntimeService runtimeService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        
        LOG.info("In JobFeedBack - executing JOB at {} by context.getTrigger().getName()={}",
                new Date(), context.getTrigger().getName());
        LOG.info("getFeedBackCountBefore : " + BpServiceHandler.getFeedBackCount());
        BpServiceHandler.setFeedBackCount(0L);
        LOG.info("getFeedBackCountAfter : " + BpServiceHandler.getFeedBackCount());
        LocalDate today = LocalDate.now();
        LOG.info("today: " + today);
        LocalDate deadline = today.minusDays(20);
        LOG.info("deadline: " + deadline);
        Date date = java.sql.Date.valueOf(deadline);
        LOG.info("date: " + date);
        
        List<HistoricProcessInstance> feedbackProcces = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionId(BpServiceHandler.PROCESS_FEEDBACK)
                .startedBefore(date).unfinished().list();
        LOG.info("feedbackProcces: " + feedbackProcces);
        
        LOG.info("List feedbackProcces: " + feedbackProcces.size());
        for (HistoricProcessInstance feedbackProcce : feedbackProcces) {
            LOG.info("Delete feedbackProcce.getId(): " + feedbackProcce.getId());
            runtimeService.deleteProcessInstance(feedbackProcce.getId(), " deprecated");

        }
    }
}
