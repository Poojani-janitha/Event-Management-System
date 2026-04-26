package com.faculty.ems.repository;

import com.faculty.ems.model.VenueBooking;
import com.faculty.ems.model.VenueBooking.BookingStatus;

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
          AND b.status = 'APPROVED'
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

        List<VenueBooking> findByEventIdOrderByBookingDateDesc(Long eventId);

    boolean existsByEventId(Long eventId);

    @Query("""
        SELECT b FROM VenueBooking b
        WHERE b.venue.id   = :venueId
          AND b.bookingDate = :bookingDate
          AND b.id         != :excludeId
          AND b.status = 'APPROVED'
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


    @Query("""
        SELECT b FROM VenueBooking b
        JOIN FETCH b.event
        JOIN FETCH b.venue
        JOIN FETCH b.society
        WHERE b.venue.id = :venueId
          AND b.bookingDate BETWEEN :startDate AND :endDate
          AND b.status = :status
        ORDER BY b.bookingDate, b.startTime
    """)
    List<VenueBooking> findByVenueIdAndBookingDateBetweenAndStatus(
            @Param("venueId") Long venueId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") BookingStatus status
    );

    @Query("""
        SELECT b FROM VenueBooking b
        JOIN FETCH b.event
        JOIN FETCH b.venue
        JOIN FETCH b.society
        WHERE b.bookingDate BETWEEN :startDate AND :endDate
          AND b.status = :status
        ORDER BY b.bookingDate, b.startTime
    """)
    List<VenueBooking> findByBookingDateBetweenAndStatus(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("status") BookingStatus status
    );
}