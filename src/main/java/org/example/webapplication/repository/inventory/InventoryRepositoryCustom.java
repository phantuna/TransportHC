package org.example.webapplication.repository.inventory;

import com.querydsl.core.Tuple;
import org.example.webapplication.entity.Inventory;
import org.example.webapplication.enums.InventoryStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface InventoryRepositoryCustom {

    List<Inventory> searchAndFilter(
            String itemId,
            InventoryStatus status,
            String keyword,
            LocalDateTime fromDate,
            LocalDateTime toDate
    );

    List<Tuple> getInventorySummary();
}
