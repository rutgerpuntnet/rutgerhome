package net.rutger.home.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * Data file containing information about a (daily) watering job
 */
@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EntityListeners(AuditingEntityListener.class)
public class WateringJobData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate localDate;
    @Column(precision=3, scale=1)
    private Double makkinkIndex;
    @Column(precision=4, scale=1)
    private Double precipitation;
    @Column(precision=4, scale=1)
    private Double precipitationDuration;
    @Column(precision=3, scale=1)
    private Double meanTemperature;
    @Column(precision=3, scale=1)
    private Double maxTemperature;
    @Column(length=2)
    private int numberOfMinutes;
    @Column(length=2)
    private int minutesLeft;

    @Enumerated(EnumType.STRING)
    private WateringJobType type;

    private LocalDateTime nextRun;

    public WateringJobData(final Optional<Map<WeatherDataType, Double>> weatherData, final int numberOfMinutes) {
        this.numberOfMinutes = numberOfMinutes;
        this.minutesLeft = numberOfMinutes;
        this.localDate = LocalDate.now();
        if (weatherData.isPresent()) {
            this.makkinkIndex = weatherData.get().get(WeatherDataType.EV24);
            this.precipitation = weatherData.get().get(WeatherDataType.RH);
            this.precipitationDuration = weatherData.get().get(WeatherDataType.DR);
            this.meanTemperature = weatherData.get().get(WeatherDataType.TG);
            this.maxTemperature = weatherData.get().get(WeatherDataType.TX);
        }
        this.nextRun = LocalDateTime.now();
        this.type = WateringJobType.AUTO;
    }
}
