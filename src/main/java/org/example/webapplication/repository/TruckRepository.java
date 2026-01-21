package org.example.webapplication.repository;

import org.example.webapplication.entity.Truck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TruckRepository extends JpaRepository<Truck,String> {
    Optional<Truck>  findByDriver (String driver);
    boolean existsByLicensePlate(String licensePlate);
    boolean existsByDriver_Id(String driverId);

}
