package org.example.webapplication.repository;

import org.example.webapplication.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule,String> {

    List<Schedule> findByDrivers_Username(String username);
    boolean existsByStartPlaceIgnoreCaseAndEndPlaceIgnoreCase(String startPlace, String endPlace);


    boolean existsByStartPlaceIgnoreCaseAndEndPlaceIgnoreCaseAndIdNot(
            String startPlace, String endPlace, String id);

}
