package org.cmstracker.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyNote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate noteDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 5000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Category category = Category.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Importance importance = Importance.NORMAL;

    @Column(nullable = false)
    @Builder.Default
    private boolean pinned = false;

    public enum Category {
        GENERAL, SAFETY, TECHNICAL, OPERATIONAL, ADMINISTRATIVE
    }

    public enum Importance {
        LOW, NORMAL, HIGH, CRITICAL
    }
}
