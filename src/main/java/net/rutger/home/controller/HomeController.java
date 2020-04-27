package net.rutger.home.controller;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import net.rutger.home.domain.GardenArduino;
import net.rutger.home.domain.WateringAction;
import net.rutger.home.domain.WateringJobData;
import net.rutger.home.repository.WateringActionRepository;
import net.rutger.home.repository.WateringJobDataRepository;
import net.rutger.home.service.GardenArduinoService;
import net.rutger.home.service.WateringJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController("watering")
public class HomeController {
    private final static Logger LOG = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private WateringJobService wateringJobService;

    @Autowired
    private WateringJobDataRepository wateringJobDataRepository;

    @Autowired
    private WateringActionRepository wateringActionRepository;

    @Autowired
    private GardenArduinoService gardenArduinoService;

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/job/latest")
    public WateringJobData latestJob() {
        final WateringJobData result = wateringJobDataRepository.findFirstByOrderByUpdatedOnDesc();
        LOG.debug("latest job data: {} ", result);
        return result;
    }

    @RequestMapping("/job/latest/{amount}")
    public List<WateringJobData> latestJobs(@PathVariable final Integer count) {
        final Pageable pageable = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "localDate"));
        final Page<WateringJobData> result = wateringJobDataRepository.findAll(pageable);
        return result.get().collect(Collectors.toList());
    }

    @RequestMapping("/action/latest/{amount}")
    public List<WateringAction> latestActions(@PathVariable final Integer count) {
        final Pageable pageable = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "createdDateTime"));
        final Page<WateringAction> result = wateringActionRepository.findAll(pageable);
        return result.get().collect(Collectors.toList());
    }

    @PostMapping("/action")
    public GardenArduino waterAction(@RequestParam final Integer count) {
        LOG.debug("Manual action to water for {} minutes", count);
        final GardenArduino result = gardenArduinoService.call(count);
        wateringActionRepository.save(new WateringAction(count, true));
        return result;
    }

    @RequestMapping(value = "/waterforminutes")
    @ResponseStatus(value = HttpStatus.OK)
    public void checkWaterJob() {
        LOG.info("checkWaterJob");
        wateringJobService.checkWateringJob(true);
    }
}
