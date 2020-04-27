package net.rutger.home.service;

import net.rutger.home.domain.GardenArduino;
import net.rutger.home.domain.WateringAction;
import net.rutger.home.domain.WateringJobData;
import net.rutger.home.repository.WateringActionRepository;
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

    @Autowired
    private WateringActionRepository wateringActionRepository;

    public void executeWateringAction() {
        final WateringJobData wateringJobData = wateringJobDataRepository.findFirstActiveWateringJob(LocalDate.now(),LocalDateTime.now());

        if (wateringJobData != null) {
            LOG.debug("Found WateringJob: {}", wateringJobData);

            final int minutesLeft = wateringJobData.getMinutesLeft();
            int minutes;
            if (minutesLeft > maxDuration) {
                minutes = maxDuration;
                wateringJobData.setMinutesLeft(minutesLeft - maxDuration);
            } else {
                minutes = minutesLeft;
                wateringJobData.setMinutesLeft(0);
            }
            LOG.info("Watering job ID {} for {} minutes. MinutesLeft now {}", wateringJobData.getId(), minutes, wateringJobData.getMinutesLeft());
            final GardenArduino gardenArduinoResult = gardenArduinoService.call(minutes);
            wateringActionRepository.save(new WateringAction(minutes));
            LOG.debug("Called gardenArduino. Result: {}", gardenArduinoResult);
            wateringJobData.setNextRun(LocalDateTime.now().plusMinutes(minutes).plusMinutes(interval).minusSeconds(10));
            wateringJobDataRepository.save(wateringJobData);
        } else {
            LOG.trace("No WateringJob found for now.");
        }
    }
}
