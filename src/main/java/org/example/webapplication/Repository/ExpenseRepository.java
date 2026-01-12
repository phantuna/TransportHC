package org.example.webapplication.Repository;

import org.example.webapplication.Entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense,String> {

    List<Expense> findByTravel_Truck_Id(String truckId);

}
