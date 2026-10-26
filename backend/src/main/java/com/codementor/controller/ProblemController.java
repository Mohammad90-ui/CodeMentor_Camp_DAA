package com.codementor.controller;

import com.codementor.dto.Dtos.*;
import com.codementor.model.Topic;
import com.codementor.repository.TopicRepository;
import com.codementor.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProblemController {
    private final ProblemService problems;
    private final UserService users;
    private final TopicRepository topics;

    public ProblemController(ProblemService problems, UserService users, TopicRepository topics) {
        this.problems = problems; this.users = users; this.topics = topics;
    }

    @GetMapping("/topics")
    public List<String> topics() { return topics.findAll().stream().map(Topic::getName).sorted().toList(); }

    @GetMapping("/problems")
    public List<ProblemDto> list(Authentication auth,
                                 @RequestParam(required = false) String q,
                                 @RequestParam(required = false) String topic,
                                 @RequestParam(required = false) String difficulty,
                                 @RequestParam(required = false) String sort) {
        return problems.search(users.current(auth), q, topic, difficulty, sort);
    }

    @GetMapping("/problems/{id}")
    public ProblemDetailDto detail(Authentication auth, @PathVariable Long id) {
        return problems.detail(users.current(auth), id);
    }
}
