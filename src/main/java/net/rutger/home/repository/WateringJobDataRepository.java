package net.rutger.home.repository;

import net.rutger.home.domain.WateringJobData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface WateringJobDataRepository extends JpaRepository<WateringJobData, Long> {

    @Query("select w from WateringJobData w where w.localDate = ?1 and (w.minutesLeftUpper > 0 or w.minutesLeftLower > 0) and w.nextRun < ?2")
    WateringJobData findFirstActiveWateringJob(final LocalDate localDate, final LocalDateTime nextRun);

    WateringJobData findFirstByOrderByNextRunDesc();

    @Query("select w from WateringJobData w where w.localDate = ?1 and w.type <> 'MANUAL' order by w.nextRun desc")
    WateringJobData findLatestTodaysNonManualJob(final LocalDate localDate);

}
