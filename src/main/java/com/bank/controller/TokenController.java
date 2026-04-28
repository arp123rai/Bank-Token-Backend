package com.bank.controller;

import com.bank.entity.Token;
import com.bank.repository.TokenRepository;
import com.bank.service.TokenService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
@RestController
@RequestMapping("/api/tokens")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class TokenController {

    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private TokenRepository tokenRepo;

    // 1. Token Generate (CASH/LOAN) - PUBLIC ACCESS
    @PostMapping("/generate")
    public ResponseEntity<Token> generateToken(@RequestParam String phone, @RequestParam String type) {
        if(!type.equals("CASH") && !type.equals("LOAN")) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(tokenService.createToken(phone, type));
    }

    // 2. Staff: Call Next Token - RESTRICTED TO ROLE_STAFF
    // @PreAuthorize check karta hai ki login karne wale ka role STAFF hai ya nahi
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/call-next/{counterId}")
    public ResponseEntity<Token> callNext(@PathVariable Integer counterId, @RequestParam String service) {
        Token t = tokenService.callNextToken(counterId, service);
        if(t != null) return ResponseEntity.ok(t);
        return ResponseEntity.noContent().build(); 
    }
    
    // 3. Manager Dashboard Stats - RESTRICTED TO ROLE_MANAGER
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Full Day Traffic (10 AM - 5 PM view)
        stats.put("hourly", tokenRepo.getFullDayTraffic());
        
        // Distribution for Doughnut Chart
        stats.put("distribution", tokenRepo.getServiceDistribution());
        
        // Today's total count
        stats.put("totalToday", tokenRepo.countByCreatedAtAfter(LocalDate.now().atStartOfDay()));
        stats.put("weekly", tokenRepo.getWeeklyStats());
        
        return ResponseEntity.ok(stats);
    }
    
    
 // TokenController.java mein ye method add karein:

    @GetMapping("/avg-wait/{type}")
    public ResponseEntity<Double> getAvgWait(@PathVariable String type) {
        // Agar service logic sahi hai toh ye return karega
        return ResponseEntity.ok(tokenService.getWaitTimeForCategory(type));
    }
    
    
//    @PutMapping("/skip/{id}")
//    @Transactional
//    public ResponseEntity<?> skipToken(@PathVariable Long id) {
//        return tokenRepo.findById(id)
//            .map(token -> {
//                token.setStatus("SKIPPED");
//                tokenRepo.save(token);
//                return ResponseEntity.ok("Token #" + token.getTokenNumber() + " skipped.");
//            })
//            .orElse(ResponseEntity.status(404).body("Token ID not found"));
//    }
}