package org.cmstracker.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateResourceRequest {
    @NotBlank @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotBlank
    private String category;

    private Long locationId;

    @Size(max = 1000)
    private String notes;
}
