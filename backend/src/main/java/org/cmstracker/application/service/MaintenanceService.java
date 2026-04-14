package org.cmstracker.application.service;

import lombok.RequiredArgsConstructor;
import org.cmstracker.application.dto.*;
import org.cmstracker.domain.model.*;
import org.cmstracker.domain.repository.*;
import org.cmstracker.infrastructure.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MaintenanceService {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<MaintenanceRecordDTO> getAll() {
        return maintenanceRecordRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRecordDTO> getForResource(Long resourceId) {
        return maintenanceRecordRepository.findByResource_IdOrderByScheduledStartDesc(resourceId)
                .stream().map(this::toDTO).toList();
    }

    public MaintenanceRecordDTO createRecord(Long resourceId, CreateMaintenanceRequest req, String username) {
        var resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", resourceId));
        var reporter = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        // Set resource status to MAINTENANCE
        resource.setStatus(Resource.Status.MAINTENANCE);
        resourceRepository.save(resource);

        var record = MaintenanceRecord.builder()
                .resource(resource)
                .reportedBy(reporter)
                .title(req.getTitle())
                .description(req.getDescription())
                .type(MaintenanceRecord.MaintenanceType.valueOf(req.getType().toUpperCase()))
                .scheduledStart(req.getScheduledStart())
                .scheduledEnd(req.getScheduledEnd())
                .status(MaintenanceRecord.MaintenanceStatus.PLANNED)
                .build();

        var saved = maintenanceRecordRepository.save(record);
        auditService.record("MAINTENANCE_CREATED", "MaintenanceRecord", saved.getId(), reporter,
                "Maintenance scheduled for resource: " + resource.getName());
        return toDTO(saved);
    }

    public MaintenanceRecordDTO completeRecord(Long id, String resolution, String username) {
        var record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRecord", id));
        var user = userRepository.findByUsername(username).orElse(null);

        record.setStatus(MaintenanceRecord.MaintenanceStatus.COMPLETED);
        record.setResolution(resolution);
        record.setActualEnd(java.time.Instant.now());

        // Restore resource to AVAILABLE
        var resource = record.getResource();
        resource.setStatus(Resource.Status.AVAILABLE);
        resourceRepository.save(resource);

        auditService.record("MAINTENANCE_COMPLETED", "MaintenanceRecord", id, user,
                "Maintenance completed for resource: " + resource.getName());
        return toDTO(maintenanceRecordRepository.save(record));
    }

    private MaintenanceRecordDTO toDTO(MaintenanceRecord m) {
        return MaintenanceRecordDTO.builder()
                .id(m.getId())
                .resourceId(m.getResource().getId())
                .resourceName(m.getResource().getName())
                .reportedById(m.getReportedBy().getId())
                .reportedByUsername(m.getReportedBy().getUsername())
                .title(m.getTitle())
                .description(m.getDescription())
                .type(m.getType().name())
                .scheduledStart(m.getScheduledStart())
                .scheduledEnd(m.getScheduledEnd())
                .actualEnd(m.getActualEnd())
                .status(m.getStatus().name())
                .resolution(m.getResolution())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
