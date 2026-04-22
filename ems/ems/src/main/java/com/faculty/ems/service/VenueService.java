package com.faculty.ems.service;

import com.faculty.ems.model.Venue;
import com.faculty.ems.model.VenueBooking;
import com.faculty.ems.repository.VenueRepository;
import com.faculty.ems.repository.VenueBookingRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class VenueService {
    
    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private VenueBookingRepository venueBookingRepository;

    public List<Venue> getAllVenues(){
        return venueRepository.findAll();
    }

    public List<Venue> getActiveVenues(){
        return venueRepository.findByActiveTrue();
    }

    public void saveVenue(Venue venue) {
        venueRepository.save(venue);
    }

    public Venue getVenueById(Long id) {
        Optional<Venue> optionalVenue = venueRepository.findById(id);
        return optionalVenue.orElse(null);
    }

    public void editVenue(Venue venue) {
        venueRepository.save(venue);
    }

    public void deleteVenue(Long id) {
        
        // Check if venue has any bookings
        List<VenueBooking> bookings = venueBookingRepository.findByVenueId(id);
        if (!bookings.isEmpty()) {
            // Build detailed error message with booking details
            StringBuilder message = new StringBuilder();
            message.append("Venue cannot be deleted because it has the following bookings:<br/><br/>");
            
            for (VenueBooking booking : bookings) {
                message.append("• Event: ").append(booking.getEvent().getTitle()).append("<br/>");
                message.append("&nbsp;&nbsp;Date: ").append(booking.getBookingDate()).append("<br/>");
                message.append("&nbsp;&nbsp;Time: ").append(booking.getStartTime()).append(" - ").append(booking.getEndTime()).append("<br/>");
                message.append("&nbsp;&nbsp;Status: ").append(booking.getStatus()).append("<br/>");
                message.append("<br/>");
            }
            
            message.append("Please cancel all bookings before deleting this venue.");
            throw new IllegalArgumentException(message.toString());
        }
        
        if (!venueRepository.existsById(id)) {
            throw new EntityNotFoundException("Venue not found with id: " + id);
        }
        venueRepository.deleteById(id);
    }

   


    

}
