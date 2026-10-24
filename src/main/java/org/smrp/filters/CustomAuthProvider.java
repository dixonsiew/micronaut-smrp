package org.smrp.filters;

import java.util.List;
import java.util.Map;

import org.smrp.services.TokenService;

import io.jsonwebtoken.Claims;

import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.provider.ReactiveAuthenticationProvider;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

import org.reactivestreams.Publisher;


@Singleton
public class CustomAuthProvider implements ReactiveAuthenticationProvider<Object, String, String> {

    private final TokenService tokenService;
    
    public CustomAuthProvider(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Publisher<AuthenticationResponse> authenticate(
            Object requestContext,
            AuthenticationRequest<String, String> authenticationRequest) {
        if (authenticationRequest instanceof CustomTokenRequest customTokenRequest) {
            String tokenStr = customTokenRequest.getIdentity();
            Claims claims = tokenService.decodeToken(tokenStr);
            String sub = claims.getSubject();
            String username = (String) claims.get("username");
            Map<String, Object> attributes = Map.of(
                "id", Long.valueOf(sub)
            );
            return Mono.just(AuthenticationResponse.success(username, List.of("ROLE_USER"), attributes));
        } else {
            return Mono.just(AuthenticationResponse.failure(AuthenticationFailureReason.UNKNOWN));
        }
    }
}
