package com.codementor.controller;

import com.codementor.dto.Dtos.*;
import com.codementor.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService users;
    public AuthController(UserService users) { this.users = users; }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) { return users.register(req); }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) { return users.login(req); }
}
