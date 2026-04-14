package org.cmstracker.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data @Builder @JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String status;
    private Long locationId;
    private String locationName;
    private String notes;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
