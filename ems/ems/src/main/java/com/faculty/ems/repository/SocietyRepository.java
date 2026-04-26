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
    Optional<Society> findBySocietyAdminId(Integer adminId);
    List<Society> findAllBySocietyAdminId(Integer adminId);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByContactEmailIgnoreCase(String contactEmail);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);

    boolean existsByContactEmailIgnoreCaseAndIdNot(String contactEmail, Integer id);
}
