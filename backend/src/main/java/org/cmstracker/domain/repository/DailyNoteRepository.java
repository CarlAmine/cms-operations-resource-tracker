package org.cmstracker.domain.repository;

import org.cmstracker.domain.model.DailyNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyNoteRepository extends JpaRepository<DailyNote, Long> {
    List<DailyNote> findByNoteDateOrderByPinnedDescCreatedAtDesc(LocalDate date);
    List<DailyNote> findByNoteDateBetweenOrderByNoteDateDescCreatedAtDesc(LocalDate from, LocalDate to);
}
