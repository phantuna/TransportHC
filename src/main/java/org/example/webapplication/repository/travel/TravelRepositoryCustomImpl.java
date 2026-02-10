package org.example.webapplication.repository.travel;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.travel.TravelDailyReportItemResponse;
import org.example.webapplication.dto.response.travel.TravelDailyReportResponse;
import org.example.webapplication.dto.response.travel.TravelScheduleReportResponse;
import org.example.webapplication.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TravelRepositoryCustomImpl implements TravelRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    private final QTravel qTravel = QTravel.travel;
    private final QSchedule qSchedule = QSchedule.schedule;
    private final QExpense  qExpense = QExpense.expense1;
    private final QTruck qTruck = QTruck.truck;
    private final QUser qUser = QUser.user;

    private BooleanBuilder buildWhere(
            String truckId,
            User user,
            LocalDate from,
            LocalDate to,
            LocalDate date
    ){

        BooleanBuilder builder = new BooleanBuilder();
        if (truckId != null){
            builder.and(qTravel.truck.id.eq(truckId));
        }

        if (user != null){
            builder.and(qTravel.user.eq(user));
        }
        if (from != null || to != null){
            builder.and(qTravel.startDate.between(from,to));
        }
        if (date != null){
            builder.and(qTravel.startDate.eq(date));
        }
        return builder;
    }

    @Override
    public List<TravelDailyReportItemResponse> dailyTravelReport(
            String truckId,
            Integer month,
            Integer year
    ) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        return queryFactory
                .select(Projections.constructor(
                        TravelDailyReportItemResponse.class,
                        Expressions.constant(0), // STT set sau
                        qUser.username,
                        qUser.username,
                        qTruck.licensePlate,
                        qTruck.ganMooc,
                        qTravel.startDate,
                        qSchedule.startPlace,
                        qSchedule.endPlace,
                        qTravel.id.count()                ))
                .from(qTravel)
                .join(qTravel.user, qUser)
                .join(qTravel.truck, qTruck)
                .join(qTravel.schedule, qSchedule)
                .where(
                        qTravel.startDate.between(from, to),
                        truckId != null ? qTruck.id.eq(truckId) : null
                )
                .groupBy(
                        qUser.id,
                        qTruck.id,
                        qSchedule.id,
                        qTravel.startDate
                )
                .orderBy(qTravel.startDate.asc())
                .fetch();
    }

    @Override
    public boolean existsByTruck_IdAndStartDate(String truckId, LocalDate startDate) {
        BooleanBuilder buildWhere = buildWhere(truckId,null,startDate,null,null);
        return queryFactory
                .selectOne()
                .from(qTravel)
                .where(buildWhere)
                .fetchFirst() != null;
    };
    @Override
    public boolean existsTravel(String truckId, LocalDate startDate, String travelId){
        BooleanBuilder buildWhere = buildWhere(truckId,null,null,null,startDate);
        return queryFactory
                .selectOne()
                .from(qTravel)
                .where(buildWhere)
                .fetchFirst() != null;
    };
    @Override
    public boolean existsActiveTravelToday(String truckId) {
        BooleanBuilder buildWhere = buildWhere(truckId,null,null,null,null);
        return queryFactory
                .selectOne()
                .from(qTravel)
                .where(buildWhere)
                .fetchFirst() != null;
    };

    @Override
    public Page<TravelScheduleReportResponse> findTravelPage(Pageable pageable) {
        List<TravelScheduleReportResponse> data =
                queryFactory
                        .select(Projections.constructor(
                                TravelScheduleReportResponse.class,
                                qTravel.id,
                                qTruck.licensePlate,
                                qUser.username,
                                qSchedule.startPlace,
                                qSchedule.endPlace,
                                qTravel.startDate,
                                qTravel.endDate,
                                qTravel.schedule.id,
                                qExpense.expense.sum().coalesce(0.0)
                        ))
                        .from(qTravel)
                        .leftJoin(qTravel.truck, qTruck)
                        .leftJoin(qTravel.user, qUser)
                        .leftJoin(qTravel.schedule, qSchedule)
                        .leftJoin(qTravel.expenses, qExpense)
                        .groupBy(
                                qTravel.id,
                                qTruck.licensePlate,
                                qUser.username,
                                qSchedule.startPlace,
                                qSchedule.endPlace
                        )
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        long total = Optional.ofNullable(
                queryFactory
                .select(qTravel.count())
                .from(qTravel)
                .fetchOne()
        ).orElse(0L);
        return new PageImpl<>(data, pageable, total);
    }

    @Override
    public Travel findCurrentBySchedule(String scheduleId, LocalDate today) {
        return queryFactory
                .selectFrom(qTravel)
                .where(
                        qTravel.schedule.id.eq(scheduleId),
                        qTravel.startDate.loe(today),
                        qTravel.endDate.goe(today)
                )
                .orderBy(qTravel.startDate.desc())
                .fetchFirst();
    }

    @Override
    public Page<Travel> findTravelPageWithFetch(Pageable pageable) {

        List<Travel> content = queryFactory
                .selectDistinct(qTravel)
                .from(qTravel)
                .leftJoin(qTravel.truck, qTruck).fetchJoin()
                .leftJoin(qTravel.user, qUser).fetchJoin()
                .leftJoin(qTravel.schedule, qSchedule).fetchJoin()
                .leftJoin(qTravel.expenses, qExpense).fetchJoin()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(qTravel.countDistinct())
                .from(qTravel)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }


}
