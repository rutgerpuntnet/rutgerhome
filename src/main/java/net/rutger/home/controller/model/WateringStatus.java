package net.rutger.home.controller.model;

import lombok.Data;
import net.rutger.home.domain.WateringJobData;

@Data
public class WateringStatus {
    private WateringJobData latestJob;
    private boolean active = false;
    private String nextRunDay;
    private Double nextEnforceFactor;
}
