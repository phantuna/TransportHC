package org.example.webapplication.repository.inventory;

import org.example.webapplication.entity.Inventory;
import org.example.webapplication.enums.InventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory,String> , InventoryRepositoryCustom {

}
