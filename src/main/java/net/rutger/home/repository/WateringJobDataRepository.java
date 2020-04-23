package net.rutger.home.repository;

import net.rutger.home.domain.WateringJobData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WateringJobDataRepository extends JpaRepository<WateringJobData, Long> {
}
