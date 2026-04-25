package com.faculty.ems.service;

import com.faculty.ems.model.Event;
import com.faculty.ems.model.VenueBooking;
import com.faculty.ems.repository.EventRepository;
import com.faculty.ems.repository.VenueBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepo;
    private final VenueBookingRepository bookingRepo;

    public List<Event> findAll() {
        return eventRepo.findAll();
    }

    public List<Event> findBySociety(long societyId) {
        return eventRepo.findBySocietyId(societyId);
    }

    public Event findById(long id) {
        return eventRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found: " + id));
    }

    public Event save(Event event) {
        if (event == null) throw new IllegalArgumentException("Event cannot be null");
        return eventRepo.save(event);
    }

    public Event update(long id, Event updated) {
        Event existing = findById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setEventType(updated.getEventType());
        existing.setExpectedAttendees(updated.getExpectedAttendees());
        existing.setStatus(updated.getStatus());
        return eventRepo.save(existing);
    }

    
    public void deleteIfNoApprovedBooking(long eventId) {
        List<VenueBooking> approved = bookingRepo
            .findBySocietyIdOrderByBookingDateDesc(eventId) 
            .stream()
            .filter(b -> b.getEvent().getId().equals(eventId))
            .filter(b -> b.getStatus() == VenueBooking.BookingStatus.APPROVED)
            .toList();

        if (!approved.isEmpty()) {
            throw new RuntimeException(
                "Cannot delete event: it has an approved venue booking. Cancel the booking first."
            );
        }
        eventRepo.deleteById(eventId);
    }
}