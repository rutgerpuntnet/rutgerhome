package net.rutger.home.controller.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class EnforceData {
    private Double factor;
    private Integer minutes;
    private Integer days;
}
