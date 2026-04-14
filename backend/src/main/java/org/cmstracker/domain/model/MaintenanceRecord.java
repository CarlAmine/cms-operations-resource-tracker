package org.cmstracker.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "maintenance_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_by_id", nullable = false)
    private User reportedBy;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceType type;

    @Column(nullable = false)
    private Instant scheduledStart;

    @Column
    private Instant scheduledEnd;

    @Column
    private Instant actualEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MaintenanceStatus status = MaintenanceStatus.PLANNED;

    @Column(length = 1000)
    private String resolution;

    public enum MaintenanceType {
        PREVENTIVE, CORRECTIVE, CALIBRATION, UPGRADE, INSPECTION
    }

    public enum MaintenanceStatus {
        PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
