package org.example.webapplication.service.cache;

import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.PageResponse;
import org.example.webapplication.dto.response.expense.ExpenseResponse;
import org.example.webapplication.entity.Expense;
import org.example.webapplication.repository.expense.ExpenseRepository;
import org.example.webapplication.service.mapper.ExpenseMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseCacheService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    @Cacheable(
            value = "expenses_list",
            key = "'page:' + #page + ':size:' + #size"
    )
    public PageResponse<ExpenseResponse> getAllExpenses(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Expense> expensePage = expenseRepository.findExpensePage(pageable);

        List<ExpenseResponse> content = expensePage.getContent()
                .stream()
                .map(expenseMapper::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                page,
                size,
                expensePage.getTotalElements(),
                expensePage.getTotalPages()
        );
    }

}
