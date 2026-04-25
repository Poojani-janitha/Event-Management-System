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

    public Society saveSociety(Society society) {
        if (society == null) throw new IllegalArgumentException("Society cannot be null");
        Society savedSociety = societyRepo.save(society);
        // Add the society admin as a member with ADMIN role
        if (savedSociety.getSocietyAdmin() != null) {
            addOrUpdateAdminMember(savedSociety, savedSociety.getSocietyAdmin());
        }
        return savedSociety;
    }

    //to find single society by id
    public Society getSocietyById(int id) {
        return societyRepo.findById(id).orElseThrow(() -> new RuntimeException("Society Not Found"));
    }

    // Update existing society details
    public void updateSociety(Society society) {
        Society existing = getSocietyById(society.getId());

        existing.setName(society.getName());
        existing.setDescription(society.getDescription());
        existing.setContactEmail(society.getContactEmail());

        //check if admin has changed
        User newAdmin = society.getSocietyAdmin();
        if(!existing.getSocietyAdmin().getId().equals(newAdmin.getId())) {
            existing.setSocietyAdmin(newAdmin);
            addOrUpdateAdminMember(existing, newAdmin);
        }
        societyRepo.save(existing);
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
    public void toggleSocietyStatus(int id) {
        Society society = getSocietyById(id);
        society.setActive(!society.isActive()); // Flips true to false, or false to true
        societyRepo.save(society);
    }

    public void addMemberToSociety(Society society, User user) {
        if(!memberRepo.existsBySocietyIdAndUserId(society.getId(),user.getId())) {
            SocietyMember member = new SocietyMember();
            member.setSociety(society);
            member.setUser(user);
            memberRepo.save(member);
        }
    }

    //Method to get all members belonging to a specific society
    public List<SocietyMember> getSocietyMembers(int societyId) {
        return memberRepo.findBySocietyId(societyId);
    }

    public void joinSociety(int societyId, User user) {
        //check if already a member to avoid duplicate errors
        if(!memberRepo.existsBySocietyIdAndUserId(societyId,user.getId())) {
            SocietyMember  member = new SocietyMember();
            member.setSociety(societyRepo.findById(societyId).orElseThrow());
            member.setUser(user);
            member.setRoleInSociety(RoleInSociety.MEMBER);
            memberRepo.save(member);
        }
    }

    public void removeMember(int memberId) {
        memberRepo.deleteById(memberId);
    }

    public boolean isMemberOfSociety(int societyId, int userId) {
        return memberRepo.existsBySocietyIdAndUserId(societyId, userId);
    }


}
