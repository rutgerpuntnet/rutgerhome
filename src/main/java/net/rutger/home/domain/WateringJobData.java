package net.rutger.home.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
    public static final Locale DUTCH_LOCALE = Locale.of("nl", "NL");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, dd MM YYYY", DUTCH_LOCALE);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate localDate;

    private Double makkinkIndex;
    private Double precipitation;
    private Double precipitationDuration;
    private Double meanTemperature;
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
        } else if (localDate.isEqual(LocalDate.now())) {
            return "Vandaag";
        } else if (localDate.isEqual(LocalDate.now().minusDays(1))) {
            return "Gisteren";
        } else {
            return localDate.format(DATE_FORMATTER);
        }
    }

    public String getMakkinkIndexString() {
        return formatDouble(makkinkIndex);
    }

    public String getPrecipitationString() {
        return formatDouble(precipitation);
    }

    public String getPrecipitationDurationString() {
        return formatDouble(precipitationDuration);
    }

    public String getMeanTemperatureString() {
        return formatDouble(meanTemperature);
    }

    public String getMaxTemperatureString() {
        return formatDouble(maxTemperature);
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

    private String formatDouble(Double value) {
        return value == null ? "N/A" : String.format(DUTCH_LOCALE, "%.2f", value).replaceAll(",00$", "");
    }
}