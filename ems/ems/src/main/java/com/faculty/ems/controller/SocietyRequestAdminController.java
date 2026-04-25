package com.faculty.ems.controller;

import com.faculty.ems.service.SocietyAdminRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/society-requests")
@PreAuthorize("hasRole('ADMIN')")
public class SocietyRequestAdminController {

    @Autowired
    private SocietyAdminRequestService societyAdminRequestService;

    // Future: Add admin endpoints for viewing/managing requests
    // @GetMapping
    // @PostMapping("/{id}/approve")
    // @PostMapping("/{id}/reject")
}
