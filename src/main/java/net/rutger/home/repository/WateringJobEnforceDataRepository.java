package net.rutger.home.repository;

import net.rutger.home.domain.WateringJobEnforceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface WateringJobEnforceDataRepository extends JpaRepository<WateringJobEnforceData, Long> {

    public WateringJobEnforceData findFirstByLocalDate(final LocalDate now);
}
