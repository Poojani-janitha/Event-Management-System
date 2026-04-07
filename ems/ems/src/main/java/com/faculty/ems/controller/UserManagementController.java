package com.faculty.ems.controller;

import com.faculty.ems.dto.UserEditDto;
import com.faculty.ems.model.Role;
import com.faculty.ems.model.User;
import com.faculty.ems.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
                user.getFullName(), user.getRole(), user.isEnabled()
        );
        model.addAttribute("user", dto);
        model.addAttribute("roles", Role.values());
        return "user/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateUser(@PathVariable Integer id,
                             @Valid @ModelAttribute("user") UserEditDto dto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "user/edit";
        }
        dto.setId(id);
        userService.updateUser(dto);
        ra.addFlashAttribute("success", "User updated successfully");
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Integer id, RedirectAttributes ra) {
        userService.deleteUser(id);
        ra.addFlashAttribute("success", "User deleted successfully");
        return "redirect:/users";
    }
}
