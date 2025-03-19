package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.quoteservice.entities.AuditLog;
import com.example.brokerportal.quoteservice.entities.Quote;
import com.example.brokerportal.quoteservice.enums.AuditAction;
import com.example.brokerportal.quoteservice.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void logAction(AuditAction action, Quote quote, String details, String performedBy) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .quote(quote)
                .changedDetails(details)
                .performedBy(performedBy)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(log);
    }
}
