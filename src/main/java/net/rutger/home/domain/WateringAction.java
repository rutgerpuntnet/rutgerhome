package net.rutger.home.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@ToString
@EntityListeners(AuditingEntityListener.class)
public class WateringAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    private LocalDateTime createdDateTime;

    private int minutes;

    private boolean manual = false;

    public WateringAction(final int minutes) {
        this.minutes = minutes;
    }

    public WateringAction(final int minutes, final boolean manual) {
        this.minutes = minutes;
        this.manual = manual;
    }
}
