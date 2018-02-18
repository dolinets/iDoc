package org.igov.run.schedule;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.igov.io.GeneralConfig;

import java.text.ParseException;

/**
 * User: goodg_000 Date: 27.08.2015 Time: 1:05
 */
public class JobsInitializer implements InitializingBean, ApplicationContextAware {

    private final static Logger LOG = LoggerFactory.getLogger(JobsInitializer.class);

    @Autowired
    GeneralConfig generalConfig;

    private static ApplicationContext applicationContext;
    private Scheduler scheduler;

    /**
     * @return used by {@link IAutowiredSpringJob} to autowire property beans into jobs.
     */
    static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        addEscalationJob(scheduler);
        addFeedBackJob(scheduler);
        addBuilderFlowSlotsJob(scheduler);
        addProcessLaunchersJob(scheduler);
        addSubmitEscalationJob(scheduler);
        addStaffSyncJob(scheduler);
        addUBStaffSyncJob(scheduler);
    }

    private void addSubmitEscalationJob(Scheduler scheduler) throws SchedulerException {
        JobDetail oJobDetail_Escalation_Standart = new JobDetail("oJobDetail_Escalation_Standart_Submit",
                "oJobDetail_Escalation_Group_Submit", JobEscalation.class);

        CronTrigger oCronTrigger_EveryNight_Deep = new CronTrigger("oCronTrigger_EveryNight_Deep_Submit",
                "oCronTrigger_EveryNight_EscalationGroup_Submit");
        try {
            LOG.info("oCronExpression__EveryNight_Deep...");
            CronExpression oCronExpression__EveryNight_Deep = new CronExpression("0 00 14 1/1 * ?");   
            LOG.info("oCronExpression__EveryNight_Deep.setCronExpression...");
            oCronTrigger_EveryNight_Deep.setCronExpression(oCronExpression__EveryNight_Deep);
        } catch (Exception oException) {
            LOG.error("FAIL: ", oException.getMessage());
            LOG.debug("FAIL: ", oException);
        }
        if ((generalConfig.getbStartEscalation())||(!generalConfig.isSelfTest() && !generalConfig.isTest_Escalation() 
                && !"https://prod-double-region.tech.igov.org.ua".equalsIgnoreCase(generalConfig.getSelfHost()))) {
            LOG.info("scheduleJob...");
            scheduler.scheduleJob(oJobDetail_Escalation_Standart, oCronTrigger_EveryNight_Deep);
        } else {
            LOG.info("scheduleJob... SKIPED(test)!");
        }
    }
    
    private void addEscalationJob(Scheduler scheduler) throws SchedulerException {
        JobDetail oJobDetail_Escalation_Standart = new JobDetail("oJobDetail_Escalation_Standart",
                "oJobDetail_Escalation_Group", JobEscalation.class);

        CronTrigger oCronTrigger_EveryNight_Deep = new CronTrigger("oCronTrigger_EveryNight_Deep",
                "oCronTrigger_EveryNight_EscalationGroup");
        try {
            LOG.info("oCronExpression__EveryNight_Deep...");
            CronExpression oCronExpression__EveryNight_Deep = new CronExpression("0 0 4 1/1 * ?");   //maxline: todo поменять обратно на 2 часа ночи с 4-х
            LOG.info("oCronExpression__EveryNight_Deep.setCronExpression...");
            oCronTrigger_EveryNight_Deep.setCronExpression(oCronExpression__EveryNight_Deep);
        } catch (Exception oException) {
            LOG.error("FAIL: ", oException.getMessage());
            LOG.debug("FAIL: ", oException);
        }
        if ((generalConfig.getbStartEscalation())||(!generalConfig.isSelfTest() && !generalConfig.isTest_Escalation() 
                && !"https://prod-double-region.tech.igov.org.ua".equalsIgnoreCase(generalConfig.getSelfHost()))) {
            LOG.info("scheduleJob...");
            scheduler.scheduleJob(oJobDetail_Escalation_Standart, oCronTrigger_EveryNight_Deep);
        } else {
            LOG.info("scheduleJob... SKIPED(test)!");
        }
    }
    
    private void addFeedBackJob(Scheduler scheduler) throws SchedulerException {
        JobDetail oJobDetail_FeedBack_Standart = new JobDetail("oJobDetail_FeedBack_Standart",
                "oJobDetail_FeedBack_Group", JobFeedBack.class);

        CronTrigger oCronTrigger_EveryNight_Deep = new CronTrigger("oCronTrigger_EveryNight_Deep",
                "oCronTrigger_EveryNight_FeedBackGroup");
        try {
            LOG.info("oCronExpression__EveryNight_Deep...");
            CronExpression oCronExpression__EveryNight_Deep = new CronExpression("0 0 4 1/1 * ?");   // maxline: todo поменять обратно на 2 часа ночи с 4-х
            LOG.info("oCronExpression__EveryNight_Deep.setCronExpression...");
            oCronTrigger_EveryNight_Deep.setCronExpression(oCronExpression__EveryNight_Deep);
        } catch (Exception oException) {
            LOG.error("FAIL: ", oException.getMessage());
            LOG.debug("FAIL: ", oException);
        }
        //TODO:раскомментировать после тестирования
       // if (!generalConfig.isSelfTest() && !"https://prod-double-region.tech.igov.org.ua".equalsIgnoreCase(generalConfig.getSelfHost())) {
            LOG.info("scheduleJob...");
            scheduler.scheduleJob(oJobDetail_FeedBack_Standart, oCronTrigger_EveryNight_Deep);  
        /*} else {
            LOG.info("scheduleJob... SKIPED(test)!");
        }*/
    }

    private void addBuilderFlowSlotsJob(Scheduler scheduler) throws SchedulerException {
        JobDetail oJobDetail_BuilderFlowSlots_Standart = new JobDetail("oJobDetail_BuilderFlowSlots_Standart",
                "oJobDetail_BuilderFlowSlots_Group", JobBuilderFlowSlots.class);

        CronTrigger oCronTrigger_EveryNight_Deep = new CronTrigger("oCronTrigger_EveryNight_Deep",
                "oCronTrigger_EveryNight_BuilderFlowSlotsJobGroup");
       
            LOG.info("oCronExpression__EveryNight_Deep...");
        try { 
         CronExpression oCronExpression__EveryNight_Deep = new CronExpression("0 0 6 1/1 * ?"); //раз в сутки в 6-00
            LOG.info("oCronExpression__EveryNight_Deep.setCronExpression...");
            oCronTrigger_EveryNight_Deep.setCronExpression(oCronExpression__EveryNight_Deep); 
        } catch (Exception oException) {
            LOG.error("FAIL: ", oException.getMessage());
            LOG.debug("FAIL: ", oException);
        }
        scheduler.scheduleJob(oJobDetail_BuilderFlowSlots_Standart, oCronTrigger_EveryNight_Deep);
    }
    
    private void addPaymentProcessorJob(Scheduler scheduler) throws SchedulerException {
        JobDetail oJobDetail_PaymentProcessor_Standart = new JobDetail("oJobDetail_PaymentProcessor_Standart",
                "oJobDetail_PaymentProcesor_Group", JobPaymentProcessor.class);

        CronTrigger oCronTrigger_EveryNight_Deep = new CronTrigger("oCronTrigger_EveryNight_Deep",
                "oCronTrigger_EveryNight_Group");
        try {
            LOG.info("oCronExpression__EveryNight_Deep...");
            CronExpression oCronExpression__EveryNight_Deep = new CronExpression("0 30 9 1/1 * ?");  
            LOG.info("oCronExpression__EveryNight_Deep.setCronExpression...");
            oCronTrigger_EveryNight_Deep.setCronExpression(oCronExpression__EveryNight_Deep);
        } catch (Exception oException) {
            LOG.error("FAIL: ", oException.getMessage());
            LOG.debug("FAIL: ", oException);
        }
        if (!generalConfig.isSelfTest() && !"https://prod-double-region.tech.igov.org.ua".equalsIgnoreCase(generalConfig.getSelfHost())) {
            LOG.info("scheduleJob...");
            scheduler.scheduleJob(oJobDetail_PaymentProcessor_Standart, oCronTrigger_EveryNight_Deep);
        } else {
            LOG.info("scheduleJob... SKIPED(test)!");
        }
    }

    /**
     * Инициализация джобов, которые проверяют протокол {@link org.igov.model.action.launch.Launch} вызова методов,
     * через {@link org.igov.service.business.launch.LaunchService#processLaunchers(Integer)} и инициализируют повторный
     * вызов в зависимости от условий.
     */
    private void addProcessLaunchersJob(Scheduler scheduler) throws SchedulerException {
        //джоб проверяет каждые 10 минут методы, которые запустились с ошибкой и вызывались не более 3 раз
        JobDetail oJobDetail_ProcessLaunchers_Standart = new JobDetail("oJobDetail_ProcessLaunchers_Standart",
                "oJobDetail_ProcessLaunchers_Group", JobProcessLaunchers.class);
        CronTrigger oCronTrigger_Every_10_minutes = new CronTrigger("oCronTrigger_Every_10_minutes",
                "oCronTrigger_ProcessLaunchers");
        //джоб раз в сутки в 5 утра проверяет методы, которые запустились с ошибкой и вызывались не более 4 раз
        JobDetail oJobDetail_ProcessLaunchers_Night = new JobDetail("oJobDetail_ProcessLaunchers_Night",
                "oJobDetail_ProcessLaunchers_Group", JobProcessLaunchers.class);
        CronTrigger oCronTrigger_Every_Night = new CronTrigger("oCronTrigger_Every_Night",
                "oCronTrigger_ProcessLaunchers");
        try {
            CronExpression oCronExpression = new CronExpression("0 */10 * ? * *");
            oCronTrigger_Every_10_minutes.setCronExpression(oCronExpression);

            oCronExpression = new CronExpression("0 0 5 * * ?");
            oCronTrigger_Every_Night.setCronExpression(oCronExpression);
        } catch (ParseException oException) {
            LOG.error("Error during parse CronExpression", oException);
        }
        scheduler.scheduleJob(oJobDetail_ProcessLaunchers_Standart, oCronTrigger_Every_10_minutes);
        scheduler.scheduleJob(oJobDetail_ProcessLaunchers_Night, oCronTrigger_Every_Night);
        LOG.info("JobProcessLaunchers added!");
    }
    
    private void addStaffSyncJob(Scheduler scheduler) throws SchedulerException {
        JobDetail oJobDetail_StaffSync = new JobDetail("oJobDetail_StaffSync",
                "oJobDetail_StaffSync", JobStaffSync.class);

        CronTrigger oCronTrigger_StaffSync = new CronTrigger("oCronTrigger_StaffSync",
                "oCronTrigger_StaffSync");
        try {
            LOG.info("oCronExpression__StaffSync...");
            CronExpression oCronExpression_StaffSync = new CronExpression("0 */10 * ? * *");   
            LOG.info("oCronExpression__StaffSync.setCronExpression...");
            oCronTrigger_StaffSync.setCronExpression(oCronExpression_StaffSync);
        } catch (Exception oException) {
            LOG.error("FAIL: ", oException.getMessage());
            LOG.debug("FAIL: ", oException);
        }
        
        scheduler.scheduleJob(oJobDetail_StaffSync, oCronTrigger_StaffSync);
    }

    private void addUBStaffSyncJob(Scheduler scheduler) throws SchedulerException {
        JobDetail oJobDetail = new JobDetail("oJobDetail_UBStaffSync",
                "oJobDetail_UBStaffSync", JobUBStaffSync.class);

        CronTrigger oCronTrigger = new CronTrigger("oCronTrigger_UBStaffSync",
                "oCronTrigger_UBStaffSync");

        try {
            oCronTrigger.setCronExpression(everyMidnight());
        } catch (Exception oException) {
            LOG.error("set cron expression error", oException);
        }

        scheduler.scheduleJob(oJobDetail, oCronTrigger);
    }

    private static CronExpression everyMidnight() throws ParseException {
        return new CronExpression("0 0 0 * * ?");
    }

}
