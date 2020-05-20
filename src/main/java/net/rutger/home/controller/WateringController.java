package net.rutger.home.controller;

import net.rutger.home.controller.model.EnforceData;
import net.rutger.home.controller.model.HistoryGraphData;
import net.rutger.home.controller.model.WateringStatus;
import net.rutger.home.domain.*;
import net.rutger.home.repository.StaticWateringDataRepository;
import net.rutger.home.repository.WateringActionRepository;
import net.rutger.home.repository.WateringJobDataRepository;
import net.rutger.home.repository.WateringJobEnforceDataRepository;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("watering")
public class WateringController {
    private final static Logger LOG = LoggerFactory.getLogger(WateringController.class);

    @Autowired
    private WateringJobService wateringJobService;

    @Autowired
    private WateringJobDataRepository wateringJobDataRepository;

    @Autowired
    private WateringActionRepository wateringActionRepository;

    @Autowired
    private GardenArduinoService gardenArduinoService;

    @Autowired
    private WateringJobEnforceDataRepository wateringJobEnforceDataRepository;

    @Autowired
    private StaticWateringDataRepository staticWateringDataRepository;

    @RequestMapping("/status")
    public WateringStatus getStatus() {
        final WateringStatus result = new WateringStatus();
        final WateringJobData jobData = wateringJobDataRepository.findFirstByOrderByNextRunDesc();
        result.setLatestJob(jobData);
        result.setActive(jobData != null && jobData.getNextRun().isAfter(LocalDateTime.now()) && jobData.getMinutesLeft() > 0);
        result.setNextRunDay(jobData != null && LocalDate.now().equals(jobData.getLocalDate()) ? "Morgen" : "Vandaag");
        if (jobData != null && LocalDate.now().equals(jobData.getLocalDate())) {
            result.setNextRunDay("Morgen");
            final WateringJobEnforceData enforceData = wateringJobEnforceDataRepository.findFirstByLocalDate(LocalDate.now().plusDays(1));
            result.setNextEnforceFactor(enforceData == null ? null : enforceData.getMultiplyFactor());
        } else {
            result.setNextRunDay("Vandaag");
            final WateringJobEnforceData enforceData = wateringJobEnforceDataRepository.findFirstByLocalDate(LocalDate.now());
            result.setNextEnforceFactor(enforceData == null ? null : enforceData.getMultiplyFactor());
        }
        LOG.debug("status: {} ", result);
        return result;
    }

    @RequestMapping("/job/latest")
    public WateringJobData latestJob() {
        final WateringJobData result = wateringJobDataRepository.findFirstByOrderByNextRunDesc();
        LOG.debug("latest job data: {} ", result);
        return result;
    }

    @RequestMapping("/staticdata")
    public StaticWateringData getStaticWateringData() {
        return staticWateringDataRepository.findFirstByOrderByIdDesc();
    }

    @RequestMapping("/history/{days}")
    public HistoryGraphData getHistoryGraphData(@PathVariable final Integer days) {
        final Pageable pageable = PageRequest.of(0, days, Sort.by(Sort.Direction.DESC, "localDate"));
        final Page<WateringJobData> pageResult = wateringJobDataRepository.findAll(pageable);
        List<WateringJobData> wateringJobData = pageResult.get().collect(Collectors.toList());

        final HistoryGraphData historyGraphData = new HistoryGraphData();
        for (WateringJobData jobData : wateringJobData) {
            final String label = jobData.getLocalDate().format(DateTimeFormatter.ofPattern("EEEE dd-MM", WateringJobData.DUTCH_LOCALE));

            historyGraphData.getLabels().add(0, label);
            historyGraphData.getDuration().add(0, jobData.getNumberOfMinutes());
            historyGraphData.getMakkink().add(0, jobData.getMakkinkIndex());
            historyGraphData.getPrecipitation().add(0, jobData.getPrecipitation());
        }
        return historyGraphData;
    }

    @RequestMapping("/action/latest/{count}")
    public List<WateringAction> latestActions(@PathVariable final Integer count) {
        final Pageable pageable = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "createdDateTime"));
        final Page<WateringAction> result = wateringActionRepository.findAll(pageable);
        return result.get().collect(Collectors.toList());
    }

    @PostMapping("/manualAction")
    public GardenArduino waterAction(@RequestParam final Integer count) {
        LOG.debug("Manual action to water for {} minutes", count);
        WateringJobData data = new WateringJobData();

        final GardenArduino result = gardenArduinoService.call(count);
        wateringActionRepository.save(new WateringAction(count, true));
        return result;
    }

    @PostMapping("/enforceMinutes")
    public ResponseEntity enforceWateringJobMinutes(@RequestBody final EnforceData body) {
        return enforce(body);
    }

    @PostMapping("/enforceFactor")
    public ResponseEntity enforceWateringJobFactor(@RequestBody final EnforceData body) {
        return enforce(body);
    }

    private ResponseEntity enforce(final EnforceData body) {
        LOG.debug("Set enforce watering data. Body: {}", body);
        LocalDate localDate = LocalDate.now();
        final WateringJobData wateringJobData = wateringJobDataRepository.findFirstByLocalDateOrderByNextRunDesc(localDate);

        if (wateringJobData != null) { // there is already a job for today, enforcement is for next job (tomorrow)
            LOG.debug("enforce for tomorrow");
            localDate = localDate.plusDays(1);
        } else {
            LOG.debug("enforce for today");
        }

        WateringJobEnforceData enforceData = wateringJobEnforceDataRepository.findFirstByLocalDate(localDate);
        if (enforceData == null) {
            enforceData = new WateringJobEnforceData();
            enforceData.setLocalDate(localDate);
        } else {
            LOG.debug("Enforce data already exists. Data will be overwritten");
        }
        enforceData.setMultiplyFactor(body.getFactor());
        enforceData.setNumberOfMinutes(body.getMinutes());

        LOG.debug("Saving enforceData: {}", enforceData);
        wateringJobEnforceDataRepository.save(enforceData);
        return ResponseEntity.ok().build();

    }

    @RequestMapping(value = "/checkjob")
    @ResponseStatus(value = HttpStatus.OK)
    public void checkWaterJob() {
        LOG.info("checkWaterJob");
        wateringJobService.checkWateringJob(true);
    }

    @PostMapping(value = "/killCurrentJob")
    @ResponseStatus(value = HttpStatus.OK)
    public void killCurrentJob() {
        LOG.info("killCurrentJob");
        gardenArduinoService.call(0);
        final WateringJobData data = wateringJobDataRepository.findFirstByLocalDateOrderByNextRunDesc(LocalDate.now());
        data.setMinutesLeft(0);
        data.setType(WateringJobType.MANUAL);
        wateringJobDataRepository.save(data);
    }
}
