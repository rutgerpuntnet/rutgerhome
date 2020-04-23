package net.rutger.home.service;

import net.rutger.home.domain.WateringJobData;
import net.rutger.home.domain.WeatherDataType;
import net.rutger.home.repository.WateringJobDataRepository;
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

    private LocalDate latestWatering = LocalDate.MIN;

    @Value("${watering.minutes.default}")
    private int defaultWateringMinutes;

    @Value("${watering.initial.mm}")
    private int initialMillimeters;

    @Value("${watering.minutes.maximum}")
    private int maxWateringMinutes;

    @Autowired
    private KnmiService knmiService;

    @Autowired
    private WateringJobDataRepository wateringJobDataRepository;

    public void checkWateringJob(final boolean finalRun) {
        LOG.debug("Executing watering job");
        final LocalDate now = LocalDate.now();
        if (latestWatering.isBefore(now)) {
            LOG.debug("Running watering job");
            // determine if KNMI data is available
            final Optional<Map<WeatherDataType, Double>> weatherData = knmiService.getDataFromPreviousDay();
            if (weatherData.isPresent() || finalRun) {
                createAndStoreWateringJobData(weatherData);
                latestWatering = now;
            } else {
                LOG.debug("No data retrieved for this intermediate run, skipping task this time.");
            }
        } else {
            LOG.debug("Watering job already executed today");
        }
    }


    /**
     * Execute the actual task (when applicable)
     */
    private void createAndStoreWateringJobData(final Optional<Map<WeatherDataType, Double>> weatherData) {
        final int minutes = determineMinutes(weatherData);
        final WateringJobData wateringJobData = new WateringJobData(weatherData, minutes);
        wateringJobData.setMinutesLeft(minutes);
        wateringJobDataRepository.save(wateringJobData);
    }

    private int determineMinutes(final Optional<Map<WeatherDataType, Double>> incomingWeatherData) {
        int result = defaultWateringMinutes;
        if (incomingWeatherData.isPresent() && incomingWeatherData.get().get(WeatherDataType.EV24) != null
                && incomingWeatherData.get().get(WeatherDataType.RH) != null) {
            final Double makkink = incomingWeatherData.get().get(WeatherDataType.EV24);
            final Double totalPrecip = incomingWeatherData.get().get(WeatherDataType.RH);
            final Double mmLost = makkink - totalPrecip;
            if (mmLost > 0) {
                result = Math.toIntExact(Math.round((mmLost + initialMillimeters) / MILLIMETER_PER_MINUTE));
            } else {
                result = 0;
            }
        }
        return result > maxWateringMinutes ? maxWateringMinutes : result;
    }

    public WateringJobData getLatestWateringJobData(){
        return null; // TODO wateringJobDataRepository.findOne();
    }
}
