package com.example.demo.controller;

import com.example.demo.entity.Users;
import com.example.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Helper to sanitize User by excluding passwordHash
    private Map<String, Object> sanitize(Users u) {
        return Map.of(
                "userId", u.getUserId(),
                "username", u.getUsername(),
                "role", u.getRole(),
                "shelterId", u.getShelter() == null ? null : u.getShelter().getShelterId()
        );
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getUsers() {
        List<Map<String, Object>> list = userService.getAllUsers().stream().map(this::sanitize).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id).map(u -> ResponseEntity.ok(sanitize(u))).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Users user) {
        try {
            Users created = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(sanitize(created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}