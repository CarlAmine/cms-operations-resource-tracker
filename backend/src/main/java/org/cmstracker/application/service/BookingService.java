package org.cmstracker.application.service;

import lombok.RequiredArgsConstructor;
import org.cmstracker.application.dto.*;
import org.cmstracker.domain.model.*;
import org.cmstracker.domain.repository.*;
import org.cmstracker.infrastructure.exception.ConflictException;
import org.cmstracker.infrastructure.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<BookingDTO> getAllBookings() {
        return bookingRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public BookingDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    public BookingDTO createBooking(CreateBookingRequest req, String username) {
        var resource = resourceRepository.findById(req.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource", req.getResourceId()));
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        validateNoConflict(req.getResourceId(), req.getStartTime(), req.getEndTime(), null);
        validateNoActiveMaintenance(req.getResourceId(), req.getStartTime(), req.getEndTime());

        if (resource.getStatus() == Resource.Status.UNAVAILABLE) {
            throw new ConflictException("Resource '" + resource.getName() + "' is currently unavailable");
        }

        var booking = Booking.builder()
                .resource(resource)
                .bookedBy(user)
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .purpose(req.getPurpose())
                .status(Booking.Status.PENDING)
                .build();

        var saved = bookingRepository.save(booking);
        auditService.record("BOOKING_CREATED", "Booking", saved.getId(),
                user, "Booking created for resource: " + resource.getName());
        return toDTO(saved);
    }

    public BookingDTO approveBooking(Long id, String approverUsername) {
        var booking = findOrThrow(id);
        var approver = userRepository.findByUsername(approverUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User", approverUsername));

        if (booking.getStatus() != Booking.Status.PENDING) {
            throw new ConflictException("Only PENDING bookings can be approved");
        }

        booking.setStatus(Booking.Status.APPROVED);
        booking.setApprovedBy(approver);
        booking.setApprovedAt(Instant.now());

        auditService.record("BOOKING_APPROVED", "Booking", id, approver,
                "Booking approved by " + approverUsername);
        return toDTO(bookingRepository.save(booking));
    }

    public BookingDTO cancelBooking(Long id, String reason, String username) {
        var booking = findOrThrow(id);
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));

        if (booking.getStatus() == Booking.Status.CANCELLED) {
            throw new ConflictException("Booking is already cancelled");
        }

        booking.setStatus(Booking.Status.CANCELLED);
        booking.setCancellationReason(reason);

        auditService.record("BOOKING_CANCELLED", "Booking", id, user,
                "Booking cancelled. Reason: " + reason);
        return toDTO(bookingRepository.save(booking));
    }

    private void validateNoConflict(Long resourceId, Instant start, Instant end, Long excludeId) {
        List<Booking> conflicts = excludeId == null
                ? bookingRepository.findConflictingBookings(resourceId, start, end)
                : bookingRepository.findConflictingBookingsExcluding(resourceId, start, end, excludeId);

        if (!conflicts.isEmpty()) {
            throw new ConflictException("Resource is already booked during the requested time window");
        }
    }

    private void validateNoActiveMaintenance(Long resourceId, Instant start, Instant end) {
        var maintenance = maintenanceRecordRepository.findActiveMaintenanceOverlapping(resourceId, start, end);
        if (!maintenance.isEmpty()) {
            throw new ConflictException("Resource is scheduled for maintenance during the requested time window");
        }
    }

    private Booking findOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }

    private BookingDTO toDTO(Booking b) {
        return BookingDTO.builder()
                .id(b.getId())
                .resourceId(b.getResource().getId())
                .resourceName(b.getResource().getName())
                .bookedById(b.getBookedBy().getId())
                .bookedByUsername(b.getBookedBy().getUsername())
                .startTime(b.getStartTime())
                .endTime(b.getEndTime())
                .purpose(b.getPurpose())
                .status(b.getStatus().name())
                .approvedById(b.getApprovedBy() != null ? b.getApprovedBy().getId() : null)
                .approvedByUsername(b.getApprovedBy() != null ? b.getApprovedBy().getUsername() : null)
                .approvedAt(b.getApprovedAt())
                .cancellationReason(b.getCancellationReason())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
