package com.faculty.ems.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocietyAdminRequestDto {

    private Integer id;

    @NotNull(message = "Please select a society")
    private Integer selectedSocietyId;

    private String societyName;

    private String contactEmail;

    private String description;
}
