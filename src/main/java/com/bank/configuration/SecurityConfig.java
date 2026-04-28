package com.bank.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        .cors(Customizer.withDefaults()) 
	        .csrf(csrf -> csrf.disable())  
	        // Session management ko upar rakhein
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        
	        .authorizeHttpRequests(auth -> auth
	            // 1. Sabse pehle permitAll waale
	        		// SecurityConfig.java mein permitAll waala section update karein
	        		.requestMatchers("/api/auth/**", "/ws-bank/**").permitAll()
	        		.requestMatchers("/api/tokens/generate/**").permitAll()
	        		// Ise exact aise likhein:
	        		.requestMatchers("/api/tokens/avg-wait/{type}").permitAll() 
	        		.requestMatchers("/api/tokens/avg-wait/**").permitAll()
	        	    .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

	        	    // 2. Roles verification
	        	    .requestMatchers("/api/tokens/call-next/**").hasRole("STAFF")
	        	    .requestMatchers("/api/tokens/stats/**").hasRole("MANAGER")
	        	    
	        	    .anyRequest().authenticated()
	        )
	        // 🔥 FIX: httpBasic ko sirf enable rakhein, disable waali line hata dein
	        .httpBasic(Customizer.withDefaults()); 

	    return http.build();
	}
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS Configuration Bean
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173")); // React URL
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}