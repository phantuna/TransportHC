package org.example.webapplication.repository.user;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.entity.QTruck;
import org.example.webapplication.entity.QUser;
import org.example.webapplication.entity.Role_Permission.QRole;
import org.example.webapplication.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    private final QUser qUser = QUser.user;
    private final QRole qRole = QRole.role;

    @Override
    public Page<User> findAllByRole_Id(String roleId, Pageable pageable) {
        List<User> content = queryFactory
                .selectDistinct(qUser)
                .from(qUser)
                .join(qUser.roles, qRole).fetchJoin()
                .where(qRole.id.eq(roleId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        Long total = queryFactory
                .select(qUser.countDistinct())
                .from(qUser)
                .join(qUser.roles, qRole)
                .where(qRole.id.eq(roleId))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<User> findUsersByRoleId(String roleId) {
        QUser qUser = QUser.user;
        QRole qRole = QRole.role;

        return queryFactory.selectFrom(qUser)
                .join(qUser.roles, qRole)
                .where(qRole.id.eq(roleId))
                .distinct()
                .fetch();
    }

    @Override
    public List<User> findAvailableDrivers(String currentTruckId) {
        QUser qUser = QUser.user;
        QRole qRole = QRole.role;
        QTruck qTruck = QTruck.truck;

        BooleanExpression isDriver = qRole.id.eq("R_DRIVER");
        BooleanExpression isNotAssigned = qTruck.id.isNull();

        if (currentTruckId != null && !currentTruckId.isBlank()) {
            isNotAssigned = isNotAssigned.or(qTruck.id.eq(currentTruckId));
        }

        return queryFactory.selectFrom(qUser)
                .join(qUser.roles, qRole)
                .leftJoin(qTruck).on(qTruck.driver.eq(qUser))
                .where(isDriver.and(isNotAssigned))
                .distinct()
                .fetch();
    }
}
