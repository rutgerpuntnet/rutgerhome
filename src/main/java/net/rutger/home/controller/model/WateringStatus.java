package net.rutger.home.controller.model;

import lombok.Data;
import net.rutger.home.domain.WateringJobData;

import java.time.LocalDateTime;

@Data
public class WateringStatus {
    private WateringJobData latestJob;
    private boolean active = false;
    private String nextRunDay;
    private Double nextEnforceFactor;

    public boolean getAutomaticJobUpcoming(){
        return LocalDateTime.now().getHour() == 8;
    }
}
