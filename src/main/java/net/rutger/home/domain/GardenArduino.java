package net.rutger.home.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class GardenArduino {
    private float latestTemp;
    private long millis;
    private long relayTimer;
    private int secondsleft;
    private long lastTempMeasureMillisAgo;
    private String relayStatus;
}
