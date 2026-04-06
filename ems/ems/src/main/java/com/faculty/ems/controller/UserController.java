package com.faculty.ems.controller;

import com.faculty.ems.dto.UserRegistrationDto;
import com.faculty.ems.model.User;
import com.faculty.ems.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class UserController {
    @Autowired
    UserService userService;

    @GetMapping("/register")
    public String showRegisterForm(Model model){
        model.addAttribute("user",new UserRegistrationDto());
        return "auth/register";
    }


    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("user") UserRegistrationDto dto, BindingResult result, RedirectAttributes ra){
        if(result.hasErrors()){
            return "auth/register";
        }
        try{
            userService.registerUser(dto);
            ra.addFlashAttribute("success","Account created!");
            return "redirect:auth/register";
        }catch(IllegalArgumentException e){
            ra.addFlashAttribute("error",e.getMessage());
            return "redirect:/register";
        }

    }
}
