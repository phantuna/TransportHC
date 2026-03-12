package org.example.webapplication.repository.truck;

import org.example.webapplication.entity.Truck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TruckRepository extends JpaRepository<Truck,String>, TruckRepositoryCustom {
    boolean existsByLicensePlate(String licensePlate);
    boolean existsByDriver_Id(String driverId);
    Optional<Truck> findByDriver_Id(String driverId);

}
