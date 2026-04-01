package com.eventmanagement.service;

import com.eventmanagement.model.Event;
import com.eventmanagement.model.EventStatus;
import com.eventmanagement.model.User;
import com.eventmanagement.model.Venue;
import com.eventmanagement.repository.EventRepository;
import com.eventmanagement.repository.VenueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    public Event createEvent(Event event) {
        List<Event> conflicts = eventRepository.findConflictingEvents(
            event.getVenue(),
            event.getStartDateTime(),
            event.getEndDateTime(),
            event.getId() == null ? -1L : event.getId()
        );

        if (!conflicts.isEmpty()) {
            event.setStatus(EventStatus.CONFLICT);
            StringBuilder note = new StringBuilder("Booking conflict detected with: ");
            for (Event conflict : conflicts) {
                note.append(conflict.getTitle())
                    .append(" (")
                    .append(conflict.getStartDateTime())
                    .append(" - ")
                    .append(conflict.getEndDateTime())
                    .append("), ");
            }
            event.setConflictNote(note.toString());
        }

        return eventRepository.save(event);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public List<Event> getEventsByOrganizer(User organizer) {
        return eventRepository.findByOrganizer(organizer);
    }

    public List<Event> getEventsByVenue(Venue venue) {
        return eventRepository.findByVenue(venue);
    }

    public List<Event> getPendingEvents() {
        return eventRepository.findByStatus(EventStatus.PENDING);
    }

    public List<Event> getConflictEvents() {
        return eventRepository.findByStatus(EventStatus.CONFLICT);
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    public Event approveEvent(Long id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
        event.setStatus(EventStatus.APPROVED);
        return eventRepository.save(event);
    }

    public Event rejectEvent(Long id, String reason) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
        event.setStatus(EventStatus.REJECTED);
        if (reason != null && !reason.isBlank()) {
            event.setConflictNote(reason);
        }
        return eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    public List<Event> checkConflicts(Venue venue, Event excludeEvent) {
        return eventRepository.findConflictingEvents(
            venue,
            excludeEvent.getStartDateTime(),
            excludeEvent.getEndDateTime(),
            excludeEvent.getId() == null ? -1L : excludeEvent.getId()
        );
    }
}
