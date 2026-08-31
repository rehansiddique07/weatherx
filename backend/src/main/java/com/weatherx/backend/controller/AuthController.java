package com.weatherx.backend.controller;

import com.weatherx.backend.model.User;
import com.weatherx.backend.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ==============================
    // LOGIN
    // ==============================

    @GetMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String email,
            @RequestParam String password
    ) {

        Optional<User> userOptional =
                userRepository.findByEmail(email.trim());

        if (userOptional.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid email or password.");
        }

        User user = userOptional.get();

        if (!password.equals(user.getPassword())) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid email or password.");
        }

        return ResponseEntity.ok(user);
    }

    // ==============================
    // SIGNUP
    // ==============================

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password
    ) {

        String cleanName = name.trim();
        String cleanEmail = email.trim();

        if (cleanName.isEmpty()
                || cleanEmail.isEmpty()
                || password.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("All fields are required.");
        }

        if (userRepository.findByEmail(cleanEmail).isPresent()) {

            return ResponseEntity
                    .badRequest()
                    .body("Email already registered.");
        }

        User user = new User();

        user.setName(cleanName);
        user.setEmail(cleanEmail);
        user.setPassword(password);

        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(savedUser);
    }
}