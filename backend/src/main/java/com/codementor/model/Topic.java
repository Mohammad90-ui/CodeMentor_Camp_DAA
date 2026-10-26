package com.codementor.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "topics") @Getter @Setter @NoArgsConstructor
public class Topic {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String name;
    public Topic(String name) { this.name = name; }
}
