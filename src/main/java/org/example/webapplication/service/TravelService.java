package org.example.webapplication.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.travel.TravelScheduleReportResponse;
import org.example.webapplication.enums.ApprovalStatus;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.enums.TruckStatus;
import org.example.webapplication.dto.request.travel.TravelRequest;
import org.example.webapplication.dto.response.expense.ExpenseResponse;
import org.example.webapplication.dto.response.travel.TravelResponse;
import org.example.webapplication.entity.Expense;
import org.example.webapplication.entity.Schedule;
import org.example.webapplication.entity.Travel;
import org.example.webapplication.entity.Truck;
import org.example.webapplication.exception.AppException;
import org.example.webapplication.exception.ErrorCode;
import org.example.webapplication.repository.schedule.ScheduleRepository;
import org.example.webapplication.repository.travel.TravelRepository;
import org.example.webapplication.repository.truck.TruckRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelService {
    private final TravelRepository travelRepository;
    private final TruckRepository truckRepository;
    private final ScheduleRepository scheduleRepository;
    private final PermissionService  permissionService;

    public TravelResponse toResponse(Travel travel) {

        Truck truck = travel.getTruck();
        Schedule schedule = travel.getSchedule();

        double totalExpense = 0;
        List<ExpenseResponse> expenseResponses = new ArrayList<>();

        if (travel.getExpenses() != null) {
            for (Expense expense : travel.getExpenses()) {

                if (expense.getApproval() == ApprovalStatus.APPROVED) {
                    totalExpense += expense.getExpense();
                }

                expenseResponses.add(
                        ExpenseResponse.builder()
                                .id(expense.getId())
                                .type(expense.getType())
                                .expense(expense.getExpense())
                                .description(expense.getDescription())
                                .approval(expense.getApproval())
                                .travelId(travel.getId())
                                .incurredDate(expense.getIncurredDate())
                                .driverName(
                                        travel.getUser() != null
                                                ? travel.getUser().getUsername()
                                                : null
                                )
                                .modifiedBy(expense.getModifiedBy())
                                .createdDate(expense.getCreatedDate())
                                .build()
                );
            }
        }

        return TravelResponse.builder()
                .travelId(travel.getId())
                .truckPlate(truck != null ? truck.getLicensePlate() : null)
                .driverName(
                        truck != null && truck.getDriver() != null
                                ? truck.getDriver().getUsername()
                                : null
                )
                .scheduleName(
                        schedule != null
                                ? schedule.getStartPlace() + " - " + schedule.getEndPlace()
                                : null
                )
                .startDate(travel.getStartDate())
                .endDate(travel.getEndDate())
                .expenses(expenseResponses)
                .totalExpense(totalExpense)
                .build();
    }

    @Transactional
    public TravelResponse createdTravel(TravelRequest dto) {
        permissionService.getUser(
                List.of(PermissionKey.MANAGE),
                PermissionType.TRAVEL
        );
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new AppException(ErrorCode.START_DATE_AND_END_DATE_NOT_NULL);
        }
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new AppException(ErrorCode.END_DATE_GREATER_OR_EQUAL_START_DATE);
        }

        Truck truck = truckRepository.findById(dto.getTruckId())
                .orElseThrow(() -> new AppException(ErrorCode.TRUCK_NOT_FOUND));
        Schedule schedule = scheduleRepository.findById(dto.getScheduleId())
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        boolean existedSameDay = travelRepository
                .existsByTruck_IdAndStartDate(
                        truck.getId(),
                        dto.getStartDate());

        if (existedSameDay) {
            throw new AppException(ErrorCode.TRUCK_ALREADY_IN_TRAVEL_SAME_DAY);
        }

        Travel travel = new Travel();
        travel.setTruck(truck);
        travel.setSchedule(schedule);
        travel.setUser(truck.getDriver());
        travel.setStartDate(dto.getStartDate());
        travel.setEndDate(dto.getEndDate());
        Travel saved = travelRepository.save(travel);

        return toResponse(saved);
    }


    public TravelResponse getTravelById(String travelId) {
        permissionService.getUser(
                List.of(PermissionKey.VIEW,PermissionKey.MANAGE),
                PermissionType.TRAVEL
        );
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAVEL_NOT_FOUND));

        return toResponse(travel);
    }


    public Page<TravelScheduleReportResponse> getALlTravels(int page, int size) {
        permissionService.getUser(
                List.of(PermissionKey.VIEW,PermissionKey.MANAGE),
                PermissionType.TRAVEL
        );
        Pageable pageable = PageRequest.of(page, size);
        return  travelRepository.findTravelPage(pageable);

    }

    @Transactional
    public TravelResponse updateTravel(String travelId, TravelRequest dto) {
        permissionService.getUser(
                List.of(PermissionKey.MANAGE),
                PermissionType.TRAVEL
        );
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAVEL_NOT_FOUND));

        Truck truck = truckRepository.findById(dto.getTruckId())
                .orElseThrow(() -> new AppException(ErrorCode.TRUCK_NOT_FOUND));

        Schedule schedule = scheduleRepository.findById(dto.getScheduleId())
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new AppException(ErrorCode.START_DATE_AND_END_DATE_NOT_NULL);
        }
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new AppException(ErrorCode.END_DATE_GREATER_OR_EQUAL_START_DATE);
        }

        if (truck.getStatus() != TruckStatus.AVAILABLE) {
            throw new AppException(ErrorCode.TRUCK_NOT_ACTIVE);
        }

        boolean existed = travelRepository
                .existsTravel(
                        truck.getId(),
                        dto.getStartDate(),
                        travelId
                );

        if (existed) {
            throw new AppException(ErrorCode.TRUCK_ALREADY_IN_TRAVEL_TODAY);
        }

        if (travel.getExpenses() != null) {
            boolean hasApprovedExpense = travel.getExpenses().stream()
                    .anyMatch(e -> e.getApproval() == ApprovalStatus.APPROVED);
            if (hasApprovedExpense) {
                throw new AppException(ErrorCode.TRAVEL_LOCKED_BY_EXPENSE);
            }
        }

        travel.setTruck(truck);
        travel.setSchedule(schedule);
        travel.setUser(truck.getDriver());
        travel.setStartDate(dto.getStartDate());
        travel.setEndDate(dto.getEndDate());

        Travel saved = travelRepository.save(travel);

        return toResponse(saved);
    }

    @Transactional
    public void deleteTravel(String travelId) {
        permissionService.getUser(
                List.of(PermissionKey.MANAGE),
                PermissionType.TRAVEL
        );
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAVEL_NOT_FOUND));

        travelRepository.delete(travel);
    }




}
