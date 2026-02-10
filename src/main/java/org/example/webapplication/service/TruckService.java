package org.example.webapplication.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.request.truck.TruckRequest;
import org.example.webapplication.dto.response.PageResponse;
import org.example.webapplication.dto.response.truck.TruckResponse;
import org.example.webapplication.dto.response.user.UserResponse;
import org.example.webapplication.entity.Truck;
import org.example.webapplication.entity.User;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.exception.AppException;
import org.example.webapplication.exception.ErrorCode;
import org.example.webapplication.repository.travel.TravelRepository;
import org.example.webapplication.repository.truck.TruckRepository;
import org.example.webapplication.repository.user.UserRepository;
import org.example.webapplication.service.cache.TruckCacheService;
import org.example.webapplication.service.mapper.TruckMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TruckService {
    private final TravelRepository  travelRepository;
    private final TruckRepository truckRepository;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final TruckCacheService  truckCacheService;
    private final TruckMapper  truckMapper;



    @CacheEvict(value = "trucks_list", allEntries = true)
    @Transactional
    public TruckResponse createdTruck(TruckRequest dto) {
        permissionService.getUser(
                List.of(PermissionKey.CREATE),
                PermissionType.TRUCK
        );
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

        return truckMapper.toResponse(saved);
    }

    @CacheEvict(value = "trucks_list", allEntries = true)
    @Transactional
    public TruckResponse updatedTruck(TruckRequest dto, String truckId) {
        permissionService.getUser(
                List.of(PermissionKey.UPDATE),
                PermissionType.TRUCK
        );
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

        return truckMapper.toResponse(saved);
    }

    public PageResponse<TruckResponse> getAllTrucks(int page, int size) {
        permissionService.getUser(
                List.of(PermissionKey.MANAGE, PermissionKey.VIEW),
                PermissionType.TRUCK
        );
        return truckCacheService.getAllTrucks(page, size);
    }

    @CacheEvict(value = "trucks_list", allEntries = true)
    @Transactional
    public void deleteTruck(String truckId) {
        permissionService.getUser(
                List.of(PermissionKey.MANAGE),
                PermissionType.TRUCK
        );
        Truck truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new AppException(ErrorCode.TRUCK_NOT_FOUND));

        truckRepository.delete(truck);
    }
}
