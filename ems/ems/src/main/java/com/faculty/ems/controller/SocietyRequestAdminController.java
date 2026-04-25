package com.faculty.ems.controller;

import com.faculty.ems.service.SocietyAdminRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/society-requests")
@PreAuthorize("hasRole('ADMIN')")
public class SocietyRequestAdminController {

    @Autowired
    private SocietyAdminRequestService societyAdminRequestService;

    @PostMapping("/{id}/approve")
    public String approveRequest(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            societyAdminRequestService.approveRequest(id);
            ra.addFlashAttribute("success", "Request approved and user role updated to SOCIETY_ADMIN.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/{id}/reject")
    public String rejectRequest(@PathVariable Integer id,
                                @RequestParam("reason") String reason,
                                RedirectAttributes ra) {
        try {
            societyAdminRequestService.rejectRequest(id, reason);
            ra.addFlashAttribute("success", "Request rejected.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/dashboard";
    }

}
