package org.example.webapplication.repository.truck;

import org.example.webapplication.entity.Truck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TruckRepository extends JpaRepository<Truck,String>, TruckRepositoryCustom {
    boolean existsByLicensePlate(String licensePlate);
    boolean existsByDriver_Id(String driverId);

}
