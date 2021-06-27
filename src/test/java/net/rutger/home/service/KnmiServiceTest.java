package net.rutger.home.service;

import net.rutger.home.domain.WeatherDataType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

public class KnmiServiceTest {
    private KnmiService knmiService = new KnmiService();

    @Test
    public void testGetDataFromZip(){
        final Optional<Map<WeatherDataType, Double>> result = knmiService.getDataFromPreviousDay();

        Assertions.assertTrue(LocalDateTime.now().getHour() < 9 || result.isPresent());
    }
}
