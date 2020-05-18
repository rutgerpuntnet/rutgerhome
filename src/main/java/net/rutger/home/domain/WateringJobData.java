package net.rutger.home.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Data file containing information about a (daily) watering job
 */
@Entity
@NoArgsConstructor
@ToString
@Data
public class WateringJobData {
    private static final Locale DUTCH_LOCALE = new Locale("nl", "NL");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###.##");

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

    @ManyToOne
    private StaticWateringData staticWateringData;

    public WateringJobData(final Optional<Map<WeatherDataType, Double>> weatherData, final int numberOfMinutes,
                           final WateringJobType type, final StaticWateringData staticWateringData) {
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
        this.type = type;
        this.staticWateringData = staticWateringData;
    }

    public String getDay() {
        if(localDate.isEqual(LocalDate.now())) {
            return "Vandaag";
        } else if (localDate.isEqual(LocalDate.now().minusDays(1))) {
            return "Gisteren";
        } else {
            return localDate.format(DateTimeFormatter.ofPattern("EEEE, dd MM YYYY", DUTCH_LOCALE));
        }
    }

    public String getMakkinkIndexString() {
        return DECIMAL_FORMAT.format(makkinkIndex);
    }

    public String getPrecipitationString() {
        return DECIMAL_FORMAT.format(precipitation);
    }

    public String getPrecipitationDurationString() {
        return DECIMAL_FORMAT.format(precipitationDuration);
    }

    public String getMeanTemperatureString() {
        return DECIMAL_FORMAT.format(meanTemperature);
    }

    public String getMaxTemperatureString() {
        return DECIMAL_FORMAT.format(maxTemperature);
    }

}
