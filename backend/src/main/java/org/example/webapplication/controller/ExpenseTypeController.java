package org.example.webapplication.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.request.expense.ExpenseTypeRequest;
import org.example.webapplication.dto.response.expense.ExpenseTypeResponse;
import org.example.webapplication.service.ExpenseTypeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expense-type")
public class ExpenseTypeController {

    private final ExpenseTypeService expenseTypeService;

    @GetMapping("/getAll")
    @PreAuthorize("isAuthenticated()")
    public List<ExpenseTypeResponse> getTypes() {
        return expenseTypeService.getExpenseTypes();
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ExpenseTypeResponse createExpenseType(@Valid @RequestBody ExpenseTypeRequest request){
        return expenseTypeService.createExpenseType(request);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("isAuthenticated()")
    public ExpenseTypeResponse updateExpenseType(@Valid @RequestBody ExpenseTypeRequest request, @PathVariable String id){
        return expenseTypeService.updateExpenseType(id, request);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public void deleteExpenseType(@PathVariable String id){
        expenseTypeService.deleteExpenseType(id);
    }
}