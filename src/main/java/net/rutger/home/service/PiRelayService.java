package net.rutger.home.service;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import net.rutger.home.service.WaterValveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * See https://pi4j.com/1.2/pins/model-3b-plus-rev1.html for wiring
 *
 * control example https://pi4j.com/1.2/example/control.html
 */
@Service
@Profile("pi")
public class PiRelayService implements WaterValveService {
    private static final Logger LOG = LoggerFactory.getLogger(PiRelayService.class);

    private final DigitalOutput upperValvePin;
    private final DigitalOutput lowerValvePin;
    private final ScheduledExecutorService executor;

    public PiRelayService() {
        // Maak de automatische Pi4J context aan (detecteert je Pi hardware)
        Context pi4j = Pi4J.newAutoContext();

        // Executor initialiseren voor de 'pulse' (timer) functionaliteit
        this.executor = Executors.newSingleThreadScheduledExecutor();

        // Configureer de Bovenste Klep (was GPIO_01 -> BCM 18)
        var upperConfig = DigitalOutput.newConfigBuilder(pi4j)
                .id("upper-valve")
                .name("UpperValve")
                .address(18) // LET OP: Pi4J V3 gebruikt Broadcom (BCM) pin nummers!
                .initial(DigitalState.LOW)
                .shutdown(DigitalState.LOW)
                .build();
        this.upperValvePin = pi4j.create(upperConfig);

        // Configureer de Onderste Klep (was GPIO_04 -> BCM 23)
        var lowerConfig = DigitalOutput.newConfigBuilder(pi4j)
                .id("lower-valve")
                .name("LowerValve")
                .address(23) // LET OP: Pi4J V3 gebruikt Broadcom (BCM) pin nummers!
                .initial(DigitalState.LOW)
                .shutdown(DigitalState.LOW)
                .build();
        this.lowerValvePin = pi4j.create(lowerConfig);
    }

    @Override
    public void openLowerValve(long seconds) {
        LOG.info("Open lower water valve for {} seconds", seconds);
        pulsePin(lowerValvePin, seconds);
    }

    @Override
    public void openUpperValve(long seconds) {
        LOG.info("Open upper water valve for {} seconds", seconds);
        pulsePin(upperValvePin, seconds);
    }

    /**
     * Helper methode om de oude .pulse() functionaliteit na te bootsen.
     * Zet de pin HIGH, en plant een taak in om hem na X seconden weer LOW te zetten.
     */
    private void pulsePin(DigitalOutput pin, long seconds) {
        pin.high();
        executor.schedule(() -> {
            pin.low();
            LOG.info("Closed valve on pin {}", pin.address());
        }, seconds, TimeUnit.SECONDS);
    }
}