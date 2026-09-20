package com.weatherx.backend.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.weatherx.backend.model.User;
import com.weatherx.backend.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ==========================================
    // SIGNUP
    // POST /api/users
    // ==========================================

    @PostMapping("/users")
    public ResponseEntity<?> signup(@RequestBody User user) {

        if (user.getName() == null ||
            user.getEmail() == null ||
            user.getPassword() == null) {

            return ResponseEntity
                    .badRequest()
                    .body("Name, email and password are required.");
        }

        Optional<User> existingUser =
                userRepository.findByEmail(user.getEmail());

        if (existingUser.isPresent()) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already registered.");
        }

        User savedUser = userRepository.save(user);

        // Never return password to frontend
        savedUser.setPassword(null);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedUser);
    }

    // ==========================================
    // LOGIN
    // POST /api/auth/login
    // ==========================================

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        if (request.getEmail() == null ||
            request.getPassword() == null) {

            return ResponseEntity
                    .badRequest()
                    .body("Email and password are required.");
        }

        Optional<User> userOptional =
                userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password.");
        }

        User user = userOptional.get();

        if (!user.getPassword().equals(request.getPassword())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password.");
        }

        // Never return password to frontend
        user.setPassword(null);

        return ResponseEntity.ok(user);
    }

    // ==========================================
    // LOGIN REQUEST
    // ==========================================

    public static class LoginRequest {

        private String email;
        private String password;

        public LoginRequest() {
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}