package net.rutger.home.repository;

import net.rutger.home.domain.StaticWateringData;
import net.rutger.home.domain.ValveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaticWateringDataRepository extends JpaRepository<StaticWateringData, Long> {

    StaticWateringData findFirstByValveTypeOrderByIdDesc(final ValveType valveType);

}
