package com.faculty.ems.service;

import com.faculty.ems.model.RoleInSociety;
import com.faculty.ems.model.Society;
import com.faculty.ems.model.SocietyMember;
import com.faculty.ems.model.User;
import com.faculty.ems.repository.SocietyMemberRepository;
import com.faculty.ems.repository.SocietyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SocietyService {
    @Autowired private SocietyRepository societyRepo;
    @Autowired private SocietyMemberRepository memberRepo;

    public List<Society> getAllSocieties() {
        return societyRepo.findAll();
    }

    public List<Society> getSocietiesByAdminId(Integer adminId) {
        return societyRepo.findAllBySocietyAdminId(adminId);
    }

    public List<Society> getSocietiesByMemberId(Integer userId) {
        return memberRepo.findByUserId(userId)
                .stream()
                .map(SocietyMember::getSociety)
                .distinct()
                .toList();
    }

    public Society saveSociety(Society society) {
        if (society.getSocietyAdmin() == null) {
            throw new IllegalArgumentException("Society admin is required");
        }

        if (societyRepo.existsByNameIgnoreCase(society.getName())) {
            throw new IllegalArgumentException("A society with this name already exists.");
        }

        // if (societyRepo.existsByContactEmailIgnoreCase(society.getContactEmail())) {
        //     throw new IllegalArgumentException("A society with this contact email already exists.");
        // }

        society.setActive(true);
        Society savedSociety = societyRepo.save(society);
        // Add the society admin as a member with ADMIN role
        if (savedSociety.getSocietyAdmin() != null) {
            addOrUpdateAdminMember(savedSociety, savedSociety.getSocietyAdmin());
        }
        return savedSociety;
    }

    //to find single society by id
    public Society getSocietyById(Integer id) {
        return societyRepo.findById(id).orElseThrow(() -> new RuntimeException("Society Not Found"));
    }

    // Update existing society details
    public void updateSociety(Society society) {
        Society existing = getSocietyById(society.getId());

        if (societyRepo.existsByNameIgnoreCaseAndIdNot(society.getName(), society.getId())) {
            throw new IllegalArgumentException("A society with this name already exists.");
        }

        // if (societyRepo.existsByContactEmailIgnoreCaseAndIdNot(society.getContactEmail(), society.getId())) {
        //     throw new IllegalArgumentException("A society with this contact email already exists.");
        // }

        existing.setName(society.getName());
        existing.setDescription(society.getDescription());
        existing.setContactEmail(society.getContactEmail());

        //check if admin has changed
        User newAdmin = society.getSocietyAdmin();
        if(!existing.getSocietyAdmin().getId().equals(newAdmin.getId())) {
            assignSocietyAdmin(existing, newAdmin);
        }
        societyRepo.save(existing);
    }

    public void assignSocietyAdmin(Society society, User newAdmin) {
        society.setSocietyAdmin(newAdmin);
        addOrUpdateAdminMember(society, newAdmin);
        societyRepo.save(society);
    }

    private void addOrUpdateAdminMember(Society society, User admin) {
        // Find if this user is already a member
        List<SocietyMember> members = memberRepo.findBySocietyId(society.getId());

        boolean found = false;
        for (SocietyMember m : members) {
            // If they are already in the list, make sure they are promoted to ADMIN
            if (m.getUser().getId().equals(admin.getId())) {
                m.setRoleInSociety(RoleInSociety.ADMIN);
                memberRepo.save(m);
                found = true;
            } else if (m.getRoleInSociety() == RoleInSociety.ADMIN) {
                // Optional: Demote the OLD admin to MEMBER if you want
                m.setRoleInSociety(RoleInSociety.MEMBER);
                memberRepo.save(m);
            }
        }

        // If the new admin wasn't in the member list at all, create a new record
        if (!found) {
            SocietyMember newAdminRecord = new SocietyMember();
            newAdminRecord.setSociety(society);
            newAdminRecord.setUser(admin);
            newAdminRecord.setRoleInSociety(RoleInSociety.ADMIN);
            memberRepo.save(newAdminRecord);
        }
    }

    // Toggle status (Activate/Deactivate)
    public void toggleSocietyStatus(Integer id) {
        Society society = getSocietyById(id);
        society.setActive(!society.isActive()); // Flips true to false, or false to true
        societyRepo.save(society);
    }

    public void addMemberToSociety(Society society, User user) {
        if (!society.isActive()) {
            throw new IllegalStateException("Cannot add members to an inactive society.");
        }
        if(!memberRepo.existsBySocietyIdAndUserId(society.getId(),user.getId())) {
            SocietyMember member = new SocietyMember();
            member.setSociety(society);
            member.setUser(user);
            memberRepo.save(member);
        }
    }

    //Method to get all members belonging to a specific society
    public List<SocietyMember> getSocietyMembers(Integer societyId) {
        return memberRepo.findBySocietyId(societyId);
    }

    public void joinSociety(Integer societyId, User user) {
        //check if already a member to avoid duplicate errors
        if(!memberRepo.existsBySocietyIdAndUserId(societyId,user.getId())) {
            SocietyMember  member = new SocietyMember();
            member.setSociety(societyRepo.findById(societyId).orElseThrow());
            member.setUser(user);
            member.setRoleInSociety(RoleInSociety.MEMBER);
            memberRepo.save(member);
        }
    }

    public void removeMember(Integer memberId) {
        memberRepo.deleteById(memberId);
    }

    public boolean isMemberOfSociety(Integer societyId, Integer userId) {
        return memberRepo.existsBySocietyIdAndUserId(societyId, userId);
    }

    public boolean isSocietyAdminForSociety(Integer societyId, Integer userId) {
        Society society = getSocietyById(societyId);
        return society.getSocietyAdmin() != null && society.getSocietyAdmin().getId().equals(userId);
    }


}
