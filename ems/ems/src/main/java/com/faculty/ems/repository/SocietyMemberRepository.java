package com.faculty.ems.repository;

import com.faculty.ems.model.SocietyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocietyMemberRepository extends JpaRepository<SocietyMember, Integer> {
    // Find all members of a specific society
    List<SocietyMember> findBySocietyId(Integer societyId);

    // Check if a user is already a member (to prevent duplicates)
    boolean existsBySocietyIdAndUserId(Integer societyId, Integer userId);

    // Find a specific member by society and user ID
    SocietyMember findBySocietyIdAndUserId(Integer societyId, Integer userId);

    List<SocietyMember> findByUserId(Integer userId);
}
