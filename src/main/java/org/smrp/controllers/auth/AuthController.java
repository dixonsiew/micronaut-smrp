package org.smrp.controllers.auth;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.smrp.dtos.LoginDto;
import org.smrp.dtos.RefreshTokenDto;
import org.smrp.services.TokenService;
import org.smrp.services.UserService;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.CookieValue;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.SameSite;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;
import io.micronaut.validation.validator.Validator;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;

import reactor.core.publisher.Mono;


@Tag(name = "Auth")
@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/o")
@SecurityRequirement(name="")
@Validated
public class AuthController {
    
    private final TokenService tokenService;
    private final UserService userService;
    private final Validator validator;
    
    public AuthController(TokenService tokenService, UserService userService, Validator validator) {
        this.tokenService = tokenService;
        this.userService = userService;
        this.validator = validator;
    }

    @Post("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<?>> logout() {
        Cookie cookie = createCookie("token", "", "/", 0);
        Cookie refreshCookie = createCookie("refreshToken", "", "/smrp/o/refresh-token", 0);
        return Mono.just(HttpResponse.ok(Map.of("message", "Logged out successfully"))
            .cookie(cookie)
            .cookie(refreshCookie));
    }

    @Post("/token")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<?>> login(@Valid @Body LoginDto data) {
        Set<ConstraintViolation<LoginDto>> violations = validator.validate(data);
        if (!violations.isEmpty()) {
            return Mono.error(new ConstraintViolationException(violations));
        }

        HttpStatusException ex = new HttpStatusException(HttpStatus.UNAUTHORIZED, "Invalid Credentials");
        return userService.findByUsername(data.username())
            .switchIfEmpty(Mono.error(ex))
            .flatMap(user -> {
                boolean valid = userService.validateCredentials(user, data.password());
                if (!valid) {
                    return Mono.error(ex);
                }
                
                return userService.updateLastLogin(user.getId())
                    .onErrorResume(e -> Mono.empty())
                    .then(Mono.fromSupplier(() -> {
                        String token = tokenService.generateAccessToken(user);
                        String refreshToken = tokenService.generateRefreshToken(user);

                        Map<String, String> mx = new LinkedHashMap<>();
                        mx.put("type", "bearer");
                        mx.put("token", token);
                        mx.put("refresh_token", refreshToken);

                        Cookie cookie = createCookie("token", token, "/", 30 * 24 * 60 * 60);
                        Cookie refreshCookie = createCookie("refreshToken", refreshToken, "/smrp/o/refresh-token", 3650 * 24 * 60 * 60);
                        return HttpResponse.ok(mx)
                            .cookie(cookie)
                            .cookie(refreshCookie);
                    }));
            });
    }

    @Post("/refresh-token")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<?>> refresh(@Valid @Body RefreshTokenDto data, @CookieValue("refreshToken") String cookieToken) {
        HttpStatusException ex = new HttpStatusException(HttpStatus.UNAUTHORIZED, "Invalid Credentials");
        String refreshToken = cookieToken;
        if (cookieToken == null || "".equals(cookieToken)) {
            refreshToken = data.refreshToken();
        }

        if (refreshToken == null || "".equals(refreshToken)) {
            return Mono.error(ex);
        }

        return tokenService.createAccessTokenFromRefreshToken(refreshToken).flatMap(md -> {
            String token = (String) md.get("token");
            String rtoken = (String) md.get("refreshToken");
            
            Map<String, String> mx = new LinkedHashMap<>();
            mx.put("type", "bearer");
            mx.put("token", token);
            mx.put("refresh_token", rtoken);

            Cookie cookie = createCookie("token", token, "/", 30 * 24 * 60 * 60);
            Cookie refreshCookie = createCookie("refreshToken", rtoken, "/smrp/o/refresh-token", 3650 * 24 * 60 * 60);
            return Mono.just(HttpResponse.ok(mx)
                .cookie(cookie)
                .cookie(refreshCookie));
        });
    }

    private Cookie createCookie(String name, String value, String path, int maxAge) {
        return Cookie.of(name, value)
            .httpOnly(true)
            .secure(false)
            .sameSite(SameSite.Lax)
            .path(path)
            .maxAge(maxAge);
    }
}
