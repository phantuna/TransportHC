package org.example.webapplication.repository.expense;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.expense.ExpenseResponse;
import org.example.webapplication.entity.*;
import org.example.webapplication.enums.ApprovalStatus;
import org.example.webapplication.enums.TypeExpense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.example.webapplication.entity.QExpense;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ExpenseRepositoryCustomImpl implements ExpenseRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QExpense qExpense = QExpense.expense1;
    private final QTravel qTravel = QTravel.travel;
    private final QUser qUser = QUser.user;

    private BooleanBuilder buildWhere(String truckId , LocalDate from, LocalDate to,String driverName){
        BooleanBuilder builder = new BooleanBuilder();
        if(driverName != null && !driverName.isEmpty()){
            builder.and(qUser.username.eq(driverName));
        }

        if (from != null && to != null) {
            builder.and(qTravel.startDate.between(from, to));
        } else if (from != null) {
            builder.and(qTravel.startDate.goe(from));
        } else if (to != null) {
            builder.and(qTravel.startDate.loe(to));
        }
        if(truckId !=null && !truckId.isEmpty()){
            builder.and(qTravel.truck.id.eq(truckId));
        }

        return builder;
    }

    @Override
    public Double sumApprovedExpense(String truckId,LocalDate fromDate,LocalDate toDate){
        BooleanBuilder builder = buildWhere(truckId,fromDate,toDate,null);
        return queryFactory
                .select(qExpense.expense.sum())
                .from(qExpense)
                .join(qExpense.travel,qTravel)
                .where(builder.and(qExpense.approval.eq(ApprovalStatus.APPROVED)))
                .fetchOne();

    }

    @Override
    public Page<ExpenseResponse> findExpenseReport(
            String truckId,
            LocalDate fromDate,
            LocalDate toDate,
            String driverName,
            Pageable pageable
    ){
        BooleanBuilder builder = buildWhere(truckId,fromDate,toDate,driverName);
        List<ExpenseResponse> response = queryFactory
                .select(Projections.bean(
                        ExpenseResponse.class,
                        qExpense.id,
                        qExpense.type,
                        qExpense.expense,
                        qExpense.description,
                        qExpense.approval,
                        qTravel.id.as("travelId"),
                        qUser.username.as("driverName"),
                        qExpense.incurredDate,
                        qExpense.createdDate,
                        qExpense.modifiedBy

                ))
                .from(qExpense)
                .join(qExpense.travel,qTravel)
                .join(qTravel.user,qUser)
                .where(builder)
                .fetch();

        Long total = queryFactory
                .select(qExpense.count())
                .from(qExpense)
                .join(qExpense.travel, qTravel)
                .join(qTravel.user, qUser)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(response, pageable, total);

    }
    @Override
    public List<Tuple> sumExpenseByDriverAndMonth(
            String driverId,
            LocalDate from,
            LocalDate to
    ) {
        QExpense expense = QExpense.expense1;
        QTravel travel = QTravel.travel;

        return queryFactory
                .select(
                        expense.type,
                        expense.expense.sum()
                )
                .from(expense)
                .join(expense.travel, travel)
                .where(
                        travel.user.id.eq(driverId),
                        expense.approval.eq(ApprovalStatus.APPROVED),
                        expense.incurredDate.between(from, to)
                )
                .groupBy(expense.type)
                .fetch();
    }

}
