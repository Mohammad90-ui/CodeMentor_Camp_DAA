package com.codementor.controller;

import com.codementor.dto.Dtos.*;
import com.codementor.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attempts")
public class AttemptController {
    private final AttemptService attempts;
    private final UserService users;

    public AttemptController(AttemptService attempts, UserService users) { this.attempts = attempts; this.users = users; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public AttemptDto create(Authentication auth, @Valid @RequestBody AttemptRequest req) {
        return attempts.create(users.current(auth), req);
    }

    @GetMapping
    public List<AttemptDto> history(Authentication auth) { return attempts.history(users.current(auth)); }
}
