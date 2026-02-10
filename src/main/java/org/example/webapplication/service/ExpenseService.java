package org.example.webapplication.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.PageResponse;
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
import org.example.webapplication.service.cache.ExpenseCacheService;
import org.example.webapplication.service.mapper.ExpenseMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final ExpenseMapper  expenseMapper;
    private final ExpenseCacheService expenseCacheService;


    @Caching(evict = {
            @CacheEvict(value = "expenses_list", allEntries = true),          // Xóa cache danh sách chi phí
            @CacheEvict(value = "report_truck_summary", allEntries = true),   // Xóa báo cáo tổng hợp
            @CacheEvict(value = "report_truck_detail", allEntries = true),    // Xóa báo cáo chi tiết
            @CacheEvict(value = "report_schedule", allEntries = true),        // Xóa báo cáo lịch trình
            @CacheEvict(value = "report_driver_expense", allEntries = true)   // Xóa báo cáo tài xế
    })
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

        return expenseMapper.toResponse(saved);
    }

    @Caching(evict = {
            @CacheEvict(value = "expenses_list", allEntries = true),
            @CacheEvict(value = "report_truck_summary", allEntries = true),
            @CacheEvict(value = "report_truck_detail", allEntries = true),
            @CacheEvict(value = "report_schedule", allEntries = true),
            @CacheEvict(value = "report_driver_expense", allEntries = true)
    })
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

        return expenseMapper.toResponse(saved);
    }

    @Caching(evict = {
            @CacheEvict(value = "expenses_list", allEntries = true),
            @CacheEvict(value = "report_truck_summary", allEntries = true),
            @CacheEvict(value = "report_truck_detail", allEntries = true),
            @CacheEvict(value = "report_schedule", allEntries = true),
            @CacheEvict(value = "report_driver_expense", allEntries = true)
    })
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

        return expenseMapper.toResponse(saved);
    }

    public PageResponse<ExpenseResponse> getAllExpenses(int page, int size) {
        permissionService.getUser(
                List.of(PermissionKey.MANAGE),
                PermissionType.EXPENSE
        );
        return expenseCacheService.getAllExpenses(page, size);
    }

    public ExpenseResponse getExpenseById(String id) {
        permissionService.getUser(
                List.of(PermissionKey.VIEW),
                PermissionType.EXPENSE
        );
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EXPENSE_NOT_FOUND));
        String modifyBy = expense.getModifiedBy();
        return expenseMapper.toResponse(expense);
    }

    @Caching(evict = {
            @CacheEvict(value = "expenses_list", allEntries = true),
            @CacheEvict(value = "report_truck_summary", allEntries = true),
            @CacheEvict(value = "report_truck_detail", allEntries = true),
            @CacheEvict(value = "report_schedule", allEntries = true),
            @CacheEvict(value = "report_driver_expense", allEntries = true)
    })
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