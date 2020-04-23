package net.rutger.home.service;

import net.rutger.home.domain.GardenArduino;
import net.rutger.home.domain.WateringJobData;
import net.rutger.home.domain.WeatherDataType;
import net.rutger.home.repository.WateringJobDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WateringJobService {
    private final static Logger LOG = LoggerFactory.getLogger(WateringJobService.class);

    private final static double MILLIMETER_PER_MINUTE = 0.3;

    @Value("${garden.arduino.url}")
    private String arduinoUrl;

    @Value("${watering.minutes.default}")
    private int defaultWateringMinutes;

    @Value("${watering.initial.mm}")
    private int initialMillimeters;

    @Value("${watering.minutes.maximum}")
    private int maxWateringMinutes;

    @Value("${email.sender}")
    private String emailSenderAddress;

    @Value("#{'${email.recipients}'.split(',')}")
    private List<String> emailRecipients;

    private LocalDate latestWatering = LocalDate.MIN;

    private Map<WeatherDataType, Double> latestWeatherData = Collections.emptyMap();

    @Autowired
    private KnmiService knmiService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private WateringJobDataRepository wateringJobDataRepository;

    public void checkWateringTask(final boolean finalRun) {
        LOG.debug("Checking watering task");
        if (latestWatering.isBefore(LocalDate.now())) {
            LOG.debug("Running watering task");
            // determine if KNMI data is available
            final Optional<Map<WeatherDataType, Double>> weatherData = knmiService.getDataFromPreviousDay();
            if (weatherData.isPresent() || finalRun) {
                latestWeatherData = weatherData.isPresent() ? weatherData.get() : Collections.emptyMap();
                executeWateringTask(weatherData);
            } else {
                LOG.debug("No data retrieved for this intermediate run, skipping task this time.");
            }
        } else {
            LOG.debug("Watering task already executed today");
        }
    }


    /**
     * Execute the actual task (when applicable)
     */
    private void executeWateringTask(final Optional<Map<WeatherDataType, Double>> weatherData) {
        final int minutes = determineMinutes(weatherData);
        final WateringJobData wateringJobData = new WateringJobData(weatherData, minutes);
        wateringJobData.setMinutesLeft(minutes);
        wateringJobDataRepository.save(wateringJobData);

        executeWateringJob(wateringJobData);
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

    private void executeWateringJob(final WateringJobData wateringJobData) {
            // call arduino

        // reset date marker
        latestWatering = LocalDate.now();

        if (wateringJobData.getMinutesLeft()>0) {
            final int minutes = wateringJobData.getMinutesLeft();

            final GardenArduino gardenArduino = this.restTemplate.getForObject(arduinoUrl, GardenArduino.class, minutes);
            LOG.debug("Called gardenArduino. Result: {}", gardenArduino.toString());

            wateringJobDataRepository.save(wateringJobData);
            // send email
            emailWateringResult(wateringJobData, gardenArduino);
        } else {
            emailWateringResult(wateringJobData, null);
        }


    }

    private void emailWateringResult(final WateringJobData wateringJobData, final GardenArduino gardenArduino) {
        LOG.debug("Sending email with watering results.");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailSenderAddress);
        message.setTo(emailRecipients.toArray(new String[emailRecipients.size()]));
        message.setSubject("Tuinsproeisysteem dagrapport");
        if (gardenArduino == null) {
            message.setText(String.format("Er wordt vandaag NIET gesproeid.\n" +
                    "De hoeveelheid neerslag was groter of gelijk aan de verdamping van het water vermeerderd met %d mm,", initialMillimeters));
        } else {
            if(wateringJobData.getMakkinkIndex() == null) {
                message.setText(String.format("De tuinsproeier zal vandaag voor %d minuten sproeien.\n" +
                        "De KNMI data is helaas niet succesvol ontvangen waardoor is gekozen voor de standaardduur." +
                        "Hieronder volgt het antwoord van de arduino:\n%s", wateringJobData.getNumberOfMinutes(), gardenArduino.toString()));
            } else {
                message.setText(String.format("De tuinsproeier zal vandaag voor %d minuten sproeien.\n" +
                                "Dit is gebaseerd op de volgende gegevens van de afgelopen dag:\n" +
                                "%d mm initiele wateraanvulling, %f mm verdamping en %f mm neerslag.\n" +
                                "Dit geeft een totale aanvulling van %f mm water, tegen een factor van %f mm sproeien per minuut" +
                                "Hieronder volgt het antwoord van de arduino:\n%s", wateringJobData.getNumberOfMinutes(), initialMillimeters,
                        wateringJobData.getMakkinkIndex(), wateringJobData.getPrecipitation(),
                        gardenArduino.toString()));
            }
        }
        emailSender.send(message);
        LOG.debug("Email sent");
    }

    public Map<WeatherDataType, Double> getLatestWeatherData(){
        return latestWeatherData;
    }
}
