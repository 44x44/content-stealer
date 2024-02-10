package dev.voroby.springframework.telegram.config;

import dev.voroby.springframework.telegram.tms.jobs.StealContentJob;
import dev.voroby.springframework.telegram.utils.QuartzUtils;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;

@Configuration
public class QuartzConfig {
    private static final String MANUALLY_CRON = "0 0 0 ? 1 1 2099";
    public static final int NOT_START_JOB_MINUTES = 100000;
    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public JobDetailFactoryBean stealContentJob() {
        return QuartzUtils.createJobDetail(
            StealContentJob.class,
            "steal random post and post it"
        );
    }

    @Bean
    public CronTriggerFactoryBean transferTasksJobTriggerEveryMinute(
        @Qualifier("stealContentJob")JobDetail jobDetail) {
        return QuartzUtils.createCronTrigger(
            jobDetail, "0 0/1 * * * ?", "stealContentJob Trigger every minute"
        );
    }

    @Bean
    public SchedulerFactoryBean scheduler(DataSource dataSource, Trigger... triggers) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
        schedulerFactory.setConfigLocation(new ClassPathResource("quartz.properties"));
        schedulerFactory.setOverwriteExistingJobs(true);
        schedulerFactory.setAutoStartup(true);
        schedulerFactory.setDataSource(dataSource);
        schedulerFactory.setJobFactory(springBeanJobFactory());
        schedulerFactory.setWaitForJobsToCompleteOnShutdown(true);
        schedulerFactory.setTriggers(triggers);
        return schedulerFactory;
    }
}
