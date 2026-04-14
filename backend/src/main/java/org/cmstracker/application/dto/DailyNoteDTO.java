package org.cmstracker.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
public class DailyNoteDTO {
    private Long id;
    private LocalDate noteDate;
    private Long authorId;
    private String authorUsername;
    private String title;
    private String content;
    private String category;
    private String importance;
    private boolean pinned;
    private Instant createdAt;
    private Instant updatedAt;
}
