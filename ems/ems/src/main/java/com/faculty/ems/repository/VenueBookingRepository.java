package com.faculty.ems.repository;

import com.faculty.ems.model.Status;
import com.faculty.ems.model.VenueBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenueBookingRepository extends JpaRepository<VenueBooking, Long> {

    List<VenueBooking> findByStatus(Status status);

    List<VenueBooking> findByRequestedBy(Long userId);
}
