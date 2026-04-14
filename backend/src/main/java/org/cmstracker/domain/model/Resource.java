package org.cmstracker.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    public enum Category {
        DETECTOR, COMPUTING, NETWORKING, CRYOGENICS, ELECTRONICS, MECHANICAL, SAFETY, OTHER
    }

    public enum Status {
        AVAILABLE, BOOKED, MAINTENANCE, UNAVAILABLE
    }
}
