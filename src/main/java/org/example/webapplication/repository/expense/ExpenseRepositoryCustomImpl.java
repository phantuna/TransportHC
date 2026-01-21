package org.example.webapplication.repository.expense;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.entity.Expense;
import org.example.webapplication.enums.TypeExpense;
import org.springframework.stereotype.Repository;
import org.example.webapplication.entity.QExpense;
import org.example.webapplication.entity.QExpense;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ExpenseRepositoryCustomImpl implements ExpenseRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QExpense qExpense = QExpense.expense1;

    private BooleanBuilder buildWhere(
            String keyword,
            TypeExpense type,
            Boolean deleted
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        if (deleted != null) {
            builder.and(qExpense.deleted.eq(deleted ? 1 : 0));
        }


        if (type != null) {
            builder.and(qExpense.type.eq(type));
        }

        if (keyword != null && !keyword.isBlank()) {
            builder.and(qExpense.description.containsIgnoreCase(keyword));
        }

        return builder;
    }

    @Override
    public List<Expense> searchExpenses(
            String keyword,
            TypeExpense type,
            Boolean deleted,
            int page,
            int size
    ) {
        BooleanBuilder where = buildWhere(keyword, type, deleted);

        return queryFactory
                .selectFrom(qExpense)
                .where(where)
                .offset((long) page * size)
                .limit(size)
                .orderBy(qExpense.incurredDate.desc())
                .fetch();
    }
}
