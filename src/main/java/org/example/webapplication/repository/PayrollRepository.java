package org.example.webapplication.repository;

import org.example.webapplication.entity.Payroll;
import org.example.webapplication.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll,String> {
    Optional<Payroll> findByUser(User user);


}
