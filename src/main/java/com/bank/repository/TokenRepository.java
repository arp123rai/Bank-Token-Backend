package com.bank.repository;

import com.bank.entity.Token;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
	
    // 1. Prefix sequence generate karne ke liye (C-1, L-1 logic)
    long countByServiceTypeAndCreatedAtAfter(String serviceType, LocalDateTime date);
    
    // 2. Multi-Counter Call: Category ke hisab se agla banda dhundne ke liye
    Optional<Token> findFirstByServiceTypeAndStatusOrderByCreatedAtAsc(String serviceType, String status);
    
    // 3. Aaj ke total tokens ka count
    long countByCreatedAtAfter(LocalDateTime time);

    /**
     * MANAGER DASHBOARD: FULL DAY TRAFFIC (10 AM TO 5 PM)
     */
    @Query(value = "SELECT h.hour, COUNT(t.id) as count " +
                   "FROM (SELECT 10 AS hour UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 " +
                   "      UNION SELECT 14 UNION SELECT 15 UNION SELECT 16 UNION SELECT 17) h " +
                   "LEFT JOIN token t ON HOUR(t.created_at) = h.hour AND DATE(t.created_at) = CURDATE() " +
                   "GROUP BY h.hour ORDER BY h.hour", nativeQuery = true)
    List<Object[]> getFullDayTraffic();

    /**
     * MANAGER DASHBOARD: SERVICE DISTRIBUTION (CASH vs LOAN)
     */
    @Query(value = "SELECT service_type, COUNT(*) FROM token " +
                   "WHERE DATE(created_at) = CURDATE() GROUP BY service_type", nativeQuery = true)
    List<Object[]> getServiceDistribution();

    /**
     * USER DASHBOARD: CATEGORY-SPECIFIC WAIT TIME
     * FIX: Isme @Param use kiya hai taaki 'category' property wala error na aaye.
     * CASH aur LOAN ka time mix nahi hoga.
     */
    @Query(value = "SELECT IFNULL(AVG(TIMESTAMPDIFF(MINUTE, created_at, called_at)), 5.0) " +
            "FROM token " +
            "WHERE status != 'WAITING' " +
            "AND called_at IS NOT NULL " +
            "AND service_type = :type " + 
            "AND DATE(created_at) = CURDATE() " +
            "AND id IN (SELECT id FROM (SELECT id FROM token WHERE service_type = :type AND status != 'WAITING' ORDER BY called_at DESC LIMIT 10) as t)", 
    nativeQuery = true)
Double getAverageWaitTimeByCategory(@Param("type") String type);

    // Old method if still needed for overall average
    @Query(value = "SELECT AVG(TIMESTAMPDIFF(MINUTE, created_at, called_at)) " +
                   "FROM token WHERE status != 'WAITING' " +
                   "ORDER BY called_at DESC LIMIT 5", nativeQuery = true)
    Double getAverageWaitTime();
    
    
 // TokenRepository.java
    @Query(value = "SELECT DAYNAME(created_at) as day, COUNT(*) as count " +
                   "FROM token WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                   "GROUP BY DAYNAME(created_at) ORDER BY created_at ASC", nativeQuery = true)
    List<Object[]> getWeeklyStats();
    
    
    
    @Query(value = "SELECT AVG(TIMESTAMPDIFF(MINUTE, created_at, called_at)) FROM token " +
            "WHERE status = 'SERVING' AND DATE(created_at) = CURDATE()", nativeQuery = true)
Double getTodayAverageWaitTime();
}