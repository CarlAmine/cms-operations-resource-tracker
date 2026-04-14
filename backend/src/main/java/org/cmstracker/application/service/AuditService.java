package org.cmstracker.application.service;

import lombok.RequiredArgsConstructor;
import org.cmstracker.application.dto.AuditEventDTO;
import org.cmstracker.domain.model.AuditEvent;
import org.cmstracker.domain.model.User;
import org.cmstracker.domain.repository.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    /**
     * Records an audit event. Uses a new transaction so it persists even if the
     * calling transaction rolls back (best-effort audit trail).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String eventType, String entityType, Long entityId, User performedBy, String description) {
        var event = AuditEvent.builder()
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .performedBy(performedBy)
                .description(description)
                .occurredAt(Instant.now())
                .build();
        auditEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Page<AuditEventDTO> getAll(Pageable pageable) {
        return auditEventRepository.findAllByOrderByOccurredAtDesc(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<AuditEventDTO> getForEntity(String entityType, Long entityId, Pageable pageable) {
        return auditEventRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable).map(this::toDTO);
    }

    private AuditEventDTO toDTO(AuditEvent e) {
        return AuditEventDTO.builder()
                .id(e.getId())
                .eventType(e.getEventType())
                .entityType(e.getEntityType())
                .entityId(e.getEntityId())
                .performedByUsername(e.getPerformedBy() != null ? e.getPerformedBy().getUsername() : "system")
                .description(e.getDescription())
                .occurredAt(e.getOccurredAt())
                .build();
    }
}
