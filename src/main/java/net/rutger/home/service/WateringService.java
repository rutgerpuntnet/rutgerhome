package net.rutger.home.service;

import net.rutger.home.domain.GardenArduino;
import net.rutger.home.domain.WateringJobData;
import net.rutger.home.repository.WateringJobDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class WateringService {
    private final static Logger LOG = LoggerFactory.getLogger(WateringService.class);

    @Value("${garden.arduino.url}")
    private String arduinoUrl;

    @Value("${watering.interval.minutes}")
    private int interval;

    @Value("${watering.max.duration.minutes}")
    private int maxDuration;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WateringJobDataRepository wateringJobDataRepository;

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

            try {
                final GardenArduino gardenArduinoResult = this.restTemplate.getForObject(arduinoUrl, GardenArduino.class, minutes);
                LOG.debug("Called gardenArduino. Result: {}", gardenArduinoResult);
                wateringJobData.setNextRun(LocalDateTime.now().plusMinutes(minutes).plusMinutes(interval).minusSeconds(10));
                wateringJobDataRepository.save(wateringJobData);
            } catch (Exception e) {
                LOG.error("Exception while calling Arduino:", e);
            }
        } else {
            LOG.trace("No WateringJob found for now.");
        }
    }
}
