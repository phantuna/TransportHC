package org.example.webapplication.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.request.expense.ExpenseTypeRequest;
import org.example.webapplication.dto.response.expense.ExpenseTypeResponse;
import org.example.webapplication.entity.ExpenseTypeConfig;
import org.example.webapplication.enums.PermissionKey;
import org.example.webapplication.enums.PermissionType;
import org.example.webapplication.exception.AppException;
import org.example.webapplication.exception.ErrorCode;
import org.example.webapplication.repository.expense.ExpenseTypeConfigRepository;
import org.example.webapplication.service.cache.ExpenseCacheService;
import org.example.webapplication.service.mapper.ExpenseMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseTypeService {
    public final PermissionService  permissionService;
    private final ExpenseMapper expenseMapper;
    private final ExpenseCacheService expenseCacheService;
    private final ExpenseTypeConfigRepository expenseTypeConfigRepository;


    public List<ExpenseTypeResponse> getExpenseTypes(){
        permissionService.getUser(
                List.of(PermissionKey.MANAGE),
                PermissionType.EXPENSE
        );
        return expenseTypeConfigRepository.findAll()
                .stream()
                .map(x -> ExpenseTypeResponse.builder()
                        .id(x.getId())
                        .key(x.getKey())
                        .label(x.getLabel())
                        .description(x.getDescription())
                        .build()
                )
                .toList();
    }

    @Transactional
    public ExpenseTypeResponse createExpenseType(ExpenseTypeRequest request) {
        permissionService.getUser(
                List.of(PermissionKey.CREATE),
                PermissionType.EXPENSE
        );
        ExpenseTypeConfig config = new ExpenseTypeConfig();
        config.setKey(request.getKey());
        config.setLabel(request.getLabel());
        config.setDescription(request.getDescription());
        config.setActive(true);

        ExpenseTypeConfig saved = expenseTypeConfigRepository.save(config);

        return ExpenseTypeResponse.builder()
                .id(saved.getId())
                .key(saved.getKey())
                .label(saved.getLabel())
                .description(saved.getDescription())
                .build();
    }

    @Transactional
    public ExpenseTypeResponse updateExpenseType(String id, ExpenseTypeRequest request) {
        permissionService.getUser(
                List.of(PermissionKey.UPDATE),
                PermissionType.EXPENSE
        );
        ExpenseTypeConfig config = expenseTypeConfigRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        config.setLabel(request.getLabel());
        config.setDescription(request.getDescription());

        ExpenseTypeConfig saved = expenseTypeConfigRepository.save(config);

        return ExpenseTypeResponse.builder()
                .id(saved.getId())
                .key(saved.getKey())
                .label(saved.getLabel())
                .description(saved.getDescription())
                .build();
    }

    @Transactional
    public void deleteExpenseType(String id) {
        permissionService.getUser(
                List.of(PermissionKey.MANAGE),
                PermissionType.EXPENSE
        );
        ExpenseTypeConfig config = expenseTypeConfigRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
        expenseTypeConfigRepository.delete(config);
    }
}
