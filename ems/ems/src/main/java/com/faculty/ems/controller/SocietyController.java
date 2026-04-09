package com.faculty.ems.controller;

import com.faculty.ems.model.Society;
import com.faculty.ems.service.SocietyService;
import com.faculty.ems.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
}
