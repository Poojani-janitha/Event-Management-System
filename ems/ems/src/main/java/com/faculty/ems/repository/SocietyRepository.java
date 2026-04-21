package com.faculty.ems.repository;

import com.faculty.ems.model.Society;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocietyRepository extends JpaRepository<Society, Integer> {
    // Find all active societies for the list view
    List<Society> findByActiveTrue();
    
    // Find society by admin ID
    // Optional<Society> findByAdminId(Integer adminId);
    Optional<Society> findBySocietyAdmin_Id(Integer adminId);
}
