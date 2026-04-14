package org.cmstracker.application.service;

import lombok.RequiredArgsConstructor;
import org.cmstracker.application.dto.*;
import org.cmstracker.domain.model.*;
import org.cmstracker.domain.repository.*;
import org.cmstracker.infrastructure.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DailyNoteService {

    private final DailyNoteRepository dailyNoteRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<DailyNoteDTO> getNotesForDate(LocalDate date) {
        return dailyNoteRepository.findByNoteDateOrderByPinnedDescCreatedAtDesc(date)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<DailyNoteDTO> getNotesInRange(LocalDate from, LocalDate to) {
        return dailyNoteRepository.findByNoteDateBetweenOrderByNoteDateDescCreatedAtDesc(from, to)
                .stream().map(this::toDTO).toList();
    }

    public DailyNoteDTO createNote(CreateDailyNoteRequest req, String username) {
        var author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        var note = DailyNote.builder()
                .noteDate(req.getNoteDate())
                .author(author)
                .title(req.getTitle())
                .content(req.getContent())
                .category(DailyNote.Category.valueOf(req.getCategory().toUpperCase()))
                .importance(DailyNote.Importance.valueOf(req.getImportance().toUpperCase()))
                .pinned(req.isPinned())
                .build();

        var saved = dailyNoteRepository.save(note);
        auditService.record("NOTE_CREATED", "DailyNote", saved.getId(), author,
                "Note created for date: " + req.getNoteDate());
        return toDTO(saved);
    }

    public void deleteNote(Long id, String username) {
        if (!dailyNoteRepository.existsById(id)) {
            throw new ResourceNotFoundException("DailyNote", id);
        }
        var user = userRepository.findByUsername(username).orElse(null);
        dailyNoteRepository.deleteById(id);
        auditService.record("NOTE_DELETED", "DailyNote", id, user, "Note deleted");
    }

    private DailyNoteDTO toDTO(DailyNote n) {
        return DailyNoteDTO.builder()
                .id(n.getId())
                .noteDate(n.getNoteDate())
                .authorId(n.getAuthor().getId())
                .authorUsername(n.getAuthor().getUsername())
                .title(n.getTitle())
                .content(n.getContent())
                .category(n.getCategory().name())
                .importance(n.getImportance().name())
                .pinned(n.isPinned())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}
