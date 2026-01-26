package org.example.webapplication.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.example.webapplication.entity.QExpense;
import org.example.webapplication.entity.QSchedule;
import org.example.webapplication.enums.ApprovalStatus;

public abstract class BaseRepository {


    protected NumberExpression<Double> approvedExpenseSum(QExpense qExpense) {
        return Expressions.cases()
                .when(qExpense.approval.eq(ApprovalStatus.APPROVED))
                .then(qExpense.expense)
                .otherwise(0.0)
                .sum();
    }

    protected NumberExpression<Double> totalExpense(
            QSchedule qSchedule,
            QExpense qExpense
    ) {
        return qSchedule.expense.coalesce(0.0)
                .add(approvedExpenseSum(qExpense));
    }
}
