package com.codementor.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "attempts") @Getter @Setter @NoArgsConstructor
public class Attempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private User user;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) private Problem problem;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Result result;
    private Integer timeSpentMinutes;
    private String mistakeType;                 // e.g. LOGIC_ERROR, EDGE_CASE
    @Column(length = 1000) private String notes;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
}
