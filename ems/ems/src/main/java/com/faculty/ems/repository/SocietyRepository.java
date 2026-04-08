package com.faculty.ems.repository;

import com.faculty.ems.model.Society;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocietyRepository extends JpaRepository<Society, Long> {
    // Find all active societies for the list view
//    List<Society> findByActiveTrue();
}
