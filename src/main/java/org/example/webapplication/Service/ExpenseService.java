package org.example.webapplication.Service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.Enum.ApprovalStatus;
import org.example.webapplication.Dto.request.Expense.ExpenseRequest;
import org.example.webapplication.Dto.response.Expense.ExpenseResponse;
import org.example.webapplication.Entity.Expense;
import org.example.webapplication.Entity.Travel;
import org.example.webapplication.Entity.Truck;
import org.example.webapplication.Exception.AppException;
import org.example.webapplication.Exception.ErrorCode;
import org.example.webapplication.Repository.ExpenseRepository;
import org.example.webapplication.Repository.TravelRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    public final ExpenseRepository expenseRepository;
    public final TravelRepository travelRepository;

    @PreAuthorize("hasAuthority('CREATE_EXPENSE')")
    public ExpenseResponse createdExpense(ExpenseRequest dto){
        Travel travel = travelRepository.findById(dto.getTravelId())
                .orElseThrow(() -> new AppException(ErrorCode.TRAVEL_NOT_FOUND));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Truck truck = travel.getTruck();
        Expense expense = new Expense();
        expense.setType(dto.getType());
        expense.setDescription(dto.getDescription());
        expense.setExpense(dto.getExpense());
        expense.setTravel(travel);
        expense.setApproval(ApprovalStatus.PENDING_APPROVAL);
        expense.setIncurredDate(dto.getIncurredDate());
        Expense saved = expenseRepository.save(expense);

        return ExpenseResponse.builder()
                .id(saved.getId())
                .type(saved.getType())
                .expense(saved.getExpense())
                .description(saved.getDescription())
                .approval(saved.getApproval())
                .travelId(saved.getTravel().getId())
                .driverName(username)
                .modifiedBy(username)
                .incurredDate(saved.getIncurredDate())
                .build();
    }

    //manager- supervisor
    @PreAuthorize("hasAuthority('APPROVE_EXPENSE')")
    public ExpenseResponse approvalExpense (String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Expense expense = expenseRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.EXPENSE_NOT_FOUND));
        Truck truck = expense.getTravel().getTruck();
        expense.setApproval(ApprovalStatus.APPROVED);
        expense.setModifiedBy(username);
        Expense saved = expenseRepository.save(expense);
        return ExpenseResponse.builder()
                .id(saved.getId())
                .type(saved.getType())
                .expense(saved.getExpense())
                .description(saved.getDescription())
                .approval(saved.getApproval())
                .travelId(saved.getTravel().getId())
                .driverName(truck.getDriver().getUsername())
                .incurredDate(saved.getIncurredDate())
                .modifiedBy(username)
                .build();
    }

    @PreAuthorize("hasAuthority('UPDATE_EXPENSE')")
    public ExpenseResponse updatedExpense(String id , ExpenseRequest dto){
        Travel travel = travelRepository.findById(dto.getTravelId())
                .orElseThrow(() -> new AppException(ErrorCode.TRAVEL_NOT_FOUND));

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXPENSE_NOT_FOUND));

        expense.setType(dto.getType());
        expense.setDescription(dto.getDescription());
        expense.setExpense(dto.getExpense());
        expense.setTravel(travel);
        expense.setIncurredDate(dto.getIncurredDate());
        expense.setApproval(ApprovalStatus.PENDING_APPROVAL);

        Expense saved = expenseRepository.save(expense);

        return ExpenseResponse.builder()
                .id(saved.getId())
                .type(saved.getType())
                .expense(saved.getExpense())
                .description(saved.getDescription())
                .approval(saved.getApproval())
                .travelId(saved.getTravel().getId())
                .incurredDate(saved.getIncurredDate())
                .build();
    }

    @PreAuthorize("hasAuthority('MANAGER_EXPENSE') OR hasAuthority('VIEW_EXPENSE')")
    public List<ExpenseResponse> getAllExpenses(){
        List<Expense> expenses = expenseRepository.findAll();

        List<ExpenseResponse> expenseResponses= new ArrayList<>();
        for (Expense expense : expenses) {
            Truck truck = expense.getTravel().getTruck();
            ExpenseResponse response = ExpenseResponse.builder()
                    .travelId(expense.getTravel().getId())
                    .id(expense.getId())
                    .type(expense.getType())
                    .approval(expense.getApproval())
                    .description(expense.getDescription())
                    .expense(expense.getExpense())
                    .driverName(truck.getDriver().getUsername())
                    .incurredDate(expense.getIncurredDate())
                    .modifiedBy(expense.getModifiedBy())
                    .CreatedDate(expense.getCreatedDate())
                    .build();

            expenseResponses.add(response);
        }

        return expenseResponses;
    }
    @PreAuthorize("hasAuthority('VIEW_EXPENSE')")
    public ExpenseResponse getExpenseById(String id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXPENSE_NOT_FOUND));
        Truck truck = expense.getTravel().getTruck();

        return ExpenseResponse.builder()
                .id(expense.getId())
                .travelId(expense.getTravel() != null ? expense.getTravel().getId() : null)
                .type(expense.getType())
                .approval(expense.getApproval())
                .description(expense.getDescription())
                .expense(expense.getExpense())
                .driverName(truck.getDriver().getUsername())
                .incurredDate(expense.getIncurredDate())
                .build();
    }


    @PreAuthorize("hasAuthority('MANAGER_EXPENSE')")
    @Transactional
    public void deleteExpense(String expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new AppException(ErrorCode.EXPENSE_NOT_FOUND));

        expenseRepository.delete(expense); // -> @SQLDelete sẽ UPDATE deleted=1
    }
}
