package org.example.webapplication.Repository;

import org.example.webapplication.Entity.Schedule;
import org.example.webapplication.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule,String> {

    List<Schedule> findByDrivers_Username(String username);
    boolean existsByStartPlaceIgnoreCaseAndEndPlaceIgnoreCase(String startPlace, String endPlace);


    boolean existsByStartPlaceIgnoreCaseAndEndPlaceIgnoreCaseAndIdNot(
            String startPlace, String endPlace, String id);

}
