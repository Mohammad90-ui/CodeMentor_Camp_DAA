package com.codementor.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity @Table(name = "problems") @Getter @Setter @NoArgsConstructor
public class Problem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String title;
    @Column(columnDefinition = "TEXT") private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Difficulty difficulty;
    private int estimatedMinutes;
    private String externalUrl;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "problem_topics",
            joinColumns = @JoinColumn(name = "problem_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id"))
    private Set<Topic> topics = new HashSet<>();
}
