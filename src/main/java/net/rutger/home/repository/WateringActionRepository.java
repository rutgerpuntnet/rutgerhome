package net.rutger.home.repository;

import net.rutger.home.domain.WateringAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WateringActionRepository extends JpaRepository<WateringAction, Long> {

}
