package net.rutger.home.service;

import net.rutger.home.domain.GardenArduino;
import net.rutger.home.domain.WeatherDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WateringService {
    private final static Logger LOG = LoggerFactory.getLogger(WateringService.class);

    private final static double MILLIMETER_PER_MINUTE = 0.3;

    @Value("${garden.arduino.url}")
    private String arduinoUrl;

    @Value("${watering.minutes.default}")
    private int defaultWateringMinutes;

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
            LOG.debug("Wateringtask already executed today");
        }
    }


    /**
     * Execute the actual task (when applicable)
     */
    private void executeWateringTask(final Optional<Map<WeatherDataType, Double>> weatherData) {
        final long minutes = determineMinutes(weatherData);

        // call arduino
        if (minutes>0) {
            final GardenArduino gardenArduino = this.restTemplate.getForObject(arduinoUrl, GardenArduino.class, minutes);
            LOG.debug("Called gardenArduino. Result: {}", gardenArduino.toString());
            // send email
            sendWateringResult(weatherData, minutes, gardenArduino);
        } else {
            sendWateringResult(weatherData, minutes, null);
        }
        // reset date marker
        latestWatering = LocalDate.now();
    }

    private void sendWateringResult(Optional<Map<WeatherDataType, Double>> weatherData, long minutes, GardenArduino gardenArduino) {
        LOG.debug("Sending email with wateringresults.");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailSenderAddress);
        message.setTo(emailRecipients.toArray(new String[emailRecipients.size()]));
        message.setSubject("Tuinsproeisysteem dagrapport");
        if (gardenArduino == null) {
            message.setText(String.format("De tuinsproeier heeft zojuist NIET gesproeid.\n" +
                    "Dit is gebaseerd op de volgende KNMI gegevens:\n%s\n\n\n" +
                    "De hoeveelheid neerslag was groter of gelijk aan de verdamping van het water:\n%s", printWeatherData(weatherData)));
        } else {
            if(weatherData.isPresent()) {
                message.setText(String.format("De tuinsproeier heeft zojuist voor %d minuten gesproeid.\n" +
                        "Dit is gebaseerd op de volgende KNMI gegevens:\n%s\n\n\n" +
                        "Hieronder volgt de response van de arduino:\n%s", minutes, printWeatherData(weatherData), gardenArduino.toString()));
            } else {
                message.setText(String.format("De tuinsproeier heeft zojuist voor %d minuten gesproeid.\n" +
                        "De KNMI data is het afgelopen uur helaas niet succesvol ontvangen waardoor is gekozen voor de standaardwaarde." +
                        "Hieronder volgt de response van de arduino:\n%s", minutes, gardenArduino.toString()));
            }
        }
        emailSender.send(message);
        LOG.debug("Email sent");
    }

    private String printWeatherData(Optional<Map<WeatherDataType, Double>> weatherData) {
        StringBuilder sb = new StringBuilder();
        if (weatherData.isPresent()) {
            final DecimalFormat formatter = new DecimalFormat("#0.00");
            for(WeatherDataType key : weatherData.get().keySet()) {
                sb.append(key.name());
                sb.append(": ");
                sb.append(formatter.format(weatherData.get().get(key)));
                sb.append(". ");
                sb.append(key.getDescription());
                sb.append("\n");
            }
        } else {
            sb.append("No weatherdata available");
        }
        return sb.toString();

    }

    private long determineMinutes(final Optional<Map<WeatherDataType, Double>> incomingWeatherData) {
        long result = defaultWateringMinutes;
        if (incomingWeatherData.isPresent() && incomingWeatherData.get().get(WeatherDataType.EV24) != null
                && incomingWeatherData.get().get(WeatherDataType.RH) != null) {
            final Double makkink = incomingWeatherData.get().get(WeatherDataType.EV24);
            final Double totalPrecip = incomingWeatherData.get().get(WeatherDataType.RH);
            final Double mmLost = makkink - totalPrecip;
            if (mmLost > 0) {
                result = Math.round(mmLost / MILLIMETER_PER_MINUTE);
            } else {
                result = 0;
            }
        }
        return result > maxWateringMinutes ? maxWateringMinutes : result;
    }

    public Map<WeatherDataType, Double> getLatestWeatherData(){
        return latestWeatherData;
    }
}
