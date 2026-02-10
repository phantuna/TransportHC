package org.example.webapplication.repository.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
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
}
