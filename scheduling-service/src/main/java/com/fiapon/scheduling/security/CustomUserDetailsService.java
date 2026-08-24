package com.fiapon.scheduling.security;

import com.fiapon.scheduling.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    public CustomUserDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return repository.findByUsername(username)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
