package org.example.webapplication.repository.expense;

import org.example.webapplication.entity.ExpenseTypeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseTypeConfigRepository
        extends JpaRepository<ExpenseTypeConfig,String> {
}