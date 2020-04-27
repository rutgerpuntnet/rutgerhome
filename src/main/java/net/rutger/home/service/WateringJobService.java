package net.rutger.home.service;

import net.rutger.home.domain.WateringJobData;
import net.rutger.home.domain.WateringJobEnforceData;
import net.rutger.home.domain.WateringJobType;
import net.rutger.home.domain.WeatherDataType;
import net.rutger.home.repository.WateringJobDataRepository;
import net.rutger.home.repository.WateringJobEnforceDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
public class WateringJobService {
    private final static Logger LOG = LoggerFactory.getLogger(WateringJobService.class);

    private final static double MILLIMETER_PER_MINUTE = 0.3;

    @Value("${watering.minutes.default}")
    private int defaultWateringMinutes;

    @Value("${watering.initial.mm}")
    private int initialMillimeters;

    @Value("${watering.daily.limit.minutes}")
    private int dailyLimitMinutes;

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
        final WateringJobData wateringJobDataToday = wateringJobDataRepository.findFirstByLocalDate(LocalDate.now());
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
            LOG.debug("Found existing job in database (will not execute): {}", wateringJobDataToday);
        }
    }


    /**
     * Execute the actual task (when applicable)
     */
    private void createAndStoreWateringJobData(final Optional<Map<WeatherDataType, Double>> weatherData) {
        final WateringJobEnforceData enforceData = wateringJobEnforceDataRepository.findFirstByLocalDate(LocalDate.now());
        final int minutes = determineMinutes(weatherData, enforceData);

        final WateringJobData wateringJobData = new WateringJobData(weatherData, minutes,
                enforceData == null ? WateringJobType.AUTO : WateringJobType.ENFORCED);

        wateringJobDataRepository.save(wateringJobData);
        LOG.info("Saved new WateringJobData: {}", wateringJobData);
        emailService.emailWateringResult(wateringJobData);
    }

    private int determineMinutes(final Optional<Map<WeatherDataType, Double>> incomingWeatherData, final WateringJobEnforceData enforceData) {
        int result = defaultWateringMinutes;
        if (enforceData != null && enforceData.getNumberOfMinutes() != null) {
            LOG.info("We have enforcement data for this day. The result will a fixed number of minutes: {}", enforceData.getNumberOfMinutes());
            result = enforceData.getNumberOfMinutes();
        } else {
            if (incomingWeatherData.isPresent() && incomingWeatherData.get().get(WeatherDataType.EV24) != null
                    && incomingWeatherData.get().get(WeatherDataType.RH) != null) {
                final Double makkink = incomingWeatherData.get().get(WeatherDataType.EV24);
                final Double totalPrecip = incomingWeatherData.get().get(WeatherDataType.RH);
                final Double mmLost = makkink - totalPrecip;
                if (mmLost > 0) {
                    if (enforceData != null && enforceData.getMultiplyFactor() != null) {
                        LOG.info("We have enforcement data for this day. The result will be multiplied by {}", enforceData.getMultiplyFactor());
                        result = Math.toIntExact(Math.round(((mmLost + initialMillimeters) / MILLIMETER_PER_MINUTE) * enforceData.getMultiplyFactor()));
                    } else {
                        result = Math.toIntExact(Math.round((mmLost + initialMillimeters) / MILLIMETER_PER_MINUTE));
                    }
                    result = result > dailyLimitMinutes ? dailyLimitMinutes : result;
                } else {
                    result = 0;
                }
            }
        }
        return result;
    }
}
