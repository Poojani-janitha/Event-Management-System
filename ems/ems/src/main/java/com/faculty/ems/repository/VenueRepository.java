package com.faculty.ems.repository;

import com.faculty.ems.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findByActiveTrue();
    
    
}
