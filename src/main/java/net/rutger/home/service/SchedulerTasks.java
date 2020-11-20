package net.rutger.home.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulerTasks {
    private static final Logger log = LoggerFactory.getLogger(SchedulerTasks.class);

    @Autowired
    private WateringJobService wateringJobService;

    @Autowired
    private WateringService wateringService;

    @EventListener
    public void onStartup(ApplicationReadyEvent event) {
        log.info("Application started. Scheduler set.");
    }

    /**
     * Run the wateringjob task every 5 minutes between 8 and 9.
     * We expect the result of the KNMI data to be available somewhere during this timewindow.
     */
    @Scheduled(cron = "${schedule.watering.job.cron}")
    public void runGardenWateringJobTask() {
        log.trace("run GardenWateringJobTask");
        wateringJobService.checkWateringJob(false);
    }

    /**
     * Run the wateringjob task for the last time today (this run should take care that the execution has always
     * been done once a day
     */
    @Scheduled(cron = "${schedule.watering.job.final.cron}")
    public void runFinalGardenWateringJobTask() {
        log.trace("run final GardenWateringJobTask");
        wateringJobService.checkWateringJob(true);
    }

    /**
     * Run the actual watering task that acts upon the results of the wateringJob
     */
    @Scheduled(fixedRate = 60000)
    public void runGardenWateringTask() {
        log.trace("run GardenWateringTask");
        wateringService.executeWateringAction();
    }
}
