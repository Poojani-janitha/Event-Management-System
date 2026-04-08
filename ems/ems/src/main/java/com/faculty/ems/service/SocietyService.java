package com.faculty.ems.service;

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
        return societyRepo.save(society);
    }

    public void addMemberToSociety(Society society, User user) {
//        if(!memberRepo.existsBySocietyIdAndUserId(society.getId(),user.getId())) {
//            SocietyMember member = new SocietyMember();
//            member.setSociety(society);
//            member.setUser(user);
//            memberRepo.save(member);
//        }
    }
}
