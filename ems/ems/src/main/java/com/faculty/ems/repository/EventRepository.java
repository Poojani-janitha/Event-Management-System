package com.faculty.ems.repository;

import com.faculty.ems.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findBySocietyId(Long societyId);

    List<Event> findBySocietyIdIn(List<Long> societyIds);

    List<Event> findBySocietyIdAndStatus(Long societyId, Event.EventStatus status);
}