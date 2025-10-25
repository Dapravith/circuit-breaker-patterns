package com.circuitbreaker.circuit_breaker_patterns.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "true")
public class SecurityConfig {

    @Value("${app.security.user:admin}")
    private String username;

    @Value("${app.security.password:admin}")
    private String password;

    @Bean
    public UserDetailsService users() {
        return new InMemoryUserDetailsManager(User.withDefaultPasswordEncoder()
                .username(username)
                .password(password)
                .roles("USER")
                .build());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/index.html", "/static/**", "/css/**", "/js/**", "/actuator/health", "/actuator/**").permitAll()
                    .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}

