package net.rutger.home.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data that can be added manually to enforce a different result of a WateringJob.
 * i.e. If on a certain day you gave the plants extra water in the evening so that the next
 * morning (normal job) it doesn't need to water so many (or the opposite: it is very dry,
 * we need to water longer), it is possible to tell it to the system upfront.
 *
 * If this data is set, the WateringJob will see this and will change the result.
 * The enforcement contains of a multiply factor (either below or above the default 1.0)
 */
@Entity
@Data
@NoArgsConstructor
@ToString
@EntityListeners(AuditingEntityListener.class)
public class WateringJobEnforceData {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###.##");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate localDate;

    @Column(precision=3, scale=1)
    private double multiplyFactor = 1.0;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    public String getMultiplyFactorString() {
        return DECIMAL_FORMAT.format(multiplyFactor);
    }

}
