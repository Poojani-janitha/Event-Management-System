package com.faculty.ems.controller;

import com.faculty.ems.service.SocietyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/societies")
public class SocietyController {
    @Autowired
    private SocietyService societyService;

    @GetMapping
    public String listSocieties(Model model) {
        model.addAttribute("societies", societyService.getAllSocieties());
        return "societies/list";
    }

    @GetMapping("/{id}")
    public String viewSociety(@PathVariable Long id, Model model) {
        // Logic to find society by ID and show details
        return "societies/detail";
    }
}
