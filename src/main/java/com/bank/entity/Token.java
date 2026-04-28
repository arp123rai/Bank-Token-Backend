package com.bank.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@Table(name = "token")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tokenNumber;   // C-1, L-1, etc.
    private String serviceType;   // CASH, LOAN
    private String userPhone;
    private String status;        // WAITING, SERVING, COMPLETED
    private Integer counterNumber = 0; // Counter 1, 2, 3

    // FIX: Dono variables ko sahi column se map karein
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "called_at") // Pehle yahan 'created_at' likha tha, ise 'called_at' kar diya
    private LocalDateTime calledAt;

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(String tokenNumber) { this.tokenNumber = tokenNumber; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCounterNumber() { return counterNumber; }
    public void setCounterNumber(Integer counterNumber) { this.counterNumber = counterNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCalledAt() { return calledAt; }
    public void setCalledAt(LocalDateTime calledAt) { this.calledAt = calledAt; }
}