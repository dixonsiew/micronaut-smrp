package org.smrp.filters;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.filters.AuthenticationFetcher;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.reactivestreams.Publisher;


@Singleton
public class AuthFetcher implements AuthenticationFetcher<HttpRequest<?>> {

    private final CustomAuthProvider authenticationProvider;

    public AuthFetcher(CustomAuthProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public Publisher<Authentication> fetchAuthentication(HttpRequest<?> request) {
        String token = request.getHeaders().get("Authorization");
        if (token == null || token.isEmpty()) {
            Cookie cookieToken = request.getCookies().get("token");
            if (cookieToken == null) {
                return Mono.empty();
            }

            token = cookieToken.getValue();
            if (token.isEmpty()) {
                return Mono.empty();
            }
        }

        if (token.startsWith("Bearer")) {
            token = token.replace("Bearer ", "").replace("Bearer", "");
            if (token.isEmpty()) {
                Cookie cookieToken = request.getCookies().get("token");
                if (cookieToken == null) {
                    return Mono.empty();
                }
                
                token = cookieToken.getValue();
                if (token.isEmpty()) {
                    return Mono.empty();
                }
            }
        }

        String tokenStr = token.trim();
        CustomTokenRequest tokenRequest = new CustomTokenRequest(tokenStr);
        Flux<AuthenticationResponse> responses = Flux.from(authenticationProvider.authenticate(request, tokenRequest));
        return responses.switchMap(response -> {
            if (response.isAuthenticated() && response.getAuthentication().isPresent()) {
                return Mono.just(response.getAuthentication().get());
            }
            return Mono.empty();
        });
    }
}
