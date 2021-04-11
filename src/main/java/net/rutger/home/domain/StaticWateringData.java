package net.rutger.home.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Static data, or settings, used to determine the effective/resulting wateringData used in job
 * These settings are maintained in the frontend
 */
@Entity
@Data
@ToString
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class StaticWateringData {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###.##");
    private static final DateTimeFormatter LAST_MODIFIED_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM YYYY", WateringJobData.DUTCH_LOCALE);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(precision=3, scale=1)
    private Double factor = 1.0;
    private int minutesPerMm = 3;
    private int defaultMinutes = 10;
    private int dailyLimitMinutes = 18;
    private int maxDurationMinutes = 2;
    private int initialMm = 1;
    private int intervalMinutes = 5;
    private ValveType valveType;

    @LastModifiedDate
    private LocalDateTime lastModified;

    public StaticWateringData(final ValveType type, final StaticWateringData origin, final Double factor,
                              final Integer minutesPerMm, final Integer defaultMinutes,
                              final Integer dailyLimitMinutes, final Integer maxDurationMinutes,
                              final Integer initialMm, final Integer intervalMinutes) {
        this.valveType = type;
        this.factor = factor == null ? origin.getFactor() : factor;
        this.minutesPerMm = minutesPerMm == null ? origin.getMinutesPerMm() : minutesPerMm;
        this.defaultMinutes = defaultMinutes == null ? origin.getDefaultMinutes() : defaultMinutes;
        this.dailyLimitMinutes = dailyLimitMinutes == null ? origin.getDailyLimitMinutes() : dailyLimitMinutes;
        this.maxDurationMinutes = maxDurationMinutes == null ? origin.getMaxDurationMinutes() : maxDurationMinutes;
        this.initialMm = initialMm == null ? origin.getInitialMm() : initialMm;
        this.intervalMinutes = intervalMinutes == null ? origin.getIntervalMinutes() : intervalMinutes;
    }
    public String getFactorString() {
        return factor == null ? "" : DECIMAL_FORMAT.format(factor);
    }

    public String getModifiedSince(){
        return lastModified == null ? "" : LAST_MODIFIED_FORMATTER.format(lastModified);
    }
}
