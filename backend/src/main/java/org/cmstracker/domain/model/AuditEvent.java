package org.cmstracker.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "audit_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, length = 50)
    private String entityType;

    @Column
    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_id")
    private User performedBy;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(length = 5000)
    private String previousState;

    @Column(length = 5000)
    private String newState;

    @Column(nullable = false)
    private Instant occurredAt;
}
