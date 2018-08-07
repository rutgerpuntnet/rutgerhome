package net.rutger.home.controller;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import net.rutger.home.domain.WeatherDataType;
import net.rutger.home.service.WateringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @Autowired
    private WateringService wateringService;

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/test")
    public Map<WeatherDataType, Double> test() {
        return wateringService.getLatestWeatherData();
    }

    @RequestMapping(value = "/checkWaterTask")
    @ResponseStatus(value = HttpStatus.OK)
    public void checkWaterTask() {
        wateringService.checkWateringTask(true);
    }
}
