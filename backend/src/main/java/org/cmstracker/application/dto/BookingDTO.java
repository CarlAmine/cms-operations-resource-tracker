package org.cmstracker.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingDTO {
    private Long id;
    private Long resourceId;
    private String resourceName;
    private Long bookedById;
    private String bookedByUsername;
    private Instant startTime;
    private Instant endTime;
    private String purpose;
    private String status;
    private Long approvedById;
    private String approvedByUsername;
    private Instant approvedAt;
    private String cancellationReason;
    private Instant createdAt;
    private Instant updatedAt;
}
