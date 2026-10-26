package com.codementor.service;

import com.codementor.dto.Dtos.*;
import com.codementor.model.User;
import com.codementor.repository.UserRepository;
import com.codementor.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public UserService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        User u = new User();
        u.setName(req.name().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(req.password()));
        users.save(u);
        return new AuthResponse(jwt.create(email), UserDto.from(u));
    }

    public AuthResponse login(LoginRequest req) {
        User u = users.findByEmail(req.email().trim().toLowerCase())
                .filter(x -> encoder.matches(req.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return new AuthResponse(jwt.create(u.getEmail()), UserDto.from(u));
    }

    /** The logged-in student. Every query in the app is scoped to this user, so students never see each other's data. */
    public User current(Authentication auth) {
        return users.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in again"));
    }

    public UserDto updateProfile(User u, ProfileRequest req) {
        u.setName(req.name().trim());
        if (req.preferredLanguage() != null && !req.preferredLanguage().isBlank())
            u.setPreferredLanguage(req.preferredLanguage());
        u.setWeeklyGoal(req.weeklyGoal() == null || req.weeklyGoal() == 0 ? null : req.weeklyGoal());
        return UserDto.from(users.save(u));
    }
}
