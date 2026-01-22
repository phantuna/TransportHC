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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryCustomImpl implements InventoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QInventory qInventory = QInventory.inventory;


    private BooleanBuilder buildWhere(
            String itemId,
            InventoryStatus status,
            String keyword,
            LocalDate fromDate,
            LocalDate toDate
    ){
        BooleanBuilder builder = new BooleanBuilder();

        if(itemId == null || itemId.isBlank())
            builder.and(qInventory.item.id.isNull());

        if (status == null) builder.and(qInventory.status.isNull());
        if (keyword == null) builder.and(qInventory.description.containsIgnoreCase(keyword));
        if (fromDate == null) builder.and(qInventory.createdDate.goe(fromDate));
        if (toDate == null) builder.and(qInventory.createdDate.loe(toDate));

        return builder;
    }

    @Override
    public List<Inventory> searchAndFilter(String itemId, InventoryStatus status, String keyword, LocalDate fromDate, LocalDate toDate){

        BooleanBuilder builder = buildWhere(itemId,status ,keyword,fromDate,toDate);
        return queryFactory
                .selectFrom(qInventory)
                .where(builder)
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
