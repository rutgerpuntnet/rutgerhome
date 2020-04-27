package net.rutger.home.repository;

import net.rutger.home.domain.WateringAction;
import net.rutger.home.domain.WateringJobData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface WateringActionRepository extends JpaRepository<WateringAction, Long> {

}
