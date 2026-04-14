package org.cmstracker.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cmstracker.application.dto.*;
import org.cmstracker.application.service.DailyNoteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@Tag(name = "Daily Notes")
public class DailyNoteController {

    private final DailyNoteService dailyNoteService;

    @GetMapping
    @Operation(summary = "Get notes for a specific date")
    public ResponseEntity<List<DailyNoteDTO>> getForDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(
                dailyNoteService.getNotesForDate(date != null ? date : LocalDate.now()));
    }

    @GetMapping("/range")
    @Operation(summary = "Get notes within a date range")
    public ResponseEntity<List<DailyNoteDTO>> getRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(dailyNoteService.getNotesInRange(from, to));
    }

    @PostMapping
    @Operation(summary = "Create a daily note")
    public ResponseEntity<DailyNoteDTO> create(
            @Valid @RequestBody CreateDailyNoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dailyNoteService.createNote(request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a daily note")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        dailyNoteService.deleteNote(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
