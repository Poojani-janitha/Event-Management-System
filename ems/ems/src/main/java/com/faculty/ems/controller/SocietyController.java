package com.faculty.ems.controller;

import com.faculty.ems.model.Society;
import com.faculty.ems.model.SocietyMember;
import com.faculty.ems.model.User;
import com.faculty.ems.service.SocietyService;
import com.faculty.ems.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/societies")
public class SocietyController {
    @Autowired
    private SocietyService societyService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listSocieties(Model model) {
        model.addAttribute("societies", societyService.getAllSocieties());
        return "societies/list";
    }

    @GetMapping("/{id}")
    public String viewSociety(@PathVariable Integer id, Model model) {
        Society society = societyService.getSocietyById(id);
        // to get the member count
        List<SocietyMember> members = societyService.getSocietyMembers(id);
        model.addAttribute("society", society);
        model.addAttribute("memberCount", members.size());
        // Logic to find society by ID and show details
        return "societies/detail";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("society", new Society());
        model.addAttribute("users", userService.findAllUsers());
        return "societies/form";
    }

    @PostMapping("/save")
    public String saveSociety(@ModelAttribute("society") Society society) {
        societyService.saveSociety(society);
        return "redirect:/societies";
    }

    @GetMapping("/{id}/members")
    public String viewMembers(@PathVariable Integer id, Model model) {
        Society society = societyService.getSocietyById(id);
        List<SocietyMember> members = societyService.getSocietyMembers(id);

        // Sort members: admins first, then members
        members.sort((m1, m2) -> {
            if (m1.getRoleInSociety().name().equals("ADMIN") && !m2.getRoleInSociety().name().equals("ADMIN")) {
                return -1; // m1 comes first
            } else if (!m1.getRoleInSociety().name().equals("ADMIN") && m2.getRoleInSociety().name().equals("ADMIN")) {
                return 1; // m2 comes first
            }
            return 0; // maintain order
        });

        model.addAttribute("society", society);
        model.addAttribute("members", members);
        model.addAttribute("allUsers", userService.findAllUsers());

        return "societies/members";
    }

    @PostMapping("/{id}/add-member")
    public String addMember(@PathVariable Integer id, @RequestParam Integer userId, Model model,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User user = userService.findUserById(userId);
        Society society = societyService.getSocietyById(id);

        // Check if user is already a member
        if (societyService.isMemberOfSociety(society.getId(), user.getId())) {
            redirectAttributes.addFlashAttribute("error",
                    user.getFullName() + " is already registered in " + society.getName());
        } else {
            societyService.addMemberToSociety(society, user);
            redirectAttributes.addFlashAttribute("success",
                    user.getFullName() + " has been added to " + society.getName());
        }

        return "redirect:/societies/" + id + "/members";
    }

    @PostMapping("/{id}/remove-member/{memberId}")
    public String removeMember(@PathVariable Integer id, @PathVariable Integer memberId) {
        societyService.removeMember(memberId);
        return "redirect:/societies/" + id + "/members";
    }

    // Show Edit Form
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Integer id, Model model) {
        model.addAttribute("society", societyService.getSocietyById(id));
        model.addAttribute("users", userService.findAllUsers());
        return "societies/form"; // Reusing the same form!
    }

    // Handle Update
    @PostMapping("/{id}/update")
    public String updateSociety(@PathVariable Integer id, @ModelAttribute("society") Society society) {
        society.setId(id);
        societyService.updateSociety(society);
        return "redirect:/societies/" + id;
    }

    // Handle Deactivation
    @PostMapping("/{id}/toggle")
    public String toggleStatus(@PathVariable Integer id) {
        societyService.toggleSocietyStatus(id);
        return "redirect:/societies";
    }
}
