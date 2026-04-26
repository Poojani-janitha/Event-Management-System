package com.faculty.ems.controller;

import com.faculty.ems.dto.SocietyAdminRequestDto;
import com.faculty.ems.dto.UserEditDto;
import com.faculty.ems.model.User;
import com.faculty.ems.service.SocietyAdminRequestService;
import com.faculty.ems.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserManagementController {

    @Autowired
    private UserService userService;

    @Autowired
    private SocietyAdminRequestService societyAdminRequestService;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "user/list";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        User user = userService.findUserById(id);
        UserEditDto dto = new UserEditDto(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getFullName(), null, user.getRole(), user.isEnabled());
        model.addAttribute("user", dto);
        return "user/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Integer id,
            @Valid @ModelAttribute("user") UserEditDto dto,
            BindingResult result,
            Model model,
            RedirectAttributes ra) {
        if (dto.getRole() == null) {
            dto.setRole(userService.findUserById(id).getRole());
        }
        if (result.hasErrors()) {
            return "user/edit";
        }
        dto.setId(id);
        try {
            userService.updateUser(dto);
            ra.addFlashAttribute("success", "User updated successfully");
            return "redirect:/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "user/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            userService.deleteUser(id);
            ra.addFlashAttribute("success", "User deleted successfully");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "User have some booking appoinments currently, cannot be deleted");
        }
        return "redirect:/users";
    }

    @GetMapping("/society-admin-request")
    @PreAuthorize("hasAnyRole('MEMBER','SOCIETY_ADMIN')")
    public String showSocietyAdminRequestPage(Model model, Authentication auth) {
        model.addAttribute("request", new SocietyAdminRequestDto());
        model.addAttribute("societies", societyAdminRequestService.getAllSocieties());
        User currentUser = userService.findUserByUsername(auth.getName());
        var existingRequest = societyAdminRequestService.getUserRequests(currentUser.getId());
        model.addAttribute("userRequests", existingRequest);
        return "user/society_admin_requests";
    }

    @PostMapping("/society-admin-request")
    @PreAuthorize("hasAnyRole('MEMBER','SOCIETY_ADMIN')")
    public String saveSocietyAdminRequest(@Valid @ModelAttribute("request") SocietyAdminRequestDto dto,
                                         BindingResult result,
                                         Authentication auth,
                                         Model model,
                                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("societies", societyAdminRequestService.getAllSocieties());
            User currentUser = userService.findUserByUsername(auth.getName());
            model.addAttribute("userRequests", societyAdminRequestService.getUserRequests(currentUser.getId()));
            return "user/society_admin_requests";
        }

        try {
            User currentUser = userService.findUserByUsername(auth.getName());
            societyAdminRequestService.submitRequest(currentUser, dto);
            ra.addFlashAttribute("success", "Society admin request submitted successfully!");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users/society-admin-request";
    }
}
