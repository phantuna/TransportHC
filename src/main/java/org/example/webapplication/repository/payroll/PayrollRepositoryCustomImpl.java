package org.example.webapplication.repository.payroll;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.payroll.PayrollDetailResponse;
import org.example.webapplication.entity.*;
import org.example.webapplication.enums.ApprovalStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PayrollRepositoryCustomImpl implements PayrollRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    private final QExpense qExpense = QExpense.expense1;
    private final QTravel qTravel = QTravel.travel;
    private final QUser qUser = QUser.user;
    private final QPayroll qPayroll = QPayroll.payroll;




    private Expression<Double> approvedExpenseSumExpr(
            QUser user,
            LocalDate from,
            LocalDate to
    ) {
        return JPAExpressions
                .select(qExpense.expense.sum().coalesce(0.0))
                .from(qExpense)
                .join(qExpense.travel, qTravel)
                .where(
                        qTravel.user.eq(user),
                        qTravel.startDate.between(from, to),
                        qExpense.approval.eq(ApprovalStatus.APPROVED)
                );
    }


    @Override
    public List<PayrollDetailResponse> payrollByMonth(
            int month,
            int year,
            Pageable pageable
    ) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        Expression<Double> expenseSum = approvedExpenseSumExpr(qUser, from, to);
        NumberExpression<Double> totalSalary = Expressions.numberTemplate(
                        Double.class,
                        "{0} + {1} - {2}",
                        qUser.baseSalary,
                        expenseSum,
                        qPayroll.advanceSalary.coalesce(0.0)
                );
        return queryFactory
                .select(Projections.constructor(
                        PayrollDetailResponse.class,
                        qUser.id,
                        qUser.username,
                        Expressions.constant(month),
                        Expressions.constant(year),
                        qUser.baseSalary,
                        expenseSum,
                        qPayroll.advanceSalary.coalesce(0.0),
                        totalSalary
                ))
                .from(qUser)
                .leftJoin(qPayroll).on(qPayroll.user.eq(qUser))
                .where(qUser.roles.any().id.eq("R_DRIVER"))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public Double sumApproedExpenseByDriverAndMonth(
            User driver,
            LocalDate from,
            LocalDate to
    ) {
        return queryFactory
                .select(qExpense.expense.sum().coalesce(0.0))
                .from(qExpense)
                .join(qExpense.travel, qTravel)
                .where(
                        qTravel.user.eq(driver),
                        qTravel.startDate.between(from, to),
                        qExpense.approval.eq(ApprovalStatus.APPROVED)
                )
                .fetchOne();
    }
}
