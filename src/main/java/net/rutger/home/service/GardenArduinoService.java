package net.rutger.home.service;

import net.rutger.home.domain.GardenArduino;
import net.rutger.home.domain.WateringAction;
import net.rutger.home.repository.WateringActionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GardenArduinoService {
    private final static Logger LOG = LoggerFactory.getLogger(GardenArduinoService.class);

    @Value("${garden.arduino.url}")
    private String arduinoUrl;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WateringActionRepository wateringActionRepository;

    public GardenArduino call(final Integer minutes) {
        GardenArduino result = null;
        try {
            LOG.debug("Calling Arduino to set timer for {} minutes.", minutes);
            result = this.restTemplate.getForObject(arduinoUrl, GardenArduino.class, minutes);
            wateringActionRepository.save(new WateringAction(minutes));
        } catch (RuntimeException e) {
            LOG.error("Exception while calling garden arduino on URL " + arduinoUrl, e);
            emailService.emailArduinoException(e);
        }
        return result;
    }
}
