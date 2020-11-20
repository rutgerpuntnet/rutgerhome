package net.rutger.home.service;

import net.rutger.home.domain.GardenArduino;
import net.rutger.home.domain.StaticWateringData;
import net.rutger.home.domain.WateringJobData;
import net.rutger.home.repository.WateringJobDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class WateringService {
    private final static Logger LOG = LoggerFactory.getLogger(WateringService.class);

    @Value("${watering.interval.minutes}")
    private int interval;

    @Value("${watering.max.duration.minutes}")
    private int maxDuration;

    @Autowired
    private GardenArduinoService gardenArduinoService;

    @Autowired
    private WateringJobDataRepository wateringJobDataRepository;

    public void executeWateringAction() {
        final WateringJobData wateringJobData = wateringJobDataRepository.findFirstActiveWateringJob(LocalDate.now(),LocalDateTime.now());

        if (wateringJobData != null) {
            LOG.debug("Found WateringJob: {}", wateringJobData);

            final StaticWateringData staticWateringData = wateringJobData.getStaticWateringData();
            if (staticWateringData == null) {
                LOG.debug("No static watering data found. Use default interval ({}) and max ({}) values", interval, maxDuration);
            } else {
                this.interval = staticWateringData.getIntervalMinutes();
                this.maxDuration = staticWateringData.getMaxDurationMinutes();
            }

            final int minutesLeft = wateringJobData.getMinutesLeft();
            int minutes;
            if (minutesLeft > maxDuration) {
                minutes = maxDuration;
                wateringJobData.setMinutesLeft(minutesLeft - maxDuration);
                wateringJobData.setNextRun(LocalDateTime.now().plusMinutes(minutes).plusMinutes(interval).minusSeconds(10));
            } else {
                minutes = minutesLeft;
                wateringJobData.setMinutesLeft(0);
            }
            LOG.info("Watering job ID {} for {} minutes. MinutesLeft now {}", wateringJobData.getId(), minutes, wateringJobData.getMinutesLeft());
            final GardenArduino gardenArduinoResult = gardenArduinoService.call(minutes);
            LOG.debug("Called gardenArduino. Result: {}", gardenArduinoResult);
            wateringJobDataRepository.save(wateringJobData);
        } else {
            LOG.trace("No WateringJob found for now.");
        }
    }
}
