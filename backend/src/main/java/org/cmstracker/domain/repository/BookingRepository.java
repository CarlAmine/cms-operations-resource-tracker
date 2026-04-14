package org.cmstracker.domain.repository;

import org.cmstracker.domain.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Detect overlapping bookings for a resource.
     * A conflict exists when an active booking's time window overlaps with [startTime, endTime].
     */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.resource.id = :resourceId
              AND b.status NOT IN ('CANCELLED', 'COMPLETED')
              AND b.startTime < :endTime
              AND b.endTime > :startTime
            """)
    List<Booking> findConflictingBookings(
            @Param("resourceId") Long resourceId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    /**
     * Same as above, but excludes a specific booking (used when updating).
     */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.resource.id = :resourceId
              AND b.id <> :excludeId
              AND b.status NOT IN ('CANCELLED', 'COMPLETED')
              AND b.startTime < :endTime
              AND b.endTime > :startTime
            """)
    List<Booking> findConflictingBookingsExcluding(
            @Param("resourceId") Long resourceId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("excludeId") Long excludeId);

    List<Booking> findByBookedBy_Id(Long userId);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.startTime >= :from AND b.startTime < :to
            ORDER BY b.startTime
            """)
    List<Booking> findBookingsInRange(
            @Param("from") Instant from,
            @Param("to") Instant to);
}
