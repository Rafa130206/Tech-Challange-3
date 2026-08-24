package com.fiapon.scheduling.exception;

public class UsernameNotFoundException extends RuntimeException {
    public UsernameNotFoundException(String username) {
        super("User not found: " + username);
    }
}
