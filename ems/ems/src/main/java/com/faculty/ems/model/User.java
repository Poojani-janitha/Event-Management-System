package com.faculty.ems.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;

@Entity
@Table(name = "users")
@Builder
public class User {

}
