package com.codementor.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "bookmarks", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "problem_id"}))
@Getter @Setter @NoArgsConstructor
public class Bookmark {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private User user;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) private Problem problem;
}
