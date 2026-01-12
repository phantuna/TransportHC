package org.example.webapplication.Repository;

import org.example.webapplication.Entity.Inventory;
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


}
