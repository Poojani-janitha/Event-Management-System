package com.faculty.ems.dto;

import com.faculty.ems.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEditDto {

    private Integer id;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Pattern(regexp = "^$|.{8,}", message = "Password must be at least 8 characters") //optional
    private String password;

    private Role role;

    private boolean enabled;
}
