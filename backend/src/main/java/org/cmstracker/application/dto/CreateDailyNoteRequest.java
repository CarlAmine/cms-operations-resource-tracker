package org.cmstracker.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateDailyNoteRequest {
    @NotNull
    private LocalDate noteDate;

    @NotBlank @Size(max = 200)
    private String title;

    @NotBlank @Size(max = 5000)
    private String content;

    private String category = "GENERAL";
    private String importance = "NORMAL";
    private boolean pinned = false;
}
