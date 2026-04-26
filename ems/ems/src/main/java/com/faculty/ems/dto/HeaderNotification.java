package com.faculty.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HeaderNotification {
    private String title;
    private String message;
    private String link;
    private String variant;
}
