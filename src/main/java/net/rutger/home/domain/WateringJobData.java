package net.rutger.home.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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
    public static final Locale DUTCH_LOCALE = new Locale("nl", "NL");
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###.##");

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
    private int numberOfMinutesUpper;
    @Column(length=2)
    private int minutesLeftUpper;
    @Column(length=2)
    private int numberOfMinutesLower;
    @Column(length=2)
    private int minutesLeftLower;

    @Enumerated(EnumType.STRING)
    private WateringJobType type;

    private LocalDateTime nextRun;

    @ManyToOne
    private StaticWateringData upperStaticWateringData;

    @ManyToOne
    private StaticWateringData lowerStaticWateringData;

    @ManyToOne
    private WateringJobEnforceData enforceData;

    public WateringJobData(final Optional<Map<WeatherDataType, Double>> weatherData, final int numberOfMinutesUpper,
                           final int numberOfMinutesLower, final WateringJobType type, final StaticWateringData upperStaticWateringData,
                           final StaticWateringData lowerStaticWateringData, final WateringJobEnforceData enforceData) {
        this.numberOfMinutesUpper = numberOfMinutesUpper;
        this.numberOfMinutesLower = numberOfMinutesLower;
        this.minutesLeftUpper = numberOfMinutesUpper;
        this.minutesLeftLower = numberOfMinutesLower;
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
        this.upperStaticWateringData = upperStaticWateringData;
        this.lowerStaticWateringData = lowerStaticWateringData;
        this.enforceData = enforceData;
    }

    public WateringJobData(final int manualNumberOfMinutesUpper, final int manualNumberOfMinutesLower,
            final StaticWateringData upperStaticWateringData, final StaticWateringData lowerStaticWateringData) {
        this.upperStaticWateringData = upperStaticWateringData;
        this.lowerStaticWateringData = lowerStaticWateringData;
        this.numberOfMinutesUpper = manualNumberOfMinutesUpper;
        this.numberOfMinutesLower = manualNumberOfMinutesLower;
        this.minutesLeftUpper = manualNumberOfMinutesUpper;
        this.minutesLeftLower = manualNumberOfMinutesLower;
        this.localDate = LocalDate.now();
        this.nextRun = LocalDateTime.now();
        this.type = WateringJobType.MANUAL;
    }

    public String getDay() {
        if (localDate == null) {
            return "";
        } else if(localDate.isEqual(LocalDate.now())) {
            return "Vandaag";
        } else if (localDate.isEqual(LocalDate.now().minusDays(1))) {
            return "Gisteren";
        } else {
            return localDate.format(DateTimeFormatter.ofPattern("EEEE, dd MM YYYY", DUTCH_LOCALE));
        }
    }

    public String getMakkinkIndexString() {
        return makkinkIndex == null ? "N/A" : DECIMAL_FORMAT.format(makkinkIndex);
    }

    public String getPrecipitationString() {
        return precipitation == null ? "N/A" : DECIMAL_FORMAT.format(precipitation);
    }

    public String getPrecipitationDurationString() {
        return precipitationDuration == null ? "N/A" : DECIMAL_FORMAT.format(precipitationDuration);
    }

    public String getMeanTemperatureString() {
        return meanTemperature == null ? "N/A" : DECIMAL_FORMAT.format(meanTemperature);
    }

    public String getMaxTemperatureString() {
        return maxTemperature == null ? "N/A" : DECIMAL_FORMAT.format(maxTemperature);
    }

    public String getUsedFactorString() {
        if (enforceData != null) {
            return enforceData.getMultiplyFactorString();
        } else if (lowerStaticWateringData != null) {
            return lowerStaticWateringData.getFactorString();
        } else {
            return "";
        }

    }
}
