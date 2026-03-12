package org.example.webapplication.repository.inventory;

import org.example.webapplication.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice,String> {
}
