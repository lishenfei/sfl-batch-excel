package com.lsf.batch.excel.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

/**
 * Job Execution Listener
 * Created by lishenfei on 2016-12-15.
 */
@Slf4j
public class BatchJobListener implements JobExecutionListener {

    private long startTime;
    private long endTime;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        startTime = System.currentTimeMillis();
        log.info("Begin to execute batch task, {}", jobExecution.toString());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        endTime = System.currentTimeMillis();
        log.info("End of the batch task processing, time: {}ms, {}", (endTime - startTime), jobExecution.toString());
    }

}
