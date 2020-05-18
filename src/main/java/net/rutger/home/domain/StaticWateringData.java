package net.rutger.home.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;

@Entity
@Data
@ToString
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class StaticWateringData {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###.##");

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

    @LastModifiedDate
    private LocalDateTime lastModified;

    public String getFactorString() {
        return DECIMAL_FORMAT.format(factor);
    }

}
