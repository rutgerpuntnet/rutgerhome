package net.rutger.home.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulerTasks {
    private static final Logger log = LoggerFactory.getLogger(SchedulerTasks.class);

    @Autowired
    private WateringJobService wateringJobService;

    /**
     * Run the watering task every 5 minutes between 8 and 9.
     * We expect the result of the KNMI data to be available somewhere during this timewindow.
     */
    @Scheduled(cron = "${schedule.watering.cron}")
    public void runGardenWateringJobTask() {
        log.info("run GardenWateringTask");
        wateringJobService.checkWateringTask(false);
    }

    /**
     * Run the watering task for the last time today (this run should take care that the execution has always
     * been done once a day
     */
    @Scheduled(cron = "${schedule.watering.final.cron}")
    public void runFinalGardenWateringJobTask() {
        log.info("run final GardenWateringTask");
        wateringJobService.checkWateringTask(true);
    }
}
