package com.faculty.ems.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {



    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Enter a Valid email")
    private String email;

    @NotBlank(message = "Enter a Valid email")
    private String password;

    @NotBlank
    private String fullName;

}
