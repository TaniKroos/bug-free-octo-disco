package com.example.brokerportal.quoteservice.repositories;

import com.example.brokerportal.quoteservice.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByQuoteId(Long quoteId);
}
