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
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final LocationRepository locationRepository;
    private final AuditService auditService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ResourceDTO> getAllResources(Boolean activeOnly) {
        return resourceRepository.findAll().stream()
                .filter(r -> activeOnly == null || r.isActive() == activeOnly)
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResourceDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    public ResourceDTO createResource(CreateResourceRequest req, String username) {
        Location location = null;
        if (req.getLocationId() != null) {
            location = locationRepository.findById(req.getLocationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Location", req.getLocationId()));
        }

        var resource = Resource.builder()
                .name(req.getName())
                .description(req.getDescription())
                .category(Resource.Category.valueOf(req.getCategory().toUpperCase()))
                .status(Resource.Status.AVAILABLE)
                .location(location)
                .notes(req.getNotes())
                .build();

        var saved = resourceRepository.save(resource);
        var user = userRepository.findByUsername(username).orElse(null);
        auditService.record("RESOURCE_CREATED", "Resource", saved.getId(), user,
                "Resource created: " + saved.getName());
        return toDTO(saved);
    }

    public ResourceDTO updateStatus(Long id, String status, String username) {
        var resource = findOrThrow(id);
        var previousStatus = resource.getStatus().name();
        resource.setStatus(Resource.Status.valueOf(status.toUpperCase()));
        var saved = resourceRepository.save(resource);
        var user = userRepository.findByUsername(username).orElse(null);
        auditService.record("RESOURCE_STATUS_CHANGED", "Resource", id, user,
                "Status changed from " + previousStatus + " to " + status);
        return toDTO(saved);
    }

    public void deleteResource(Long id, String username) {
        var resource = findOrThrow(id);
        resource.setActive(false);
        resourceRepository.save(resource);
        var user = userRepository.findByUsername(username).orElse(null);
        auditService.record("RESOURCE_DELETED", "Resource", id, user,
                "Resource soft-deleted: " + resource.getName());
    }

    private Resource findOrThrow(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", id));
    }

    private ResourceDTO toDTO(Resource r) {
        return ResourceDTO.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .category(r.getCategory().name())
                .status(r.getStatus().name())
                .locationId(r.getLocation() != null ? r.getLocation().getId() : null)
                .locationName(r.getLocation() != null ? r.getLocation().getName() : null)
                .notes(r.getNotes())
                .active(r.isActive())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
