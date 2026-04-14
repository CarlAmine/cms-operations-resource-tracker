package org.cmstracker.domain.repository;

import org.cmstracker.domain.model.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {

    List<MaintenanceRecord> findByResource_IdOrderByScheduledStartDesc(Long resourceId);

    /**
     * Check if a resource has active maintenance that overlaps a time range.
     */
    @Query("""
            SELECT m FROM MaintenanceRecord m
            WHERE m.resource.id = :resourceId
              AND m.status IN ('PLANNED', 'IN_PROGRESS')
              AND m.scheduledStart < :endTime
              AND (m.scheduledEnd IS NULL OR m.scheduledEnd > :startTime)
            """)
    List<MaintenanceRecord> findActiveMaintenanceOverlapping(
            @Param("resourceId") Long resourceId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    List<MaintenanceRecord> findByStatusIn(List<MaintenanceRecord.MaintenanceStatus> statuses);
}
