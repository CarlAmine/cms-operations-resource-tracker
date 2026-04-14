package org.cmstracker.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintenanceRecordDTO {
    private Long id;
    private Long resourceId;
    private String resourceName;
    private Long reportedById;
    private String reportedByUsername;
    private String title;
    private String description;
    private String type;
    private Instant scheduledStart;
    private Instant scheduledEnd;
    private Instant actualEnd;
    private String status;
    private String resolution;
    private Instant createdAt;
}
