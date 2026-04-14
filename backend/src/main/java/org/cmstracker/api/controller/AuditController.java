package org.cmstracker.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.cmstracker.application.dto.AuditEventDTO;
import org.cmstracker.application.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit")
@PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "Get paginated audit log")
    public ResponseEntity<Page<AuditEventDTO>> getAll(
            @PageableDefault(size = 20, sort = "occurredAt") Pageable pageable) {
        return ResponseEntity.ok(auditService.getAll(pageable));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get audit log for a specific entity")
    public ResponseEntity<Page<AuditEventDTO>> getForEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(auditService.getForEntity(entityType, entityId, pageable));
    }
}
