package org.example.webapplication.Repository;

import org.example.webapplication.Entity.Inventory;
import org.example.webapplication.Enum.InventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory,String> {

    @Query("""
    SELECT 
      i.item.id,
      SUM(i.quantity)
    FROM Inventory i
    GROUP BY i.item.id
    """)
    List<Object[]> getInventorySummary();


    @Query("""
        SELECT i
        FROM Inventory i
        WHERE
            (:itemId IS NULL OR i.item.id = :itemId)
        AND (:status IS NULL OR i.status = :status)
        AND (
            :keyword IS NULL OR
            LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(i.customerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (:fromDate IS NULL OR i.createdDate >= :fromDate)
        AND (:toDate IS NULL OR i.createdDate <= :toDate)
    """)
        List<Inventory> searchAndFilter(
                @Param("itemId") String itemId,
                @Param("status") InventoryStatus status,
                @Param("keyword") String keyword,
                @Param("fromDate") java.time.LocalDateTime fromDate,
                @Param("toDate") java.time.LocalDateTime toDate
        );

}
