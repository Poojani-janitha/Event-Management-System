package com.faculty.ems.service;

import com.faculty.ems.exception.BookingConflictException;
import com.faculty.ems.model.VenueBooking;
import com.faculty.ems.repository.VenueBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueBookingService {

    private final VenueBookingRepository bookingRepo;
    private final EmailService emailService;

    
    public VenueBooking requestBooking(VenueBooking booking) {
        List<VenueBooking> conflicts = bookingRepo.findConflicting(
            booking.getVenue().getId(),
            booking.getBookingDate(),
            booking.getStartTime(),
            booking.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            VenueBooking clash = conflicts.get(0);
            throw new BookingConflictException(
                "'" + clash.getVenue().getName() + "' is already booked on " +
                clash.getBookingDate() + " from " +
                clash.getStartTime() + " to " + clash.getEndTime() +
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

        // Conflict check: Ensure no other APPROVED or PENDING booking overlaps
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
                            " (Status: " + clash.getStatus() + ")."
            );
        }

        booking.setStatus(VenueBooking.BookingStatus.APPROVED);
        booking.setAdminNote(note);
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
}