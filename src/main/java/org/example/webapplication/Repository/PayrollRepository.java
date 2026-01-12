package org.example.webapplication.Repository;

import org.example.webapplication.Entity.Payroll;
import org.example.webapplication.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll,String> {
    Optional<Payroll> findByUser(User user);
}
