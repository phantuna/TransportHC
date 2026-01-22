package org.example.webapplication.repository.payroll;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.entity.*;
import org.example.webapplication.enums.ApprovalStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class PayrollRepositoryCustomImpl implements PayrollRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QExpense qExpense = QExpense.expense1;
    private final QTravel qTravel = QTravel.travel;
    private final QUser qUser = QUser.user;

    @Override
    public Double sumApproedExpenseByDriverAndMonth(
            User driver,
            LocalDate from,
            LocalDate to
    ){
        return queryFactory
                .select(qExpense.expense.sum())
                .from(qExpense)
                .join(qExpense.travel,qTravel)
                .where(
                        qTravel.user.eq(driver),
                        qTravel.startDate.between(from, to),
                        qExpense.approval.eq(ApprovalStatus.APPROVED)
                )
                .fetchOne();


    }

}
