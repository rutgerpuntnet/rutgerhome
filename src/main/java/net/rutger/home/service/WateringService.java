package net.rutger.home.service;

import net.rutger.home.domain.StaticWateringData;
import net.rutger.home.domain.ValveType;
import net.rutger.home.domain.WateringJobData;
import net.rutger.home.repository.WateringJobDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * This service runs a continues interval and checks if there is an active wateringjob to handle
 */
@Service
public class WateringService {
    private final static Logger LOG = LoggerFactory.getLogger(WateringService.class);

    @Value("${watering.interval.minutes}")
    private int interval;

    @Value("${watering.max.duration.minutes}")
    private int maxDuration;

    @Autowired
    private WaterValveService waterValveService;

    @Autowired
    private WateringJobDataRepository wateringJobDataRepository;

    public void executeWateringAction() {
        final WateringJobData wateringJobData = wateringJobDataRepository.findFirstActiveWateringJob(LocalDate.now(),LocalDateTime.now());

        if (wateringJobData != null) {
            LOG.debug("Found WateringJob: {}", wateringJobData);
            int upperMinutes = determineMinutes(ValveType.UPPER, wateringJobData);
            int lowerMinutes = determineMinutes(ValveType.LOWER, wateringJobData);

            LOG.info("Watering job ID {} for {} minutes upper, {} minutes lower. MinutesLeft upper now {}, minutesLeft lower now {}",
                    wateringJobData.getId(), upperMinutes, lowerMinutes, wateringJobData.getMinutesLeftUpper(), wateringJobData.getMinutesLeftLower());
            waterValveService.openUpperValve(upperMinutes*60);
            waterValveService.openLowerValve(lowerMinutes*60);
            wateringJobDataRepository.save(wateringJobData);
        } else {
            LOG.trace("No WateringJob found for now.");
        }
    }

    private int determineMinutes(final ValveType valveType, final WateringJobData wateringJobData) {

        final StaticWateringData staticWateringData = getStaticWateringData(valveType, wateringJobData);
        if (staticWateringData == null) {
            LOG.debug("No static watering data found for {} valve. Use default interval ({}) and max ({}) values",
                    valveType, interval, maxDuration);
        } else {
            this.interval = staticWateringData.getIntervalMinutes();
            this.maxDuration = staticWateringData.getMaxDurationMinutes();
        }

        final int minutesLeft = getMinutesLeft(valveType, wateringJobData);
        int minutes;
        if (minutesLeft > maxDuration) {
            minutes = maxDuration;
            setMinutesLeft(valveType, wateringJobData, minutesLeft - maxDuration);
            wateringJobData.setNextRun(LocalDateTime.now().plusMinutes(minutes).plusMinutes(interval).minusSeconds(10));
        } else {
            minutes = minutesLeft;
            setMinutesLeft(valveType, wateringJobData, 0);
        }
        return minutes;
    }

    private void setMinutesLeft(final ValveType valveType, final WateringJobData wateringJobData, final int minutes) {
        if (ValveType.UPPER.equals(valveType)) {
            wateringJobData.setMinutesLeftUpper(minutes);
        }
        if (ValveType.LOWER.equals(valveType)) {
            wateringJobData.setMinutesLeftLower(minutes);
        }
    }

    private int getMinutesLeft(final ValveType valveType, final WateringJobData wateringJobData) {
        if (ValveType.UPPER.equals(valveType)) {
            return wateringJobData.getMinutesLeftUpper();
        }
        if (ValveType.LOWER.equals(valveType)) {
            return wateringJobData.getMinutesLeftLower();
        }
        throw new IllegalStateException("Valvetype not supported");
    }

    private StaticWateringData getStaticWateringData(final ValveType valveType, final WateringJobData wateringJobData) {
        if (ValveType.UPPER.equals(valveType)) {
            return wateringJobData.getUpperStaticWateringData();
        }
        if (ValveType.LOWER.equals(valveType)) {
            return wateringJobData.getLowerStaticWateringData();
        }
        throw new IllegalStateException("Valvetype not supported");
    }
}
