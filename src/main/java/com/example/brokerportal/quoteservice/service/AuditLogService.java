package com.example.brokerportal.quoteservice.service;

import com.example.brokerportal.quoteservice.entities.Quote;
import com.example.brokerportal.quoteservice.enums.AuditAction;

public interface AuditLogService {
    void logAction(AuditAction action, Quote quote, String details, String performedBy);
}
