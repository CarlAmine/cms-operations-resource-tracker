package org.cmstracker.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.cmstracker.application.dto.*;
import org.cmstracker.application.service.MaintenanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
@Tag(name = "Maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @GetMapping
    @Operation(summary = "List all maintenance records")
    public ResponseEntity<List<MaintenanceRecordDTO>> getAll() {
        return ResponseEntity.ok(maintenanceService.getAll());
    }

    @GetMapping("/resource/{resourceId}")
    @Operation(summary = "List maintenance records for a resource")
    public ResponseEntity<List<MaintenanceRecordDTO>> getForResource(@PathVariable Long resourceId) {
        return ResponseEntity.ok(maintenanceService.getForResource(resourceId));
    }

    @PostMapping("/resource/{resourceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Create a maintenance record for a resource")
    public ResponseEntity<MaintenanceRecordDTO> create(
            @PathVariable Long resourceId,
            @Valid @RequestBody CreateMaintenanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(maintenanceService.createRecord(resourceId, request, userDetails.getUsername()));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    @Operation(summary = "Mark maintenance record as completed")
    public ResponseEntity<MaintenanceRecordDTO> complete(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                maintenanceService.completeRecord(id, body.getOrDefault("resolution", ""), userDetails.getUsername()));
    }
}
