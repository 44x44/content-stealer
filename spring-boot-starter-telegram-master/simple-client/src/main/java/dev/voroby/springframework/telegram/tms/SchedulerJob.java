package dev.voroby.springframework.telegram.tms;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Slf4j
public abstract class SchedulerJob implements Job {
    public abstract void call() throws Exception;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info(
            "Job Started {} at {}",
            context.getJobDetail().getKey().getName(),
            context.getFireTime()
        );
        try {
            call();
        } catch (Exception ex) {
            log.error("Job Failed " + context.getJobDetail().getKey().getName(), ex);
            throw new JobExecutionException(
                "Job Failed " + context.getJobDetail().getKey().getName(), ex
            );
        }
        log.info(
            "Job Finished {} Next job scheduled {}",
            context.getJobDetail().getKey().getName(),
            context.getNextFireTime()
        );
    }
}
