package net.rutger.home.service;

import net.rutger.home.domain.GardenArduino;
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

    public GardenArduino call(final Integer minutes) {
        GardenArduino result = null;
        try {
            result = this.restTemplate.getForObject(arduinoUrl, GardenArduino.class, minutes);
        } catch (RuntimeException e) {
            LOG.error("Exception while calling garden arduino on URL " + arduinoUrl, e);
            emailService.emailArduinoException(e);
        }
        return result;
    }
}
