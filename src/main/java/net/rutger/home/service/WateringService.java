package net.rutger.home.service;

import net.rutger.home.domain.GardenArduino;
import net.rutger.home.domain.WateringJobData;
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
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WateringService {
    private final static Logger LOG = LoggerFactory.getLogger(WateringService.class);

    @Value("${garden.arduino.url}")
    private String arduinoUrl;

    @Value("${watering.initial.mm}")
    private int initialMillimeters;

    @Value("${email.sender}")
    private String emailSenderAddress;

    @Value("#{'${email.recipients}'.split(',')}")
    private List<String> emailRecipients;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private WateringJobDataRepository wateringJobDataRepository;

    public void executeWateringJob() {
        // TODO make properties
        long interval=5;
        long maxDuration=2;

        final LocalDateTime lastModifiedBefore = LocalDateTime.now().minusMinutes(interval).minusMinutes(maxDuration);

        final WateringJobData wateringJobData = wateringJobDataRepository.findActiveWateringJob(LocalDate.now(),lastModifiedBefore);

        if (wateringJobData != null) {
            final int minutes = wateringJobData.getMinutesLeft();

            final GardenArduino gardenArduinoResult = this.restTemplate.getForObject(arduinoUrl, GardenArduino.class, minutes);
            LOG.debug("Called gardenArduino. Result: {}", gardenArduinoResult);

            //TODO update minutes left and save wateringJobDataRepository.save(wateringJobData);
            emailWateringResult(wateringJobData, gardenArduinoResult);
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
}
