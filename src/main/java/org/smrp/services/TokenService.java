package org.smrp.services;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;

import javax.crypto.SecretKey;

import org.smrp.Constants;
import org.smrp.models.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;


@Singleton
public class TokenService {
    
    private final UserService userService;

    public TokenService(UserService userService) {
        this.userService = userService;
    }

    public Claims decodeToken(String tokenStr) {
        try {
            return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(tokenStr)
                .getPayload();
        } catch (Exception e) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Invalid Credentials");
        }
    }
    
    public String generateAccessToken(User user) {
        long val = 720L * 60 * 60 * 1000;
        Date expiryDate = new Date(System.currentTimeMillis() + val);
        HashMap<String, Object> claims = new HashMap<>(){
            {
                put("username", user.getUsername());
            }
        };

        // 2. Create and sign the JWT
        String token = Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(user.getId()))
                .expiration(expiryDate) // Sets the 'exp' claim
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
        return token;
    }

    public String generateRefreshToken(User user) {
        long val = 87600L * 60 * 60 * 1000;
        Date expiryDate = new Date(System.currentTimeMillis() + val);
        HashMap<String, Object> claims = new HashMap<>(){
            {
                put("username", user.getUsername());
            }
        };

        // 2. Create and sign the JWT
        String token = Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(user.getId()))
                .expiration(expiryDate) // Sets the 'exp' claim
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
        return token;
    }

    public Mono<User> resolveRefreshToken(String encoded) {
        Claims claims = decodeRefreshToken(encoded);
        Long id = Long.valueOf(claims.getSubject());
        return getUserFromRefreshTokenPayload(id);
    }

    public Mono<HashMap<String, Object>> createAccessTokenFromRefreshToken(String refresh) {
        return resolveRefreshToken(refresh).flatMap(user -> {
            if (user == null) {
                return Mono.error(new HttpStatusException(HttpStatus.UNAUTHORIZED, "Invalid Credentials"));
            }

            String token = generateAccessToken(user);
            String refreshToken = generateRefreshToken(user);
            HashMap<String, Object> result = new HashMap<>();
            result.put("user", user);
            result.put("token", token);
            result.put("refreshToken", refreshToken);
            return Mono.just(result);
        });
    }

    private Claims decodeRefreshToken(String tokenStr) {
        try {
            return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(tokenStr)
                .getPayload();
        } catch (Exception e) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Invalid Refresh Token");
        }
    }

    private Mono<User> getUserFromRefreshTokenPayload(Long id) {
        return userService.findById(id);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Constants.JWT_SECRET.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
