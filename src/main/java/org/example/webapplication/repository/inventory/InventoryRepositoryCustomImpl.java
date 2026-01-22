package org.example.webapplication.repository.inventory;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.webapplication.entity.Inventory;
import org.example.webapplication.entity.QInventory;
import org.example.webapplication.enums.InventoryStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryCustomImpl implements InventoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QInventory qInventory = QInventory.inventory;

    private BooleanExpression itemIdEq(String itemId) {
        if (itemId == null || itemId.isBlank()) return null;
        return qInventory.item.id.eq(itemId);
    }
    private BooleanExpression statusEq(InventoryStatus status) {
        if (status == null) return null;
        return qInventory.status.eq(status);
    }
    private BooleanExpression keywordLike(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return qInventory.description.containsIgnoreCase(keyword)
                .or(qInventory.customerName.containsIgnoreCase(keyword));
    }
    private BooleanExpression fromDateGoe(LocalDateTime fromDate) {
        if (fromDate == null) return null;
        return qInventory.createdDate.goe(fromDate);
    }
    private BooleanExpression toDateLoe(LocalDateTime toDate) {
        if (toDate == null) return null;
        return qInventory.createdDate.loe(toDate);
    }


    @Override
    public List<Inventory> searchAndFilter(String itemId, InventoryStatus status, String keyword, LocalDateTime fromDate, LocalDateTime toDate){

        return queryFactory
                .selectFrom(qInventory)
                .where(
                        itemIdEq(itemId),
                        statusEq(status),
                        keywordLike(keyword),
                        fromDateGoe(fromDate),
                        toDateLoe(toDate)
                )
                .fetch();

    }

    @Override
    public List<Tuple> getInventorySummary() {

        return queryFactory
                .from(qInventory)
                .select(
                   qInventory.item.id,
                   qInventory.quantity.sum()
                )
                .groupBy(qInventory.item.id)
                .fetch();

    }
}
