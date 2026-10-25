package org.smrp.controllers.auth;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.smrp.dtos.ChangePasswordDto;
import org.smrp.services.UserService;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.utils.SecurityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.micronaut.validation.Validated;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import reactor.core.publisher.Mono;


@Tag(name = "Auth")
@Secured(SecurityRule.IS_AUTHENTICATED)
@SecurityRequirement(name="BearerAuth")
@Controller("/api")
@Validated
public class CurrentUser {

    private final UserService userService;
    private final Validator validator;
    private final SecurityService securityService;
    
    public CurrentUser(UserService userService, Validator validator, SecurityService securityService) {
        this.userService = userService;
        this.validator = validator;
        this.securityService = securityService;
    }

    @Get("/current-user")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<?>> userDetails() {
        String username = securityService.getAuthentication().map(auth -> auth.getName()).orElse("");
        return userService.findByUsername(username)
            .switchIfEmpty(Mono.error(new HttpStatusException(HttpStatus.NOT_FOUND, "User not found")))
            .flatMap(user -> {
                Map<String, Object> mx = new LinkedHashMap<>();
                mx.put("id", user.getId());
                mx.put("username", user.getUsername());
                mx.put("first_name", user.getFirstName());
                mx.put("last_name", user.getLastName());
                mx.put("roles", user.getRoles());
                return Mono.just(HttpResponse.ok(mx));
            });
    }

    @Post("/change-password")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<?>> changePassword(@Valid @Body ChangePasswordDto data) {
        Set<ConstraintViolation<ChangePasswordDto>> violations = validator.validate(data);
        if (!violations.isEmpty()) {
            return Mono.error(new ConstraintViolationException(violations));
        }

        if (!data.password().equals(data.confirmPassword())) {
            return Mono.error(new HttpStatusException(HttpStatus.BAD_REQUEST, "Confirm Password does not match"));
        }
        
        String username = securityService.getAuthentication().map(auth -> auth.getName()).orElse("");
        return userService.findByUsername(username)
            .switchIfEmpty(Mono.error(new HttpStatusException(HttpStatus.NOT_FOUND, "User not found")))
            .flatMap(user -> {
                user.setPassword(data.password());
                HttpResponse<?> response = HttpResponse.ok(Map.of("success", 1));
                return userService.updatePassword(user)
                    .thenReturn(response);
            });
    }
}
