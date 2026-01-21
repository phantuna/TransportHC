package org.example.webapplication.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.request.expense.ExpenseRequest;
import org.example.webapplication.dto.response.expense.ExpenseResponse;
import org.example.webapplication.service.ExpenseService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expense")
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping("/created")
    @PreAuthorize("hasAuthority('CREATE_EXPENSE')")
    public ExpenseResponse createdExpense (@Valid  @RequestBody ExpenseRequest request){
        return expenseService.createdExpense(request);
    }

    @PutMapping("/updated/{id}")
    @PreAuthorize("hasAuthority('UPDATE_EXPENSE')")
    public ExpenseResponse updatedExpense (@Valid @RequestBody ExpenseRequest request,@PathVariable String id){
        return expenseService.updatedExpense(id, request);
    }

    @GetMapping("/getAll")
    @PreAuthorize("hasAuthority('MANAGER_EXPENSE') OR hasAuthority('VIEW_EXPENSE')")
    public Page<ExpenseResponse> getAllExpenses(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size){

        return expenseService.getAllExpenses(page ,size);
    }

    @PostMapping("/getById/{id}")
    @PreAuthorize("hasAuthority('VIEW_EXPENSE')")
    public ExpenseResponse getExpenseById( @NotBlank @PathVariable String id){
        return expenseService.getExpenseById(id);
    }

    @PostMapping("/approval/{id}")
    @PreAuthorize("hasAuthority('APPROVE_EXPENSE')")
    public ExpenseResponse approvalExpense (@NotBlank @PathVariable String id){
        return expenseService.approvalExpense(id);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteExpense(@Valid @PathVariable String id){
        expenseService.deleteExpense(id);
    }
}
