package com.faculty.ems.repository;

import com.faculty.ems.model.VenueBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface VenueBookingRepository extends JpaRepository<VenueBooking, Long> {

    
    @Query("""
        SELECT b FROM VenueBooking b
        WHERE b.venue.id   = :venueId
          AND b.bookingDate = :bookingDate
          AND b.status NOT IN ('REJECTED', 'CANCELLED')
          AND b.startTime  < :endTime
          AND b.endTime    > :startTime
    """)
    List<VenueBooking> findConflicting(
        @Param("venueId")     Long venueId,
        @Param("bookingDate") LocalDate bookingDate,
        @Param("startTime")   LocalTime startTime,
        @Param("endTime")     LocalTime endTime
    );

    
    List<VenueBooking> findBySocietyIdOrderByBookingDateDesc(Long societyId);


    @Query("""
        SELECT b FROM VenueBooking b
        WHERE b.venue.id   = :venueId
          AND b.bookingDate = :bookingDate
          AND b.id         != :excludeId
          AND b.status NOT IN ('REJECTED', 'CANCELLED')
          AND b.startTime  < :endTime
          AND b.endTime    > :startTime
    """)
    List<VenueBooking> findConflictingExcluding(
        @Param("venueId")     Long venueId,
        @Param("bookingDate") LocalDate bookingDate,
        @Param("startTime")   LocalTime startTime,
        @Param("endTime")     LocalTime endTime,
        @Param("excludeId")   Long excludeId
    );

    List<VenueBooking> findByStatus(VenueBooking.BookingStatus status);

    List<VenueBooking> findByVenueId(Long venueId);

    List<VenueBooking> findByRequestedById(Integer userId);
}