package com.faculty.ems.service;

import com.faculty.ems.model.Venue;
import com.faculty.ems.repository.VenueRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class VenueService {
    
    @Autowired
    private VenueRepository venueRepository;

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
        if (!venueRepository.existsById(id)) {
            throw new EntityNotFoundException("Venue not found with id: " + id);
        }
        venueRepository.deleteById(id);
    }

   


    

}
