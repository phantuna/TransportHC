package org.example.webapplication.repository.travel;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.dto.response.schedule.ScheduleDailyTotalResponse;
import org.example.webapplication.dto.response.travel.TravelResponse;
import org.example.webapplication.dto.response.travel.TravelScheduleReportResponse;
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

    public List<ScheduleDailyTotalResponse> countTripsPerDay_ByTravelTripCountSum(
            String truckId, LocalDate from, LocalDate to
    ) {
        return queryFactory
                .select(Projections.constructor(
                        ScheduleDailyTotalResponse.class,
                        qTravel.startDate,
                        qTravel.tripCount.coalesce(0L).sum()
                ))
                .from(qTravel)
                .join(qTravel.truck, qTruck)
                .where(
                        qTruck.id.eq(truckId),
                        from != null ? qTravel.startDate.goe(from) : null,
                        to != null ? qTravel.startDate.loe(to) : null
                )
                .groupBy(qTravel.startDate)
                .orderBy(qTravel.startDate.asc())
                .fetch();
    }

    @Override
    public List<Travel> findByTruck_IdAndStartDateBetween(String truckId, LocalDate fromDate, LocalDate toDate) {
        BooleanBuilder buildWhere = buildWhere(truckId,null,fromDate,toDate,null);

        return queryFactory
                .selectFrom(qTravel)
                .where(buildWhere)
                .fetch();
    };

    @Override
    public List<Travel> findByUserAndStartDateBetween(User user, LocalDate startDate, LocalDate endDate) {
        BooleanBuilder buildWhere = buildWhere(null,user,startDate,endDate,null);

        return queryFactory
                .selectFrom(qTravel)
                .where(buildWhere)
                .fetch();
    };
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


}
