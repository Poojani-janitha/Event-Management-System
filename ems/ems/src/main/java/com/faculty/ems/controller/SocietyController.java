package com.faculty.ems.controller;

import com.faculty.ems.model.Society;
import com.faculty.ems.model.SocietyMember;
import com.faculty.ems.model.User;
import com.faculty.ems.model.Event;
import com.faculty.ems.service.EventService;
import com.faculty.ems.service.SocietyService;
import com.faculty.ems.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
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

    @Autowired
    private EventService eventService;

    @GetMapping
    public String listSocieties(Model model,
                                Authentication authentication,
                                @RequestParam(required = false) String q) {
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isSocietyAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SOCIETY_ADMIN".equals(a.getAuthority()));
        boolean isMember = authentication.getAuthorities().stream()
            .anyMatch(a -> "ROLE_MEMBER".equals(a.getAuthority()));

        User currentUser = userService.findUserByUsername(authentication.getName());

        List<Society> societies;
        if (isAdmin) {
            societies = societyService.getAllSocieties();
        } else if (isSocietyAdmin) {
            societies = societyService.getSocietiesByAdminId(currentUser.getId());
        } else if (isMember) {
            societies = societyService.getSocietiesByMemberId(currentUser.getId());
        } else {
            societies = societyService.getAllSocieties();
        }

        if (q != null && !q.isBlank()) {
            String keyword = q.trim().toLowerCase();
            societies = societies.stream()
                    .filter(s -> (s.getName() != null && s.getName().toLowerCase().contains(keyword))
                            || (s.getDescription() != null && s.getDescription().toLowerCase().contains(keyword))
                            || (s.getContactEmail() != null && s.getContactEmail().toLowerCase().contains(keyword))
                            || (s.getSocietyAdmin() != null && s.getSocietyAdmin().getFullName() != null
                            && s.getSocietyAdmin().getFullName().toLowerCase().contains(keyword)))
                    .toList();
        }

        model.addAttribute("searchTerm", q);
        model.addAttribute("societies", societies);
        model.addAttribute("viewAllSocietiesUrl", "/societies/all");
        return "societies/list";
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('MEMBER','SOCIETY_ADMIN','ADMIN')")
    public String viewAllSocieties(Model model,
                                   Authentication authentication,
                                   @RequestParam(required = false) String q) {
        List<Society> societies = societyService.getAllSocieties();

        if (q != null && !q.isBlank()) {
            String keyword = q.trim().toLowerCase();
            societies = societies.stream()
                    .filter(s -> (s.getName() != null && s.getName().toLowerCase().contains(keyword))
                            || (s.getDescription() != null && s.getDescription().toLowerCase().contains(keyword))
                            || (s.getContactEmail() != null && s.getContactEmail().toLowerCase().contains(keyword))
                            || (s.getSocietyAdmin() != null && s.getSocietyAdmin().getFullName() != null
                            && s.getSocietyAdmin().getFullName().toLowerCase().contains(keyword)))
                    .toList();
        }

        User currentUser = userService.findUserByUsername(authentication.getName());
        model.addAttribute("societies", societies);
        model.addAttribute("searchTerm", q);
        model.addAttribute("joinedSocietyIds", societyService.getSocietiesByMemberId(currentUser.getId())
                .stream().map(Society::getId).toList());
        return "societies/list";
    }

    @GetMapping("/{id}")
    public String viewSociety(@PathVariable Integer id, Model model, Authentication authentication) {
        Society society = societyService.getSocietyById(id);

        // to get the member count
        List<SocietyMember> members = societyService.getSocietyMembers(id);

        //each soety card show ongoing events 
        List<Event> ongoingEvents = eventService.findBySociety(id.longValue())
                .stream()
                .filter(e -> e.getStatus() == Event.EventStatus.PUBLISHED)
                .toList();

        model.addAttribute("society", society);
        model.addAttribute("memberCount", members.size());
        model.addAttribute("ongoingEvents", ongoingEvents);
        // Logic to find society by ID and show details
        return "societies/detail";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("society", new Society());
        return "societies/form";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveSociety(@ModelAttribute("society") Society society,
                              Authentication authentication,
                              Model model) {
        User currentUser = userService.findUserByUsername(authentication.getName());
        society.setSocietyAdmin(currentUser);
        try {
            societyService.saveSociety(society);
            return "redirect:/societies";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("society", society);
            return "societies/form";
        }
    }

    @GetMapping("/{id}/members")
    public String viewMembers(@PathVariable Integer id, Model model, Authentication authentication) {
        Society society = societyService.getSocietyById(id);
        List<SocietyMember> members = societyService.getSocietyMembers(id);
        User currentUser = userService.findUserByUsername(authentication.getName());
        boolean canManageMembers = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))
                || societyService.isSocietyAdminForSociety(id, currentUser.getId());

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
        model.addAttribute("canManageMembers", canManageMembers);

        return "societies/members";
    }

    @PostMapping("/{id}/add-member")
    public String addMember(@PathVariable Integer id,
                            @RequestParam Integer userId,
                            Authentication authentication,
                            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User currentUser = userService.findUserByUsername(authentication.getName());
        boolean canManageMembers = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))
                || societyService.isSocietyAdminForSociety(id, currentUser.getId());

        if (!canManageMembers) {
            redirectAttributes.addFlashAttribute("error", "You can only add members to societies that you admin.");
            return "redirect:/societies/" + id + "/members";
        }

        User user = userService.findUserById(userId);
        Society society = societyService.getSocietyById(id);

        // Check if user is already a member
        if (societyService.isMemberOfSociety(society.getId(), user.getId())) {
            redirectAttributes.addFlashAttribute("error",
                    user.getFullName() + " is already registered in " + society.getName());
        } else {
            try {
                societyService.addMemberToSociety(society, user);
                redirectAttributes.addFlashAttribute("success",
                        user.getFullName() + " has been added to " + society.getName());
            } catch (IllegalStateException ex) {
                redirectAttributes.addFlashAttribute("error", ex.getMessage());
            }
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
    public String updateSociety(@PathVariable Integer id,
                                @ModelAttribute("society") Society society,
                                Model model) {
        society.setId(id);
        try {
            societyService.updateSociety(society);
            return "redirect:/societies/" + id;
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("society", society);
            model.addAttribute("users", userService.findAllUsers());
            return "societies/form";
        }
    }

    // Handle Deactivation
    @PostMapping("/{id}/toggle")
    public String toggleStatus(@PathVariable Integer id) {
        societyService.toggleSocietyStatus(id);
        return "redirect:/societies";
    }
}
