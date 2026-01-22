package org.example.webapplication.repository.expense;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
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

    private BooleanExpression deletedEq(Boolean deleted) {
        if (deleted == null) return null;
        return qExpense.deleted.eq(deleted ? 1 : 0);
    }
    private BooleanExpression typeEq(TypeExpense type) {
        if (type == null) return null;
        return qExpense.type.eq(type);
    }
    private BooleanExpression keywordLike(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return qExpense.description.containsIgnoreCase(keyword);
    }


    @Override
    public List<Expense> searchExpenses(String keyword, TypeExpense type, Boolean deleted, int page, int size) {

        return queryFactory
                .selectFrom(qExpense)
                .where(
                        deletedEq(deleted),
                        typeEq(type),
                        keywordLike(keyword)
                )
                .offset((long) page * size)
                .limit(size)
                .orderBy(qExpense.incurredDate.desc())
                .fetch();
    }
}
