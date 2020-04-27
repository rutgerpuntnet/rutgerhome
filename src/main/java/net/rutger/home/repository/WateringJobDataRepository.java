package net.rutger.home.repository;

import net.rutger.home.domain.WateringJobData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface WateringJobDataRepository extends JpaRepository<WateringJobData, Long> {

    @Query("select w from WateringJobData w where w.localDate = ?1 and w.minutesLeft > 0 and w.nextRun < ?2")
    WateringJobData findFirstActiveWateringJob(final LocalDate localDate, final LocalDateTime nextRun);

    WateringJobData findFirstByOrderByNextRunDesc();

    WateringJobData findFirstByLocalDate(final LocalDate localDate);

}
