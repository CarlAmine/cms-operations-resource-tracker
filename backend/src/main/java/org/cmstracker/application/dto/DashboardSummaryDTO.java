package org.cmstracker.application.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data @Builder
public class DashboardSummaryDTO {
    private long totalResources;
    private long availableResources;
    private long resourcesUnderMaintenance;
    private long activeBookingsToday;
    private long pendingBookings;
    private List<BookingDTO> todaysBookings;
    private List<DailyNoteDTO> todaysNotes;
    private List<MaintenanceRecordDTO> activeMaintenances;
    private Map<String, Long> resourcesByStatus;
    private Map<String, Long> resourcesByCategory;
}
