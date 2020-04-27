package net.rutger.home.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data that can be added manually to enforce a different result of a WateringJob.
 * i.e. If on a certain day you gave the plants extra water in the evening so that the next
 * morning (normal job) it doesn't need to water so many (or the opposite: it is very dry,
 * we need to water longer), it is possible to tell it to the system upfront.
 *
 * If this data is set, the WateringJob will see this and will change the result. The enforcement is either
 * a multiply factor of a fixed number of minutes
 */
@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@EntityListeners(AuditingEntityListener.class)
public class WateringJobEnforceData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate localDate;

    @Column(precision=3, scale=1)
    private Double multiplyFactor;

    private Integer numberOfMinutes;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    public WateringJobEnforceData(final LocalDate localDate, final double multiplyFactor) {
        this.localDate = localDate;
        this.multiplyFactor = multiplyFactor;
        this.numberOfMinutes = null;
    }

    public WateringJobEnforceData(final LocalDate localDate, final int numberOfMinutes) {
        this.localDate = localDate;
        this.multiplyFactor = null;
        this.numberOfMinutes = numberOfMinutes;
    }
}
