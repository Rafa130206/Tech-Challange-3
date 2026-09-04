package com.fiapon.auth.controller;

import com.fiapon.auth.dto.TokenResponse;
import com.fiapon.auth.service.JwtTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtTokenService jwtTokenService;

    public AuthController(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/token")
    @ResponseStatus(HttpStatus.OK)
    public TokenResponse token(Authentication authentication) {
        return jwtTokenService.issue(authentication.getName());
    }
}
