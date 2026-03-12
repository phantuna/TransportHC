package org.example.webapplication.repository.expense;

import org.example.webapplication.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense,String>, ExpenseRepositoryCustom {

    List<Expense> findByTravel_Truck_Id(String truckId);

}
