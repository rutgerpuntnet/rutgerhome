package net.rutger.home.service;

import net.rutger.home.domain.*;
import net.rutger.home.repository.WateringJobDataRepository;
import net.rutger.home.repository.WateringJobEnforceDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
public class WateringJobService {
    private final static Logger LOG = LoggerFactory.getLogger(WateringJobService.class);

    @Autowired
    private StaticWateringDataService staticWateringDataService;

    @Autowired
    private KnmiService knmiService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private WateringJobDataRepository wateringJobDataRepository;

    @Autowired
    private WateringJobEnforceDataRepository wateringJobEnforceDataRepository;

    public void checkWateringJob(final boolean finalRun) {
        LOG.debug("Check watering job");
        final WateringJobData wateringJobDataToday = wateringJobDataRepository.findLatestTodaysNonManualJob(LocalDate.now());
        if (wateringJobDataToday == null) {
            LOG.info("Running watering job (no existing found for today");
            // determine if KNMI data is available
            final Optional<Map<WeatherDataType, Double>> weatherData = knmiService.getDataFromPreviousDay();
            if (weatherData.isPresent() || finalRun) {
                createAndStoreWateringJobData(weatherData);
            } else {
                LOG.debug("No data retrieved for this intermediate run, skipping task this time.");
            }
        } else {
            LOG.trace("Found existing job in database (will not execute): {}", wateringJobDataToday);
        }
    }


    /**
     * Execute the actual task (when applicable)
     */
    private void createAndStoreWateringJobData(final Optional<Map<WeatherDataType, Double>> weatherData) {
        final WateringJobEnforceData enforceData = wateringJobEnforceDataRepository.findFirstByLocalDate(LocalDate.now());
        final StaticWateringData staticData = staticWateringDataService.getLatest();
        final int minutes = determineMinutes(weatherData, enforceData, staticData);

        final WateringJobData wateringJobData = new WateringJobData(weatherData, minutes,
                enforceData == null ? WateringJobType.AUTO : WateringJobType.ENFORCED, staticData, enforceData);

        wateringJobDataRepository.save(wateringJobData);
        LOG.info("Saved new WateringJobData: {}", wateringJobData);
        emailService.emailWateringResult(wateringJobData);
    }

    private int determineMinutes(final Optional<Map<WeatherDataType, Double>> incomingWeatherData,
                                 final WateringJobEnforceData enforceData, final StaticWateringData staticData) {
        LOG.debug("Determin minutes using staticData: {} ", staticData.toString());
        int result = staticData.getDefaultMinutes();
        if (enforceData != null && enforceData.getNumberOfMinutes() != null) {
            LOG.info("We have enforcement number of minutes for this day. Being: {} minutes", enforceData.getNumberOfMinutes());
            result = enforceData.getNumberOfMinutes();
        } else {
            if (incomingWeatherData.isPresent() && incomingWeatherData.get().get(WeatherDataType.EV24) != null
                    && incomingWeatherData.get().get(WeatherDataType.RH) != null) {
                final Double makkink = incomingWeatherData.get().get(WeatherDataType.EV24);
                final Double totalPrecip = incomingWeatherData.get().get(WeatherDataType.RH);
                final Double supplementMillimeters = makkink - totalPrecip + staticData.getInitialMm();
                if (supplementMillimeters > 0) {
                    final Double factor;
                    if (enforceData != null && enforceData.getMultiplyFactor() != null) {
                        LOG.info("We have enforcement data for this day. The result will be multiplied by {}", enforceData.getMultiplyFactor());
                        factor = enforceData.getMultiplyFactor();
                    } else {
                        factor = staticData.getFactor();
                    }

                    result = Math.toIntExact(Math.round((supplementMillimeters * staticData.getMinutesPerMm()) * factor));

                    // now that we have a result number of minutes. Be sure it's not larger than our daily limit
                    result = result > staticData.getDailyLimitMinutes() ? staticData.getDailyLimitMinutes() : result;
                } else {
                    result = 0;
                }
            }
        }
        return result;
    }
}
