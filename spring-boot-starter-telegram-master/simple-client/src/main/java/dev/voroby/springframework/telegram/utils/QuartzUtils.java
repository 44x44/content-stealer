package dev.voroby.springframework.telegram.utils;

import dev.voroby.springframework.telegram.config.QuartzConfig;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class QuartzUtils {
    private QuartzUtils() {
    }

    public static SimpleTriggerFactoryBean createTrigger(JobDetail jobDetail,
                                                         int pollFrequencyMinutes,
                                                         String triggerName) {
        log.info(
            "createTrigger(jobDetail={}, pollFrequencyMinutes={}, triggerName={})",
            jobDetail.toString(), pollFrequencyMinutes, triggerName
        );
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        if (pollFrequencyMinutes == QuartzConfig.NOT_START_JOB_MINUTES) {
            factoryBean.setStartDelay(TimeUnit.MINUTES.toMillis(pollFrequencyMinutes));
        } else {
            factoryBean.setStartDelay(0L);
        }
        factoryBean.setRepeatInterval(
            TimeUnit.MINUTES.toMillis(pollFrequencyMinutes)
        );
        factoryBean.setName(triggerName);
        factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        factoryBean.setMisfireInstruction(
            SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT
        );
        return factoryBean;
    }

    public static CronTriggerFactoryBean createCronTrigger(
        JobDetail jobDetail, String cronExpression, String triggerName
    ) {
        log.info(
            "createCronTrigger(jobDetail={}, cronExpression={}, triggerName={})",
            jobDetail.toString(), cronExpression, triggerName
        );
        // To fix an issue with time-based cron jobs
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setCronExpression(cronExpression);
        factoryBean.setStartTime(calendar.getTime());
        factoryBean.setStartDelay(0L);
        factoryBean.setName(triggerName);
        factoryBean.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
        return factoryBean;
    }

    public static JobDetailFactoryBean createJobDetail(Class<? extends Job> jobClass, String description) {
        log.debug("createJobDetail(jobClass={}, jobName={})", jobClass.getName(), jobClass.getName());
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setName(jobClass.getSimpleName());
        factoryBean.setDescription(description);
        factoryBean.setJobClass(jobClass);
        factoryBean.setDurability(true);
        return factoryBean;
    }
}
