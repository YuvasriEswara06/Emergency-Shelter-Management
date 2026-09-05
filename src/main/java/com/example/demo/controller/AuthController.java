package com.example.demo.controller;

import com.example.demo.entity.Shelter;
import com.example.demo.entity.Users;
import com.example.demo.repository.ShelterRepository;
import com.example.demo.repository.UsersRepository;
import com.example.demo.security.CustomUserPrincipal;
import com.example.demo.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsersRepository usersRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final ShelterRepository shelterRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          UsersRepository usersRepository,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder,
                          ShelterRepository shelterRepository) {
        this.authenticationManager = authenticationManager;
        this.usersRepository = usersRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.shelterRepository = shelterRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("error", "Username and password are required"));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            Users user = usersRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            CustomUserPrincipal principal = new CustomUserPrincipal(user);
            String token = jwtUtil.generateToken(principal);
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "tokenType", "Bearer",
                    "username", user.getUsername(),
                    "role", user.getRole()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }
    }

    // POST /api/auth/register
    // body: { "username": "teststaff", "password": "test1234", "role": "Staff", "shelterId": 1 }
    // shelterId is optional (omit or null for Admin, or a Staff user not yet
    // assigned to a shelter)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String role = (String) body.get("role");

        if (username == null || username.isBlank() || password == null || password.isBlank()
                || role == null || role.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username, password, and role are required"));
        }
        if (!role.equalsIgnoreCase("Admin") && !role.equalsIgnoreCase("Staff")) {
            return ResponseEntity.badRequest().body(Map.of("error", "role must be Admin or Staff"));
        }
        if (usersRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
        }

        Users user = new Users();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password)); // real BCrypt hash, not plain text
        user.setRole(role);

        Object shelterIdObj = body.get("shelterId");
        if (shelterIdObj != null) {
            try {
                Integer shelterId = ((Number) shelterIdObj).intValue();
                Shelter shelter = shelterRepository.findById(shelterId)
                        .orElseThrow(() -> new IllegalArgumentException("Shelter not found: " + shelterId));
                user.setShelter(shelter);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
        }

        Users saved = usersRepository.save(user);
        return ResponseEntity.status(201).body(Map.of(
                "userId", saved.getUserId(),
                "username", saved.getUsername(),
                "role", saved.getRole()));
    }
}