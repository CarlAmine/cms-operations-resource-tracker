package org.cmstracker.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditEventDTO {
    private Long id;
    private String eventType;
    private String entityType;
    private Long entityId;
    private String performedByUsername;
    private String description;
    private Instant occurredAt;
}
