package com.fiapon.auth.service;

import com.fiapon.auth.config.AuthProperties;
import com.fiapon.auth.dto.TokenResponse;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenService {

    private final AuthProperties properties;

    public JwtTokenService(AuthProperties properties) {
        this.properties = properties;
    }

    public TokenResponse issue(String username) {
        AuthProperties.Account account = properties.findAccount(username);
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(properties.getTokenTtlSeconds());

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(account.getSubject())
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .claim("roles", List.of(account.getRole()))
                .build();

        try {
            SignedJWT token = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(),
                    claims
            );
            token.sign(new MACSigner(properties.getJwtSecret().getBytes(StandardCharsets.UTF_8)));
            return new TokenResponse(token.serialize(), "Bearer", properties.getTokenTtlSeconds());
        } catch (JOSEException exception) {
            throw new IllegalStateException("Nao foi possivel gerar o token JWT", exception);
        }
    }
}
