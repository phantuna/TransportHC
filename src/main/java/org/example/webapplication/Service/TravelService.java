package org.example.webapplication.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.parser.Authorization;
import org.example.webapplication.Enum.ApprovalStatus;
import org.example.webapplication.Enum.TruckStatus;
import org.example.webapplication.Dto.request.TravelRequest;
import org.example.webapplication.Dto.response.ExpenseResponse;
import org.example.webapplication.Dto.response.TravelResponse;
import org.example.webapplication.Entity.Expense;
import org.example.webapplication.Entity.Schedule;
import org.example.webapplication.Entity.Travel;
import org.example.webapplication.Entity.Truck;
import org.example.webapplication.Exception.AppException;
import org.example.webapplication.Exception.ErrorCode;
import org.example.webapplication.Repository.ScheduleRepository;
import org.example.webapplication.Repository.TravelRepository;
import org.example.webapplication.Repository.TruckRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelService {
    private final TravelRepository travelRepository;
    private final TruckRepository truckRepository;
    private final ScheduleRepository scheduleRepository;

    @PreAuthorize("hasAuthority('MANAGE_TRAVEL')")
    public TravelResponse createdTravel(TravelRequest dto) {

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
                        dto.getStartDate()
                );

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

        return TravelResponse.builder()
                .travelId(saved.getId())
                .truckPlate(truck.getLicensePlate())
                .driverName(
                        truck.getDriver() != null
                                ? truck.getDriver().getUsername()
                                : null
                )
                .scheduleName(schedule.getStartPlace() + " - " + schedule.getEndPlace())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .totalExpense(schedule.getExpense())
                .build();
    }


    public TravelResponse getTravelById(String travelId) {

        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAVEL_NOT_FOUND));

        List<ExpenseResponse> expenseResponses = new ArrayList<>();
        double totalExpense = 0;

        if (travel.getExpenses() != null) {
            for (Expense expense : travel.getExpenses()) {

                if (expense.getApproval() == ApprovalStatus.APPROVED) {
                    totalExpense += expense.getExpense();
                }
                ExpenseResponse er = ExpenseResponse.builder()
                        .id(expense.getId())
                        .type(expense.getType())
                        .expense(expense.getExpense())
                        .description(expense.getDescription())
                        .approval(expense.getApproval())
                        .travelId(travel.getId())
                        .incurredDate(expense.getIncurredDate())
                        .driverName(travel.getUser().getUsername())
                        .build();

                expenseResponses.add(er);
            }
        }

        Truck truck = travel.getTruck();
        Schedule schedule = travel.getSchedule();

        return TravelResponse.builder()
                .travelId(travel.getId())
                .truckPlate(truck.getLicensePlate())
                .driverName(
                        truck.getDriver() != null
                                ? truck.getDriver().getUsername()
                                : null
                )
                .scheduleName(schedule.getStartPlace() + " - " + schedule.getEndPlace())
                .startDate(travel.getStartDate())
                .endDate(travel.getEndDate())
                .expenses(expenseResponses)
                .totalExpense(schedule.getExpense())
                .build();
    }

    @PreAuthorize("hasAuthority('VIEW_TRAVEL') or hasAuthority('MANAGE_TRAVEL')")
    public List<TravelResponse> getALlTravels() {
        List<Travel> travels = travelRepository.findAll();
        List<TravelResponse> travelResponses = new ArrayList<>();

        for (Travel travel : travels) {
            double totalExpense = 0;
            List<ExpenseResponse> expenseResponses = new ArrayList<>();
            Truck truck = travel.getTruck();
            Schedule schedule = travel.getSchedule();
            if (travel.getExpenses() != null) {
                for (Expense expense : travel.getExpenses()) {

                    if (expense.getApproval() == ApprovalStatus.APPROVED) {
                        totalExpense += expense.getExpense();
                    }

                    ExpenseResponse er = ExpenseResponse.builder()
                            .id(expense.getId())
                            .type(expense.getType())
                            .expense(expense.getExpense())
                            .description(expense.getDescription())
                            .approval(expense.getApproval())
                            .travelId(travel.getId())
                            .driverName(truck.getDriver().getUsername())
                            .incurredDate(expense.getIncurredDate())
                            .build();

                    expenseResponses.add(er);
                }
            }
            TravelResponse response = TravelResponse.builder()
                    .travelId(travel.getId())
                    .truckPlate(truck.getLicensePlate())
                    .driverName(
                            truck.getDriver() != null
                                    ? truck.getDriver().getUsername()
                                    : null
                    )
                    .scheduleName(schedule.getStartPlace() + " - " + schedule.getEndPlace())
                    .startDate(travel.getStartDate())
                    .endDate(travel.getEndDate())
                    .expenses(expenseResponses)
                    .totalExpense(schedule.getExpense())
                    .build();
            travelResponses.add(response);
        }
        return travelResponses;
    }

    @PreAuthorize("hasAuthority('MANAGE_TRAVEL')")
    public TravelResponse updateTravel(String travelId, TravelRequest dto) {

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
                .existsByTruck_IdAndStartDateAndIdNot(
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

        return TravelResponse.builder()
                .travelId(saved.getId())
                .truckPlate(truck.getLicensePlate())
                .driverName(
                        truck.getDriver() != null
                                ? truck.getDriver().getUsername()
                                : null
                )
                .scheduleName(schedule.getStartPlace() + " - " + schedule.getEndPlace())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .totalExpense(schedule.getExpense())
                .build();
    }

    @PreAuthorize("hasAuthority('MANAGE_TRAVEL')")
    @Transactional
    public void deleteTravel(String travelId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new AppException(ErrorCode.TRAVEL_NOT_FOUND));

        travelRepository.delete(travel);
    }

}
