package org.cmstracker.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateMaintenanceRequest {
    @NotBlank @Size(max = 200)
    private String title;

    @Size(max = 2000)
    private String description;

    @NotBlank
    private String type;

    @NotNull
    private Instant scheduledStart;

    private Instant scheduledEnd;
}
