package org.example.webapplication.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.enums.ApprovalStatus;
import org.example.webapplication.dto.request.expense.ExpenseRequest;
import org.example.webapplication.dto.response.expense.ExpenseResponse;
import org.example.webapplication.entity.Expense;
import org.example.webapplication.entity.Travel;
import org.example.webapplication.entity.Truck;
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

@Service
@RequiredArgsConstructor
public class ExpenseService {
    public final ExpenseRepository expenseRepository;
    public final TravelRepository travelRepository;

    public ExpenseResponse toResponse(Expense expense, String username) {
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
                .CreatedDate(expense.getCreatedDate())
                .build();
    }


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

        return toResponse(saved, username);
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
        return toResponse(saved, username);
    }

    @PreAuthorize("hasAuthority('UPDATE_EXPENSE')")
    public ExpenseResponse updatedExpense(String id , ExpenseRequest dto){
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

    @PreAuthorize("hasAuthority('MANAGER_EXPENSE') OR hasAuthority('VIEW_EXPENSE')")
    public Page<ExpenseResponse> getAllExpenses(int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        Page<Expense> expensePage = expenseRepository.findAll(pageable);

        return expensePage.map(expense ->
                toResponse(expense, expense.getModifiedBy())
        );
    }
    @PreAuthorize("hasAuthority('VIEW_EXPENSE')")
    public ExpenseResponse getExpenseById(String id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXPENSE_NOT_FOUND));
        String modifyBy = expense.getModifiedBy();

        return toResponse(expense, modifyBy);
    }

    @PreAuthorize("hasAuthority('MANAGER_EXPENSE')")
    @Transactional
    public void deleteExpense(String expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new AppException(ErrorCode.EXPENSE_NOT_FOUND));

        expenseRepository.delete(expense);
    }
}
