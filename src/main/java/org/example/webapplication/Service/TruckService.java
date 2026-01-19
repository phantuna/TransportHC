package org.example.webapplication.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.Dto.request.Truck.TruckRequest;
import org.example.webapplication.Dto.response.Truck.TruckResponse;
import org.example.webapplication.Entity.Truck;
import org.example.webapplication.Entity.User;
import org.example.webapplication.Exception.AppException;
import org.example.webapplication.Exception.ErrorCode;
import org.example.webapplication.Repository.TravelRepository;
import org.example.webapplication.Repository.TruckRepository;
import org.example.webapplication.Repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TruckService {
    private final TravelRepository  travelRepository;
    private final TruckRepository truckRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasAuthority('MANAGE_TRUCK')")
    public TruckResponse createdTruck(TruckRequest dto) {

        if (dto.getLicensePlate() == null || dto.getLicensePlate().isBlank()) {
            throw new AppException(ErrorCode.INVALID_LICENSE_PLATE);
        }

        if (truckRepository.existsByLicensePlate(dto.getLicensePlate())) {
            throw new AppException(ErrorCode.LICENCE_PLATE_EXISTED);
        }

        User driver = null;
        if (dto.getDriverId() != null) {

            if (truckRepository.existsByDriver_Id(dto.getDriverId())) {
                throw new AppException(ErrorCode.DRIVER_ALREADY_ASSIGNED);
            }

            driver = userRepository.findById(dto.getDriverId())
                    .orElseThrow(() -> new AppException(ErrorCode.DRIVER_NOT_FOUND));
        }

        Truck truck = new Truck();
        truck.setTypeTruck(dto.getTypeTruck());
        truck.setLicensePlate(dto.getLicensePlate());
        truck.setDriver(driver);
        truck.setGanMooc(dto.isGanMooc());
        truck.setStatus(dto.getStatus());

        Truck saved = truckRepository.save(truck);

        return TruckResponse.builder()
                .id(saved.getId())
                .typeTruck(saved.getTypeTruck())
                .licensePlate(saved.getLicensePlate())
                .ganMooc(saved.isGanMooc())
                .status(saved.getStatus())
                .driverId(driver != null ? driver.getId() : null)
                .driverName(driver != null ? driver.getUsername() : null)
                .build();
    }



    // manager - supervisor
    @PreAuthorize("hasAuthority('MANAGE_TRUCK')")
    public TruckResponse updatedTruck(TruckRequest dto, String truckId) {
        boolean hasTravelToday = travelRepository.existsActiveTravelToday(truckId);

        if (hasTravelToday) {
            throw new AppException(ErrorCode.TRUCK_HAS_TRAVEL_TODAY);
        }

        Truck truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new AppException(ErrorCode.TRUCK_NOT_FOUND));

        if (!truck.getLicensePlate().equals(dto.getLicensePlate())
                && truckRepository.existsByLicensePlate(dto.getLicensePlate())) {
            throw new AppException(ErrorCode.LICENCE_PLATE_EXISTED);
        }

        if (dto.getDriverId() != null) {

            User driver = userRepository.findById(dto.getDriverId())
                    .orElseThrow(() -> new AppException(ErrorCode.DRIVER_NOT_FOUND));
            truck.setDriver(driver);

        } else {
            truck.setDriver(null);
        }

        truck.setTypeTruck(dto.getTypeTruck());
        truck.setLicensePlate(dto.getLicensePlate());
        truck.setGanMooc(dto.isGanMooc());
        truck.setStatus(dto.getStatus());

        Truck saved = truckRepository.save(truck);
        User driver = saved.getDriver();

        return TruckResponse.builder()
                .id(saved.getId())
                .typeTruck(saved.getTypeTruck())
                .licensePlate(saved.getLicensePlate())
                .ganMooc(saved.isGanMooc())
                .status(saved.getStatus())
                .driverId(driver != null ? driver.getId() : null)
                .driverName(driver != null ? driver.getUsername() : null)
                .build();
    }



    @PreAuthorize("hasAuthority('MANAGE_TRUCK') or hasAuthority('VIEW_TRUCK')")
    public List<TruckResponse> getAllTrucks(){
        List<Truck> trucks = truckRepository.findAll();
        List<TruckResponse> truckResponses = new ArrayList<>();

        for (Truck truck : trucks) {
            User driver = truck.getDriver();

            TruckResponse response = TruckResponse.builder()
                    .id(truck.getId())
                    .typeTruck(truck.getTypeTruck())
                    .licensePlate(truck.getLicensePlate())
                    .ganMooc(truck.isGanMooc())
                    .status(truck.getStatus())
                    .driverId(driver != null ? driver.getId() : null)
                    .driverName(driver != null ? driver.getUsername() : null)
                    .build();

            truckResponses.add(response);
        }
        return truckResponses;
    }


    @PreAuthorize("hasAuthority('MANAGE_TRUCK')")
    @Transactional
    public void deleteTruck(String truckId) {
        Truck truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new AppException(ErrorCode.TRUCK_NOT_FOUND));

        truckRepository.delete(truck);
    }
}
