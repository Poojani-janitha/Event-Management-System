package com.eventmanagement.repository;

import com.eventmanagement.model.Event;
import com.eventmanagement.model.EventStatus;
import com.eventmanagement.model.User;
import com.eventmanagement.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizer(User organizer);
    List<Event> findByVenue(Venue venue);
    List<Event> findByStatus(EventStatus status);

    @Query("SELECT e FROM Event e WHERE e.venue = :venue " +
           "AND e.status IN (com.eventmanagement.model.EventStatus.APPROVED, com.eventmanagement.model.EventStatus.PENDING) " +
           "AND e.id <> :excludeId " +
           "AND ((e.startDateTime < :endDateTime AND e.endDateTime > :startDateTime))")
    List<Event> findConflictingEvents(
        @Param("venue") Venue venue,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("endDateTime") LocalDateTime endDateTime,
        @Param("excludeId") Long excludeId
    );
}
