package com.codementor.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "users") @Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String email;
    @Column(nullable = false) private String passwordHash;   // BCrypt hash, never plain text
    private String preferredLanguage = "Java";
    private Integer weeklyGoal;                               // optional: attempts per week
}
