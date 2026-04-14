package org.cmstracker.application.dto;

import jakarta.validation.constraints.*;
lombok.Data;

import java.time.Instant;

@Data
public class CreateBookingRequest {
    @NotNull
    private Long resourceId;

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;

    @Size(max = 500)
    private String purpose;
}
