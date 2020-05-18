package net.rutger.home.service;

import net.rutger.home.domain.StaticWateringData;
import net.rutger.home.repository.StaticWateringDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StaticWateringDataService {

    @Autowired
    private StaticWateringDataRepository staticWateringDataRepository;

    /**
     * Get the the latest StaticWateringData. Create one with default values first if no result in the database;
     * @return
     */
    public StaticWateringData getLatest() {
        StaticWateringData result = staticWateringDataRepository.findFirstByOrderByIdDesc();
        if (result == null) {
            result = new StaticWateringData();
            result = staticWateringDataRepository.save(result);
        }
        return result;
    }
}
