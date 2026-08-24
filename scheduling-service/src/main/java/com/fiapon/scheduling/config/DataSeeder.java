package com.fiapon.scheduling.config;

import com.fiapon.scheduling.model.UserRole;
import com.fiapon.scheduling.model.User;
import com.fiapon.scheduling.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }
            userRepository.save(new User("dr.silva", passwordEncoder.encode("doctor123"), UserRole.DOCTOR));
            userRepository.save(new User("nurse.souza", passwordEncoder.encode("nurse123"), UserRole.NURSE));
            userRepository.save(new User("patient.jose", passwordEncoder.encode("patient123"), UserRole.PATIENT));
        };
    }
}