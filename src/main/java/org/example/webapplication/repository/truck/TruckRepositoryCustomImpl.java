package org.example.webapplication.repository.truck;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.report.ExpenseReportDetailResponse;
import org.example.webapplication.dto.response.report.ExpenseSummaryResponse;
import org.example.webapplication.entity.*;
import org.example.webapplication.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TruckRepositoryCustomImpl implements TruckRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QTruck qTruck = QTruck.truck;
    private final QTravel qTravel = QTravel.travel;
    private final QUser qUser = QUser.user;
    private final QExpense qExpense = QExpense.expense1;

    @Override
    public Page<ExpenseSummaryResponse> getAllTruckExpenseSummary(
            Pageable pageable
    ) {
        List<ExpenseSummaryResponse> content = queryFactory
                .select(Projections.constructor(
                        ExpenseSummaryResponse.class,
                        qTruck.id,
                        qTruck.licensePlate,
                        qUser.username,
                        qExpense.expense.sum()
                ))
                .from(qExpense)
                .join(qExpense.travel, qTravel)
                .join(qTravel.truck, qTruck)
                .leftJoin(qTruck.driver, qUser)
                .where(qExpense.approval.eq(ApprovalStatus.APPROVED))
                .groupBy(qTruck.id, qTruck.licensePlate, qUser.username)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total =Optional.ofNullable(
                queryFactory
                .select(qTruck.id.countDistinct())
                .from(qExpense)
                .join(qExpense.travel, qTravel)
                .join(qTravel.truck, qTruck)
                .where(qExpense.approval.eq(ApprovalStatus.APPROVED))
                .fetchOne()
        ).orElse(0L);
        return new PageImpl<>(content, pageable, total);
    }


    @Override
    public Page<ExpenseReportDetailResponse> getExpenseDetailsByTruck(
            String truckId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        List<ExpenseReportDetailResponse> content = queryFactory
                .select(Projections.bean(
                        ExpenseReportDetailResponse.class,
                        qExpense.type.as("type"),
                        qExpense.expense.as("expense"),
                        qExpense.description.as("description"),
                        qExpense.approval.as("approval"),
                        qTravel.id.as("travelId"),
                        qUser.username.as("driverName"),
                        qExpense.incurredDate.as("incurredDate"),
                        qExpense.modifiedBy.as("modifiedBy"),
                        qExpense.createdDate.as("createdDate")
                ))
                .from(qExpense)
                .join(qExpense.travel, qTravel)
                .join(qTravel.truck, qTruck)
                .leftJoin(qTruck.driver, qUser)
                .where(
                        qTruck.id.eq(truckId),
                        qExpense.approval.eq(ApprovalStatus.APPROVED),
                        from != null ? qExpense.incurredDate.goe(from) : null,
                        to != null ? qExpense.incurredDate.loe(to) : null
                )
                .orderBy(qExpense.incurredDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                queryFactory
                .select(qExpense.count())
                .from(qExpense)
                .join(qExpense.travel, qTravel)
                .join(qTravel.truck, qTruck)
                .where(
                        qTruck.id.eq(truckId),
                        qExpense.approval.eq(ApprovalStatus.APPROVED),
                        from != null ? qExpense.incurredDate.goe(from) : null,
                        to != null ? qExpense.incurredDate.loe(to) : null
                )
                .fetchOne()
        ).orElse(0L);
        return new PageImpl<>(content, pageable, total);
    }

}
