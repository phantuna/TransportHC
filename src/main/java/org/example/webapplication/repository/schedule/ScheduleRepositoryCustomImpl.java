package org.example.webapplication.repository.schedule;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Query;
import com.querydsl.core.QueryFactory;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.expense.ExpenseResponse;
import org.example.webapplication.dto.response.schedule.ScheduleDocumentResponse;
import org.example.webapplication.dto.response.schedule.ScheduleResponse;
import org.example.webapplication.dto.response.travel.TravelScheduleReportResponse;
import org.example.webapplication.entity.*;
import org.example.webapplication.enums.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ScheduleRepositoryCustomImpl implements ScheduleRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QSchedule qSchedule = QSchedule.schedule;
    private final QTravel qTravel = QTravel.travel;
    private final QExpense qExpense = QExpense.expense1;
    private final QScheduleDocument qScheduleDocument = QScheduleDocument.scheduleDocument;
    private final QUser qUser = QUser.user;
    private final QTruck  qTruck = QTruck.truck;

    private BooleanBuilder travelWhere(
            String truckId,
            LocalDate from,
            LocalDate to
    ) {
        BooleanBuilder build = new BooleanBuilder();

        if (truckId != null) {
            build.and(qTravel.truck.id.eq(truckId));
        }

        if (from != null) {
            build.and(qTravel.startDate.goe(from));
        }

        if (to != null) {
            build.and(qTravel.endDate.loe(to));
        }

        return build;
    }
    private BooleanBuilder scheduleByDriverWhere(String username) {
        BooleanBuilder build = new BooleanBuilder();

        if (username != null) {
            build.and(
                    qSchedule.drivers.any().username.eq(username)
            );
        }

        return build;
    }
    private BooleanBuilder documentByScheduleIdsWhere(List<String> scheduleIds) {
        BooleanBuilder build = new BooleanBuilder();

        if (scheduleIds != null && !scheduleIds.isEmpty()) {
            build.and(qScheduleDocument.schedule.id.in(scheduleIds));
        }

        return build;
    }


    @Override
    public List<TravelScheduleReportResponse> getTravelScheduleReport (String truckId){
            BooleanBuilder travelWhere = travelWhere(truckId,null,null);

            return queryFactory
                    .select(Projections.constructor(
                            TravelScheduleReportResponse.class,
                            qTravel.id,
                            qTruck.licensePlate,
                            qTruck.driver.username,
                            qSchedule.startPlace,
                            qSchedule.endPlace,
                            qTravel.startDate,
                            qTravel.endDate,
                            qSchedule.id,
                            qSchedule.expense
                                    .coalesce(0.0)
                                    .add(
                                            Expressions.cases()
                                                    .when(qExpense.approval.eq(ApprovalStatus.APPROVED))
                                                    .then(qExpense.expense)
                                                    .otherwise(0.0)
                                                    .sum()
                                    )
                                    ))
                    .from(qTravel)
                    .leftJoin(qTravel.schedule, qSchedule)
                    .leftJoin(qTravel.expenses, qExpense)
                    .where(travelWhere)
                    .groupBy(
                            qTravel.id,
                            qTravel.startDate,
                            qTravel.endDate,
                            qSchedule.id,
                            qSchedule.startPlace,
                            qSchedule.endPlace,
                            qSchedule.expense
                    )
                    .fetch();
        }

        @Override
        public List<ExpenseResponse> findExpensesByTravel(String truckId) {
            BooleanBuilder travelWhere = travelWhere(truckId,null,null);
            return queryFactory
                    .select(Projections.bean(
                            ExpenseResponse.class,
                            qExpense.id,
                            qExpense.type,
                            qExpense.expense,
                            qExpense.description,
                            qExpense.approval,
                            qTravel.id.as("travelId"),
                            qExpense.incurredDate,
                            qExpense.createdDate,
                            qExpense.modifiedBy
                    ))
                    .from(qExpense)
                    .join(qExpense.travel, qTravel)
                    .where(travelWhere)
                    .fetch();
        }
    @Override
    public List<ScheduleDocumentResponse> findDocumentsByTravel(String truckId) {
        BooleanBuilder travelWhere = travelWhere(truckId,null,null);
        return queryFactory
                .select(Projections.bean(
                        ScheduleDocumentResponse.class,
                        qSchedule.id.as("scheduleId"),
                        qScheduleDocument.fileName,
                        qScheduleDocument.fileUrl,
                        qScheduleDocument.fileType,
                        qScheduleDocument.fileSize
                ))
                .from(qTravel)                                // ⭐ BẮT ĐẦU TỪ TRAVEL
                .leftJoin(qTravel.schedule, qSchedule)
                .leftJoin(qSchedule.documents, qScheduleDocument)
                .where(travelWhere)
                .fetch();
    }

    @Override
    public Page<ScheduleResponse> findSchedulePageByUsername(String username,Pageable pageable) {
        BooleanBuilder scheduleWhere = scheduleByDriverWhere(username);
        List<ScheduleResponse> responses = queryFactory
                .select(Projections.bean(
                        ScheduleResponse.class,
                        qSchedule.id,
                        qSchedule.startPlace,
                        qSchedule.endPlace,
                        qSchedule.expense,
                        qSchedule.description,
                        qSchedule.approval,
                        qUser.username.as("username")
                ))
                .from(qSchedule)
                .join(qSchedule.drivers, qUser)
                .where(scheduleWhere)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(qSchedule.count())
                .from(qSchedule)
                .join(qSchedule.drivers, qUser)
                .where(scheduleWhere)
                .fetchOne();
        return new PageImpl<>(responses, pageable,total == null ?0 : total);

    }

    @Override
    public List<ScheduleDocumentResponse> findDocumentsByScheduleIds(
            List<String> scheduleIds)
    {
        BooleanBuilder documentWhere = documentByScheduleIdsWhere(scheduleIds);
        return queryFactory
                .select(Projections.bean(
                        ScheduleDocumentResponse.class,
                        qScheduleDocument.schedule.id.as("scheduleId"),
                        qScheduleDocument.fileName,
                        qScheduleDocument.fileUrl,
                        qScheduleDocument.fileType,
                        qScheduleDocument.fileSize
                ))
                .from(qScheduleDocument)
                .where(qScheduleDocument.schedule.id.in(scheduleIds))
                .fetch();

    }

}
