package com.bank.service;

import com.bank.entity.Token;
import com.bank.repository.TokenRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TokenService {

    @Autowired
    private TokenRepository tokenRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // --- FIX 1: TWILIO CREDENTIALS ---
    // Inhe hamesha Twilio Console se copy-paste karein. 
    // SID "AC" se shuru hona chahiye aur Auth Token bina spaces ke hona chahiye.
    public static final String ACCOUNT_SID = "ACxxxxxxxxxxxxxxxxxxxxxxxx"; // Apna sahi SID yahan dalein
    public static final String AUTH_TOKEN = "your_auth_token_here";         // Apna sahi Token yahan dalein
    public static final String TWILIO_PHONE = "+1234567890"; 

    // --- Create Token ---
    public Token createToken(String phone, String serviceType) {
        long count = tokenRepo.countByServiceTypeAndCreatedAtAfter(serviceType, LocalDate.now().atStartOfDay());
        String prefix = serviceType.equals("CASH") ? "C" : "L";
        
        Token token = new Token();
        token.setTokenNumber(prefix + "-" + (count + 1));
        token.setServiceType(serviceType);
        token.setUserPhone(phone);
        token.setStatus("WAITING");
        token.setCounterNumber(0);
        token.setCreatedAt(LocalDateTime.now());
        Token savedToken = tokenRepo.save(token);
        return tokenRepo.save(token);
    }

    // --- Call Next Token ---
    public Token callNextToken(Integer counterId, String serviceType) {
        // Status "WAITING" fetch karein jo sabse pehle bana tha
        Optional<Token> next = tokenRepo.findFirstByServiceTypeAndStatusOrderByCreatedAtAsc(serviceType, "WAITING");
        
        if(next.isPresent()) {
            Token t = next.get();
            t.setStatus("SERVING");
            t.setCounterNumber(counterId);
            t.setCalledAt(LocalDateTime.now()); 
            
            // Hibernate Null Issue Fix: save karne se pehle object mein data hai
            Token savedToken = tokenRepo.save(t);

            // WebSocket update
            messagingTemplate.convertAndSend("/topic/token-updates", savedToken);

            // SMS notify next person (Wait-list alert)
            notifyNextInLine(serviceType);

            return savedToken;
        }
        return null;
    }

    // --- SMS Logic ---
    private void notifyNextInLine(String serviceType) {
        // Queue mein agle person ko check karein
        Optional<Token> next = tokenRepo.findFirstByServiceTypeAndStatusOrderByCreatedAtAsc(serviceType, "WAITING");
        
        if(next.isPresent()) {
            String customerPhone = next.get().getUserPhone();
            
            // --- FIX 2: VALID PHONE NUMBER CHECK ---
            // Twilio random numbers (12345) par message nahi bhejta.
            // Hum ek check laga dete hain taaki console mein error na aaye.
            if(customerPhone == null || customerPhone.length() < 10) {
                System.out.println("Skipping SMS: Invalid Phone Number format.");
                return;
            }

            String msg = "Bank Alert: Token " + next.get().getTokenNumber() + ". Aapka turn aane wala hai!";
            
            try {
                // SID/Token ko init karna zaroori hai
                Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
                Message.creator(
                    new PhoneNumber(customerPhone), 
                    new PhoneNumber(TWILIO_PHONE), 
                    msg
                ).create();
                System.out.println("SMS successfully triggered for " + customerPhone);
            } catch (Exception e) {
                // Agar Twilio credentials galat hain toh ye error message print karega
                System.err.println("Twilio SMS failed (Check SID/Token): " + e.getMessage());
            }
        }
    }

    public Double getWaitTimeForCategory(String type) {
        Double avg = tokenRepo.getAverageWaitTimeByCategory(type);
        return (avg != null) ? avg : 5.0; 
    }
}