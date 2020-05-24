package net.rutger.home.controller.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class StaticData {
    private Double factor;
    private Integer minutesPerMm;
    private Integer defaultMinutes;
    private Integer dailyLimitMinutes;
    private Integer maxDurationMinutes;
    private Integer initialMm;
    private Integer intervalMinutes;

}
