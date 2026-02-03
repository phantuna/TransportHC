package org.example.webapplication.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.enums.ApprovalStatus;
import org.example.webapplication.dto.request.expense.ExpenseRequest;
import org.example.webapplication.dto.response.expense.ExpenseResponse;
import org.example.webapplication.entity.Expense;
import org.example.webapplication.entity.Travel;
import org.example.webapplication.entity.Truck;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.exception.AppException;
import org.example.webapplication.exception.ErrorCode;
import org.example.webapplication.repository.expense.ExpenseRepository;
import org.example.webapplication.repository.travel.TravelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    public final ExpenseRepository expenseRepository;
    public final TravelRepository travelRepository;
    public final PermissionService  permissionService;

    private ExpenseResponse toResponse(Expense expense, String username) {
        Truck truck = expense.getTravel().getTruck();

        return ExpenseResponse.builder()
                .id(expense.getId())
                .type(expense.getType())
                .expense(expense.getExpense())
                .description(expense.getDescription())
                .approval(expense.getApproval())
                .travelId(expense.getTravel().getId())
                .driverName(truck.getDriver().getUsername())
                .incurredDate(expense.getIncurredDate())
                .modifiedBy(username)
                .createdDate(expense.getCreatedDate())
                .build();
    }

    @Transactional
    public ExpenseResponse createdExpense(ExpenseRequest dto){
        permissionService.getUser(
                List.of(PermissionKey.CREATE),
                PermissionType.EXPENSE
        );
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

        return toResponse(saved, username);
    }

    //manager- supervisor
    @Transactional
    public ExpenseResponse approvalExpense (String id){
        permissionService.getUser(
                List.of(PermissionKey.APPROVE),
                PermissionType.EXPENSE
        );
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Expense expense = expenseRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.EXPENSE_NOT_FOUND));
        Truck truck = expense.getTravel().getTruck();
        expense.setApproval(ApprovalStatus.APPROVED);
        expense.setModifiedBy(username);
        Expense saved = expenseRepository.save(expense);
        return toResponse(saved, username);
    }

    @Transactional
    public ExpenseResponse updatedExpense(String id , ExpenseRequest dto){
        permissionService.getUser(
                List.of(PermissionKey.UPDATE),
                PermissionType.EXPENSE
        );
        Travel travel = travelRepository.findById(dto.getTravelId())
                .orElseThrow(() -> new AppException(ErrorCode.TRAVEL_NOT_FOUND));
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXPENSE_NOT_FOUND));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        expense.setType(dto.getType());
        expense.setDescription(dto.getDescription());
        expense.setExpense(dto.getExpense());
        expense.setTravel(travel);
        expense.setIncurredDate(dto.getIncurredDate());
        expense.setApproval(ApprovalStatus.PENDING_APPROVAL);
        Expense saved = expenseRepository.save(expense);

        return toResponse(saved, username);
    }

    public Page<ExpenseResponse> getAllExpenses(int page, int size){
        permissionService.getUser(
                List.of(PermissionKey.MANAGE),
                PermissionType.EXPENSE
        );
        Pageable pageable = PageRequest.of(page, size);
        Page<Expense> expensePage = expenseRepository.findAll(pageable);
        return expensePage.map(expense ->
                toResponse(expense, expense.getModifiedBy())
        );
    }

    public ExpenseResponse getExpenseById(String id) {
        permissionService.getUser(
                List.of(PermissionKey.VIEW),
                PermissionType.EXPENSE
        );
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXPENSE_NOT_FOUND));
        String modifyBy = expense.getModifiedBy();
        return toResponse(expense, modifyBy);
    }

    @Transactional
    public void deleteExpense(String expenseId) {
        permissionService.getUser(
                List.of(PermissionKey.MANAGE),
                PermissionType.EXPENSE
        );
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new AppException(ErrorCode.EXPENSE_NOT_FOUND));
        expenseRepository.delete(expense);
    }
}
