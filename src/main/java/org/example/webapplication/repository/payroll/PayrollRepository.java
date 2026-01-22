package org.example.webapplication.repository.payroll;

import org.example.webapplication.entity.Payroll;
import org.example.webapplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll,String>, PayrollRepositoryCustom {
    Optional<Payroll> findByUser(User user);


}
