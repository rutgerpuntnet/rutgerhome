package net.rutger.home.service;

import net.rutger.home.domain.GardenArduino;
import net.rutger.home.domain.WateringAction;
import net.rutger.home.repository.WateringActionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @deprecated This service implementation is deprecated but still functional as long as there is a arduino listening
 * Yet it doesn't support upper and lower valve, so ONLY lower valve will be implemented
 */
@Service
@Profile("arduino")
public class GardenArduinoService implements WaterValveService {
    private final static Logger LOG = LoggerFactory.getLogger(GardenArduinoService.class);

    @Value("${garden.arduino.url}")
    private String arduinoUrl;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WateringActionRepository wateringActionRepository;

    private GardenArduino call(final Integer minutes) {
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

    @Override
    public void openLowerValve(final long seconds) {
        // first translate seconds to minutes
        final int minutes = Math.round(seconds/60);
        call(minutes);
    }

    @Override
    public void openUpperValve(final long seconds) {
        LOG.warn("Upper valve not applicable in arduino setup. This call is ignored");
    }
}
