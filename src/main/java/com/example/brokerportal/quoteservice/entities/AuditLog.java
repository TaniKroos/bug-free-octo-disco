package com.example.brokerportal.quoteservice.entities;

import com.example.brokerportal.quoteservice.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AuditAction action;  // Better use Enum instead of String

    @Column(length = 2000)
    private String changedDetails; // JSON or description like "Quote status changed from DRAFT to BOUND"

    private LocalDateTime timestamp;

    private String performedBy; // Optional: broker username or id who did the action

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id")
    private Quote quote;
}
