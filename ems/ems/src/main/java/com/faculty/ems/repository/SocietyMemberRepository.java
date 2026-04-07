package com.faculty.ems.repository;

import com.faculty.ems.model.SocietyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocietyMemberRepository extends JpaRepository<SocietyMember, Long> {
    // Find all members of a specific society
//    List<SocietyMember> findBySocietyId(Long societyId);

    // Check if a user is already a member (to prevent duplicates)
    boolean existsBySocietyIdAndUserId(Long societyId, Long userId);
}
