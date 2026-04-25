package com.faculty.ems.service;

import com.faculty.ems.dto.SocietyAdminRequestDto;
import com.faculty.ems.model.Society;
import com.faculty.ems.model.SocietyAdminRequest;
import com.faculty.ems.model.User;
import com.faculty.ems.repository.SocietyAdminRequestRepository;
import com.faculty.ems.repository.SocietyRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class SocietyAdminRequestService {

    @Autowired
    private SocietyAdminRequestRepository requestRepository;

    @Autowired
    private SocietyRepository societyRepository;

    // Submit a new request
    public SocietyAdminRequest submitRequest(User user, SocietyAdminRequestDto dto) {
        // Check if user already has a pending request
        var existingRequest = requestRepository.findByUserIdAndStatus(user.getId(), SocietyAdminRequest.RequestStatus.PENDING);
        if (existingRequest.isPresent()) {
            throw new IllegalArgumentException("You already have a pending society admin request");
        }

        SocietyAdminRequest request = SocietyAdminRequest.builder()
                .user(user)
                .societyName(dto.getSocietyName())
                .contactEmail(dto.getContactEmail())
                .description(dto.getDescription())
                .status(SocietyAdminRequest.RequestStatus.PENDING)
                .build();

        return requestRepository.save(request);
    }

    // Get requests by user
    public List<SocietyAdminRequest> getUserRequests(Integer userId) {
        return requestRepository.findByUserId(userId);
    }