package com.faculty.ems.service;

import com.faculty.ems.exception.BookingConflictException;
import com.faculty.ems.model.Event;
import com.faculty.ems.model.VenueBooking;
import com.faculty.ems.repository.EventRepository;
import com.faculty.ems.repository.VenueBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueBookingService {

    private final VenueBookingRepository bookingRepo;
    private final EventRepository eventRepo;
    private final EmailService emailService;

    
    public VenueBooking requestBooking(VenueBooking booking) {
        Long eventId = booking.getEvent().getId();

        // Only block re-booking when there is an ACTIVE booking flow for the event.
        // If a booking was postponed/rejected/cancelled, we allow a fresh request.
        boolean hasActiveBookingForEvent = bookingRepo.existsByEventIdAndStatusIn(
                eventId,
                EnumSet.of(VenueBooking.BookingStatus.PENDING, VenueBooking.BookingStatus.APPROVED)
        );

        if (hasActiveBookingForEvent) {
            VenueBooking existing = bookingRepo.findByEventIdOrderByBookingDateDesc(eventId)
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                throw new BookingConflictException(
                        "A venue booking already exists for event '" + booking.getEvent().getTitle() +
                                "' at '" + existing.getVenue().getName() + "' on " + existing.getBookingDate() +
                                " (Status: " + existing.getStatus() + "). You cannot book the same venue again."
                );
            }

            throw new BookingConflictException(
                    "A venue booking already exists for event '" + booking.getEvent().getTitle() +
                            "'. You cannot book the same venue again."
            );
        }

        List<VenueBooking> conflicts = bookingRepo.findConflicting(
            booking.getVenue().getId(),
            booking.getBookingDate(),
            booking.getStartTime(),
            booking.getEndTime()
        );

        //conflict check: Ensure no other APPROVED booking overlaps
        if (!conflicts.isEmpty()) {
            VenueBooking clash = conflicts.get(0);
            throw new BookingConflictException(
                "'" + clash.getVenue().getName() + "' is already booked on " +
                clash.getBookingDate() + " from " +
                clash.getStartTime() + " to " + clash.getEndTime() +
                " for event '" + clash.getEvent().getTitle() + "'" +
                " (Status: " + clash.getStatus() + ")."
            );
        }

        booking.setStatus(VenueBooking.BookingStatus.PENDING);
        return bookingRepo.save(booking);
    }

    public List<VenueBooking> getBookingsBySociety(long societyId) {
        return bookingRepo.findBySocietyIdOrderByBookingDateDesc(societyId);
    }

    public VenueBooking findById(long id) {
        return bookingRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found: " + id));
    }

    
    public void cancelBooking(long bookingId) {
        VenueBooking booking = findById(bookingId);
        if (booking.getStatus() != VenueBooking.BookingStatus.PENDING) {
            throw new RuntimeException("Only PENDING bookings can be cancelled.");
        }
        booking.setStatus(VenueBooking.BookingStatus.CANCELLED);
        bookingRepo.save(booking);
    }

    public void approveBooking(long id, String note) {
        VenueBooking booking = findById(id);
        if (booking.getStatus() != VenueBooking.BookingStatus.PENDING) {
            throw new RuntimeException("Only pending bookings can be approved.");
        }

        // Conflict check: Ensure no other APPROVED booking overlaps
        List<VenueBooking> conflicts = bookingRepo.findConflictingExcluding(
                booking.getVenue().getId(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getId()
        );

        if (!conflicts.isEmpty()) {
            VenueBooking clash = conflicts.get(0);
            throw new BookingConflictException(
                    "Cannot approve! '" + clash.getVenue().getName() + "' is already booked on " +
                            clash.getBookingDate() + " from " +
                            clash.getStartTime() + " to " + clash.getEndTime() +
                            " for event '" + clash.getEvent().getTitle() + "'" +
                            " (Status: " + clash.getStatus() + ")."
            );
        }

        booking.setStatus(VenueBooking.BookingStatus.APPROVED);
        booking.setAdminNote(note);

        // Keep event lifecycle aligned with approved venue booking.
        Event event = booking.getEvent();
        if (event.getStatus() != Event.EventStatus.PUBLISHED) {
            event.setStatus(Event.EventStatus.PUBLISHED);
            eventRepo.save(event);
        }

        VenueBooking saved = bookingRepo.save(booking);
        emailService.sendBookingStatusEmail(saved);
    }

    public void rejectBooking(long id, String reason) {
        VenueBooking booking = findById(id);
        if (booking.getStatus() != VenueBooking.BookingStatus.PENDING) {
            throw new RuntimeException("Only pending bookings can be rejected.");
        }
        booking.setStatus(VenueBooking.BookingStatus.REJECTED);
        booking.setAdminNote(reason);
        VenueBooking saved = bookingRepo.save(booking);
        emailService.sendBookingStatusEmail(saved);
    }

    public void postponeApprovedBooking(long id, String note) {
        VenueBooking booking = findById(id);
        if (booking.getStatus() != VenueBooking.BookingStatus.APPROVED) {
            throw new RuntimeException("Only approved bookings can be postponed.");
        }

        // Postponement is represented as a rejected booking with a postponed event.
        // The rejected booking allows society admin to request a new booking later.
        booking.setStatus(VenueBooking.BookingStatus.REJECTED);
        booking.setAdminNote(note);

        Event event = booking.getEvent();
        event.setStatus(Event.EventStatus.POSTPONED);
        eventRepo.save(event);

        VenueBooking saved = bookingRepo.save(booking);
        emailService.sendBookingStatusEmail(saved);
    }
}