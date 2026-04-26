package com.faculty.ems.repository;

import com.faculty.ems.model.SocietyAdminRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocietyAdminRequestRepository extends JpaRepository<SocietyAdminRequest, Integer> {
    
    List<SocietyAdminRequest> findByStatus(SocietyAdminRequest.RequestStatus status);

    List<SocietyAdminRequest> findByStatusOrderByCreatedAtDesc(SocietyAdminRequest.RequestStatus status);
    
    List<SocietyAdminRequest> findByUserId(Integer userId);
    
    Optional<SocietyAdminRequest> findByUserIdAndStatus(Integer userId, SocietyAdminRequest.RequestStatus status);

    // Check if there's already a pending request for the same user and society
    boolean existsByUserIdAndCreatedSocietyIdAndStatus(Integer userId,
                                                       Integer createdSocietyId,
                                                       SocietyAdminRequest.RequestStatus status);
    
    List<SocietyAdminRequest> findAllByOrderByCreatedAtDesc();

    //load the notification whitch are relevant to the each user
    @Query("""
        SELECT r FROM SocietyAdminRequest r
        WHERE r.user.id = :userId AND r.status <> :status
        ORDER BY r.reviewedAt DESC, r.createdAt DESC
    """)
    List<SocietyAdminRequest> findReviewedByUser(@Param("userId") Integer userId,
                                                 @Param("status") SocietyAdminRequest.RequestStatus status);


}
