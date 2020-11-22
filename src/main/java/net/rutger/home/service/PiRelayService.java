package net.rutger.home.service;

import com.pi4j.io.gpio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * See https://pi4j.com/1.2/pins/model-3b-plus-rev1.html for wiring
 *
 * control example https://pi4j.com/1.2/example/control.html
 */
@Service
@Profile("pi")
public class PiRelayService implements WaterValveService {
    private final static Logger LOG = LoggerFactory.getLogger(PiRelayService.class);
    final GpioPinDigitalOutput upperValvePin;
    final GpioPinDigitalOutput lowerValvePin;

    public PiRelayService() {
        final GpioController gpio = GpioFactory.getInstance();
        upperValvePin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "UpperValve", PinState.LOW);
        lowerValvePin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "LowerValve", PinState.LOW);
    }

    @Override
    public void openLowerValve(long seconds) {
        LOG.info("Open lower water valve for {} seconds", seconds);
        lowerValvePin.pulse(seconds * 1000, PinState.HIGH);
    }

    @Override
    public void openUpperValve(long seconds) {
        LOG.info("Open upper water valve for {} seconds", seconds);
        upperValvePin.pulse(seconds * 1000, PinState.HIGH);
    }
}
