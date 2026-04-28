package com.bank.controller;

import com.bank.entity.User;
import com.bank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // React App ka URL
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        // 1. Database mein user ko dhundna
        User user = userRepo.findByUsername(username).orElse(null);

        // 2. User existence aur Password check (BCrypt matching)
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            
            // Login Successful - JSON Response bhejna
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("username", user.getUsername());
            response.put("role", user.getRole()); // Example: "ROLE_STAFF"
            response.put("token", "dummy-jwt-token-123"); // Future mein yahan real JWT generate hoga
            
            return ResponseEntity.ok(response);
        }

        // 3. Agar credentials galat hain
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Invalid Username or Password");
        
        // Use 401 (Unauthorized) instead of 403 for wrong password
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists!");
        }
        // Password ko encrypt karna zaroori hai
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Default role agar missing ho toh (Prefix ROLE_ check karein)
        if(!user.getRole().startsWith("ROLE_")) {
            user.setRole("ROLE_" + user.getRole().toUpperCase());
        }
        
        userRepo.save(user);
        return ResponseEntity.ok("User registered successfully as " + user.getRole());
    }
}