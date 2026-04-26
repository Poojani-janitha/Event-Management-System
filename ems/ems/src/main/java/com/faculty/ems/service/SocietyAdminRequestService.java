package com.faculty.ems.service;

import com.faculty.ems.dto.SocietyAdminRequestDto;
import com.faculty.ems.model.Role;
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

    @Autowired
    private EmailService emailService;

    // Submit a new request
    public SocietyAdminRequest submitRequest(User user, SocietyAdminRequestDto dto) {
        Society society = societyRepository.findById(dto.getSelectedSocietyId())
                .orElseThrow(() -> new EntityNotFoundException("Society not found"));

                //check if user already has an approved request for the same society
        boolean alreadyApprovedForSameSociety = requestRepository
            .existsByUserIdAndCreatedSocietyIdAndStatus(
                user.getId(),
                society.getId(),
                SocietyAdminRequest.RequestStatus.APPROVED
            );

        if (alreadyApprovedForSameSociety) {
            throw new IllegalArgumentException("Your request for this society was already approved. You cannot request again for the same society.");
        }

        SocietyAdminRequest request = SocietyAdminRequest.builder()
                .user(user)
                .societyName(society.getName())
                .contactEmail(society.getContactEmail())
                .description("Request to become admin of society: " + society.getName())
                .createdSocietyId(society.getId())
                .status(SocietyAdminRequest.RequestStatus.PENDING)
                .build();

        return requestRepository.save(request);
    }

    public List<Society> getAllSocieties() {
        return societyRepository.findAll();
    }

    // Get requests by user
    public List<SocietyAdminRequest> getUserRequests(Integer userId) {
        return requestRepository.findByUserId(userId);
    }

    //to get pending requests for admin dashboard
    public List<SocietyAdminRequest> getPendingRequests() {
        return requestRepository.findByStatusOrderByCreatedAtDesc(SocietyAdminRequest.RequestStatus.PENDING);
    }

    public List<SocietyAdminRequest> getRequestsByOptionalStatus(String status) {
        if (status == null || status.isBlank()) {
            return requestRepository.findAllByOrderByCreatedAtDesc();
        }

        try {
            SocietyAdminRequest.RequestStatus parsedStatus = SocietyAdminRequest.RequestStatus.valueOf(status.toUpperCase());
            return requestRepository.findByStatusOrderByCreatedAtDesc(parsedStatus);
        } catch (IllegalArgumentException ex) {
            return requestRepository.findAllByOrderByCreatedAtDesc();
        }
    }

    public SocietyAdminRequest getRequestById(Integer id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found with id: " + id));
    }

    public void approveRequest(Integer id) {
        SocietyAdminRequest request = getRequestById(id);

        if (request.getStatus() != SocietyAdminRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be approved");
        }

        request.setStatus(SocietyAdminRequest.RequestStatus.APPROVED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewNotes("Approved by admin and assigned as society admin");

        User user = request.getUser();
        user.setRole(Role.SOCIETY_ADMIN);

        if (request.getCreatedSocietyId() == null) {
            throw new IllegalStateException("Requested society is missing");
        }

        Society society = societyRepository.findById(request.getCreatedSocietyId())
                .orElseThrow(() -> new EntityNotFoundException("Requested society not found"));
        society.setSocietyAdmin(user);
        societyRepository.save(society);

        requestRepository.save(request);
        emailService.sendSocietyRequestDecisionEmail(request);
    }

    public void rejectRequest(Integer id, String reason) {
        SocietyAdminRequest request = getRequestById(id);

        if (request.getStatus() != SocietyAdminRequest.RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be rejected");
        }

        request.setStatus(SocietyAdminRequest.RequestStatus.REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewNotes(reason == null || reason.isBlank() ? "Rejected by admin" : reason.trim());

        requestRepository.save(request);
        emailService.sendSocietyRequestDecisionEmail(request);
    }
}