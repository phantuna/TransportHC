package org.example.webapplication.Repository;

import org.example.webapplication.Entity.Truck;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface TruckRepository extends JpaRepository<Truck,String> {
    Optional<Truck>  findByDriver (String driver);
    boolean existsByLicensePlate(String licensePlate);
    boolean existsByDriver_Id(String driverId);

}
