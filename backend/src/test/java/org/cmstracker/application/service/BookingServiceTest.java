package org.cmstracker.application.service;

import org.cmstracker.domain.model.Booking;
import org.cmstracker.domain.model.MaintenanceRecord;
import org.cmstracker.domain.model.Resource;
import org.cmstracker.domain.model.User;
import org.cmstracker.domain.repository.BookingRepository;
import org.cmstracker.domain.repository.MaintenanceRecordRepository;
import org.cmstracker.domain.repository.ResourceRepository;
import org.cmstracker.domain.repository.UserRepository;
import org.cmstracker.infrastructure.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Tests")
class BookingServiceTest {

    @Mock BookingRepository bookingRepository;
    @Mock ResourceRepository resourceRepository;
    @Mock UserRepository userRepository;
    @Mock MaintenanceRecordRepository maintenanceRecordRepository;
    @Mock AuditService auditService;

    @InjectMocks BookingService bookingService;

    private Resource mockResource;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockResource = Resource.builder()
                .id(1L).name("Test Module")
                .category(Resource.Category.DETECTOR)
                .status(Resource.Status.AVAILABLE)
                .active(true).build();

        mockUser = User.builder()
                .id(1L).username("operator1")
                .role(User.Role.OPERATOR)
                .enabled(true).build();
    }

    @Test
    @DisplayName("createBooking: succeeds when no conflict and resource is available")
    void createBooking_noConflict_succeeds() {
        var req = new org.cmstracker.application.dto.CreateBookingRequest();
        req.setResourceId(1L);
        req.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        req.setEndTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));
        req.setPurpose("Test run");

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(mockResource));
        when(userRepository.findByUsername("operator1")).thenReturn(Optional.of(mockUser));
        when(bookingRepository.findConflictingBookings(anyLong(), any(), any())).thenReturn(List.of());
        when(maintenanceRecordRepository.findActiveMaintenanceOverlapping(anyLong(), any(), any())).thenReturn(List.of());

        var savedBooking = Booking.builder()
                .id(10L).resource(mockResource).bookedBy(mockUser)
                .startTime(req.getStartTime()).endTime(req.getEndTime())
                .status(Booking.Status.PENDING).build();
        when(bookingRepository.save(any())).thenReturn(savedBooking);

        var result = bookingService.createBooking(req, "operator1");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("PENDING");
        verify(auditService).record(eq("BOOKING_CREATED"), eq("Booking"), anyLong(), any(), anyString());
    }

    @Test
    @DisplayName("createBooking: throws ConflictException when time slot is already booked")
    void createBooking_withConflict_throwsConflictException() {
        var req = new org.cmstracker.application.dto.CreateBookingRequest();
        req.setResourceId(1L);
        req.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        req.setEndTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(mockResource));
        when(userRepository.findByUsername("operator1")).thenReturn(Optional.of(mockUser));

        var conflicting = Booking.builder().id(99L).status(Booking.Status.APPROVED).build();
        when(bookingRepository.findConflictingBookings(anyLong(), any(), any())).thenReturn(List.of(conflicting));

        assertThatThrownBy(() -> bookingService.createBooking(req, "operator1"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already booked");
        verifyNoInteractions(auditService);
    }

    @Test
    @DisplayName("createBooking: throws ConflictException when resource is under maintenance")
    void createBooking_durignMaintenance_throwsConflictException() {
        var req = new org.cmstracker.application.dto.CreateBookingRequest();
        req.setResourceId(1L);
        req.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        req.setEndTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(mockResource));
        when(userRepository.findByUsername("operator1")).thenReturn(Optional.of(mockUser));
        when(bookingRepository.findConflictingBookings(anyLong(), any(), any())).thenReturn(List.of());

        var maintenanceRecord = MaintenanceRecord.builder()
                .id(5L).status(MaintenanceRecord.MaintenanceStatus.PLANNED).build();
        when(maintenanceRecordRepository.findActiveMaintenanceOverlapping(anyLong(), any(), any()))
                .thenReturn(List.of(maintenanceRecord));

        assertThatThrownBy(() -> bookingService.createBooking(req, "operator1"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("maintenance");
    }

    @Test
    @DisplayName("createBooking: throws ConflictException for UNAVAILABLE resource")
    void createBooking_unavailableResource_throwsConflictException() {
        mockResource.setStatus(Resource.Status.UNAVAILABLE);
        var req = new org.cmstracker.application.dto.CreateBookingRequest();
        req.setResourceId(1L);
        req.setStartTime(Instant.now().plus(1, ChronoUnit.DAYS));
        req.setEndTime(Instant.now().plus(1, ChronoUnit.DAYS).plus(4, ChronoUnit.HOURS));

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(mockResource));
        when(userRepository.findByUsername("operator1")).thenReturn(Optional.of(mockUser));
        when(bookingRepository.findConflictingBookings(anyLong(), any(), any())).thenReturn(List.of());
        when(maintenanceRecordRepository.findActiveMaintenanceOverlapping(anyLong(), any(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> bookingService.createBooking(req, "operator1"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("unavailable");
    }

    @Test
    @DisplayName("approveBooking: transitions status from PENDING to APPROVED")
    void approveBooking_pendingBooking_becomesApproved() {
        var booking = Booking.builder().id(1L).resource(mockResource).bookedBy(mockUser)
                .status(Booking.Status.PENDING).build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findByUsername("coordinator1")).thenReturn(Optional.of(
                User.builder().id(2L).username("coordinator1").role(User.Role.COORDINATOR).build()));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = bookingService.approveBooking(1L, "coordinator1");

        assertThat(result.getStatus()).isEqualTo("APPROVED");
        assertThat(result.getApprovedByUsername()).isEqualTo("coordinator1");
    }

    @Test
    @DisplayName("cancelBooking: sets status to CANCELLED with reason")
    void cancelBooking_activeBooking_cancelled() {
        var booking = Booking.builder().id(1L).resource(mockResource).bookedBy(mockUser)
                .status(Booking.Status.APPROVED).build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findByUsername("operator1")).thenReturn(Optional.of(mockUser));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = bookingService.cancelBooking(1L, "No longer needed", "operator1");

        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        assertThat(result.getCancellationReason()).isEqualTo("No longer needed");
    }
}
