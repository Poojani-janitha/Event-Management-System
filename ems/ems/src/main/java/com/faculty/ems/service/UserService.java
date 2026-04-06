package com.faculty.ems.service;

import com.faculty.ems.dto.UserRegistrationDto;
import com.faculty.ems.model.User;
import com.faculty.ems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;// use singleton object

    public User  registerUser(UserRegistrationDto dto){
        if(userRepository.existsByUsername(dto.getUsername())){
            throw new IllegalArgumentException("user name already taken");
        }

        if(userRepository.existsByEmail(dto.getEmail())){
            throw new IllegalArgumentException("Email already registered");

        }
        //Builder used in here

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .build();

        return userRepository.save(user);
    }

}
