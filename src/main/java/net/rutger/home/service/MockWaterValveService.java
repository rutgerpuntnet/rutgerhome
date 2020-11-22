package net.rutger.home.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("local")
public class MockWaterValveService implements WaterValveService {
    private final static Logger LOG = LoggerFactory.getLogger(MockWaterValveService.class);

    @Override
    public void openLowerValve(long seconds) {
        LOG.info("Virtually opening the LOWER water valve for {} seconds", seconds);
    }

    @Override
    public void openUpperValve(long seconds) {
        LOG.info("Virtually opening the UPPER water valve for {} seconds", seconds);
    }
}
