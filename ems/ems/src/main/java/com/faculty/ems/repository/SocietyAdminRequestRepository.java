package com.faculty.ems.repository;

import com.faculty.ems.model.SocietyAdminRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocietyAdminRequestRepository extends JpaRepository<SocietyAdminRequest, Integer> {
    
    List<SocietyAdminRequest> findByStatus(SocietyAdminRequest.RequestStatus status);
    
    List<SocietyAdminRequest> findByUserId(Integer userId);
    
    Optional<SocietyAdminRequest> findByUserIdAndStatus(Integer userId, SocietyAdminRequest.RequestStatus status);
    
    List<SocietyAdminRequest> findAllByOrderByCreatedAtDesc();
}
