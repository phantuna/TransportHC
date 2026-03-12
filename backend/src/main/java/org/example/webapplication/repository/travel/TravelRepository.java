package org.example.webapplication.repository.travel;

import org.example.webapplication.entity.Travel;
import org.example.webapplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TravelRepository extends JpaRepository<Travel,String>, TravelRepositoryCustom {

    List<Travel> findByTruck_Id(String truckId);


}
