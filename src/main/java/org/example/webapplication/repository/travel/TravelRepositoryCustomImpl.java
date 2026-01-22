package org.example.webapplication.repository.travel;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.entity.QTravel;
import org.example.webapplication.entity.Travel;
import org.example.webapplication.entity.User;
import org.example.webapplication.enums.TravelStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TravelRepositoryCustomImpl implements TravelRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    private final QTravel qTravel = QTravel.travel;

    private BooleanExpression truckIdEq(String truckId){
        return truckId != null ? qTravel.truck.id.eq(truckId) : null;
    }
    private BooleanExpression userEq(User user){
        return user != null ? qTravel.user.eq(user) : null;
    }
    private BooleanExpression startDateBetween(LocalDate from, LocalDate to){
        if(from == null || to == null)
            return null;
        return qTravel.startDate.between(from,to);
    }
    private BooleanExpression startDateEq(LocalDate date) {
        return date != null ? qTravel.startDate.eq(date) : null;
    }

    @Override
    public List<Travel> findByTruck_IdAndStartDateBetween(String truckId, LocalDate fromDate, LocalDate toDate) {

        return queryFactory
                .selectFrom(qTravel)
                .where(
                    truckIdEq(truckId),
                    startDateBetween(fromDate, toDate)
                )
                .fetch();
    };

    @Override
    public List<Travel> findByUserAndStartDateBetween(User user, LocalDate startDate, LocalDate endDate) {

        return queryFactory
                .selectFrom(qTravel)
                .where(
                     userEq(user),
                     startDateBetween(startDate, endDate)
                )
                .fetch();
    };
    @Override
    public boolean existsByTruck_IdAndStartDate(String truckId, LocalDate startDate) {

        return queryFactory
                .selectOne()
                .from(qTravel)
                .where(
                        truckIdEq(truckId),
                        startDateEq(startDate)
                )
                .fetchFirst() != null;
    };
    @Override
    public boolean existsTravel(String truckId, LocalDate startDate, String travelId){

        return queryFactory
                .selectOne()
                .from(qTravel)
                .where(
                        truckIdEq(truckId),
                        startDateEq(startDate),
                        travelId != null ? qTravel.id.ne(travelId) : null
                )
                .fetchFirst() != null;
    };
    @Override
    public boolean existsActiveTravelToday(String truckId) {

        return queryFactory
                .selectOne()
                .from(qTravel)
                .where(
                        truckIdEq(truckId),
                        startDateEq(LocalDate.now())
                )
                .fetchFirst() != null;
    };

}
