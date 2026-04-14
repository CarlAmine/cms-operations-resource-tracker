package org.cmstracker.application.service;

import lombok.RequiredArgsConstructor;
import org.cmstracker.application.dto.*;
import org.cmstracker.domain.model.AuditEvent;
import org.cmstracker.domain.model.Booking;
import org.cmstracker.domain.model.MaintenanceRecord;
import org.cmstracker.domain.model.Resource;
import org.cmstracker.domain.repository.*;
import org.cmstracker.infrastructure.exception.ConflictException;
import org.cmstracker.infrastructure.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DashboardService {

    private final ResourceRepository resourceRepository;
    private final BookingRepository bookingRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final DailyNoteRepository dailyNoteRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary() {
        var allResources = resourceRepository.findAll();
        var active = allResources.stream().filter(Resource::isActive).toList();

        var todayStart = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
        var todayEnd = LocalDate.now().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        var todaysBookings = bookingRepository.findBookingsInRange(todayStart, todayEnd);
        var todaysNotes = dailyNoteRepository.findByNoteDateOrderByPinnedDescCreatedAtDesc(LocalDate.now());
        var activeMaintenance = maintenanceRecordRepository.findByStatusIn(
                List.of(MaintenanceRecord.MaintenanceStatus.PLANNED, MaintenanceRecord.MaintenanceStatus.IN_PROGRESS));

        Map<String, Long> byStatus = active.stream()
                .collect(Collectors.groupingBy(r -> r.getStatus().name(), Collectors.counting()));
        Map<String, Long> byCategory = active.stream()
                .collect(Collectors.groupingBy(r -> r.getCategory().name(), Collectors.counting()));

        return DashboardSummaryDTO.builder()
                .totalResources(active.size())
                .availableResources(active.stream().filter(r -> r.getStatus() == Resource.Status.AVAILABLE).count())
                .resourcesUnderMaintenance(active.stream().filter(r -> r.getStatus() == Resource.Status.MAINTENANCE).count())
                .activeBookingsToday(todaysBookings.stream().filter(b -> b.getStatus() == Booking.Status.APPROVED).count())
                .pendingBookings(bookingRepository.findAll().stream().filter(b -> b.getStatus() == Booking.Status.PENDING).count())
                .todaysBookings(todaysBookings.stream().map(this::toBookingDTO).toList())
                .todaysNotes(todaysNotes.stream().map(this::toNoteDTO).toList())
                .activeMaintenances(activeMaintenance.stream().map(this::toMaintenanceDTO).toList())
                .resourcesByStatus(byStatus)
                .resourcesByCategory(byCategory)
                .build();
    }

    private BookingDTO toBookingDTO(Booking b) {
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
                .build();
    }

    private DailyNoteDTO toNoteDTO(org.cmstracker.domain.model.DailyNote n) {
        return DailyNoteDTO.builder()
                .id(n.getId())
                .noteDate(n.getNoteDate())
                .authorId(n.getAuthor().getId())
                .authorUsername(n.getAuthor().getUsername())
                .title(n.getTitle())
                .content(n.getContent())
                .category(n.getCategory().name())
                .importance(n.getImportance().name())
                .pinned(n.isPinned())
                .build();
    }

    private MaintenanceRecordDTO toMaintenanceDTO(MaintenanceRecord m) {
        return MaintenanceRecordDTO.builder()
                .id(m.getId())
                .resourceId(m.getResource().getId())
                .resourceName(m.getResource().getName())
                .title(m.getTitle())
                .status(m.getStatus().name())
                .scheduledStart(m.getScheduledStart())
                .scheduledEnd(m.getScheduledEnd())
                .build();
    }
}
