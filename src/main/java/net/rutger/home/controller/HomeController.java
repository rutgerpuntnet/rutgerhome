package net.rutger.home.controller;

import java.util.concurrent.atomic.AtomicLong;

import net.rutger.home.domain.WateringJobData;
import net.rutger.home.service.WateringJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @Autowired
    private WateringJobService wateringJobService;

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/latest")
    public WateringJobData latest() {
        return wateringJobService.getLatestWateringJobData();
    }

    @RequestMapping(value = "/checkWaterTask")
    @ResponseStatus(value = HttpStatus.OK)
    public void checkWaterTask() {
        wateringJobService.checkWateringJob(true);
    }
}
