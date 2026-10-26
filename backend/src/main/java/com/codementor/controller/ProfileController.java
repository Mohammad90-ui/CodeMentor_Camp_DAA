package com.codementor.controller;

import com.codementor.dto.Dtos.*;
import com.codementor.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
public class ProfileController {
    private final UserService users;
    public ProfileController(UserService users) { this.users = users; }

    @GetMapping
    public UserDto me(Authentication auth) { return UserDto.from(users.current(auth)); }

    @PutMapping
    public UserDto update(Authentication auth, @Valid @RequestBody ProfileRequest req) {
        return users.updateProfile(users.current(auth), req);
    }
}
