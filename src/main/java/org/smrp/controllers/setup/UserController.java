package org.smrp.controllers.setup;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.smrp.Constants;
import org.smrp.dtos.KeywordDto;
import org.smrp.dtos.UserDto;
import org.smrp.models.Pager;
import org.smrp.models.User;
import org.smrp.services.RoleService;
import org.smrp.services.UserService;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.validation.Validated;
import io.micronaut.validation.validator.Validator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;


@Tag(name = "Setup/User")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/api")
@Validated
public class UserController {
    
    private final UserService userService;
    private final RoleService roleService;
    private final Validator validator;

    public UserController(UserService userService, RoleService roleService, Validator validator) {
        this.userService = userService;
        this.roleService = roleService;
        this.validator = validator;
    }

    @Get("/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<Flux<User>>> list(
        @QueryValue(value = "_page", defaultValue = "1") Integer page,
        @QueryValue(value = "_limit", defaultValue = "10") Integer limit,
        @Nullable @QueryValue(value = "sort", defaultValue = "") String sorts
    ) {
        String sortBy = "code";
        String sortDir = "asc";

        if (sorts != null && !sorts.isEmpty()) {
            String[] lis = sorts.split("\\$");
            String s = lis[0];
            String[] arr = s.split(":");
            sortBy = arr[0];
            sortDir = arr[1];
        }

        final String fsortBy = sortBy;
        final String fsortDir = sortDir;

        return userService.count().map(total -> {
            Pager pg = Pager.from(total, page, limit);
            Flux<User> data = userService.findAll(pg.getLowerBound(), pg.getPageSize(), fsortBy, fsortDir);
            return HttpResponse.ok(data)
                .header(Constants.X_TOTAL_COUNT, String.valueOf(total))
                .header(Constants.X_TOTAL_PAGE, String.valueOf(pg.getTotalPages()));
        });
    }

    @Post("/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<Flux<User>>> searchList(
        @Valid @Body KeywordDto data,
        @QueryValue(value = "_page", defaultValue = "1") Integer page,
        @QueryValue(value = "_limit", defaultValue = "10") Integer limit,
        @Nullable @QueryValue(value = "sort", defaultValue = "") String sorts
    ) {
        String sortBy = "username";
        String sortDir = "asc";

        if (sorts != null && !sorts.isEmpty()) {
            String[] lis = sorts.split("\\$");
            String s = lis[0];
            String[] arr = s.split(":");
            sortBy = arr[0];
            sortDir = arr[1];
        }

        final String key = String.format("%%%s%%", data.getKeyword());
        final String fsortBy = sortBy;
        final String fsortDir = sortDir;

        return userService.countByKeyword(key).map(total -> {
            Pager pg = Pager.from(total, page, limit);
            Flux<User> items = userService.findByKeyword(key, pg.getLowerBound(), pg.getPageSize(), fsortBy, fsortDir);
            return HttpResponse.ok(items)
                .header(Constants.X_TOTAL_COUNT, String.valueOf(total))
                .header(Constants.X_TOTAL_PAGE, String.valueOf(pg.getTotalPages()));
        });
    }

    @Post("/user")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<?>> create(@Valid @Body UserDto data) {
        Set<ConstraintViolation<UserDto>> violations = validator.validate(data);
        if (!violations.isEmpty()) {
            return Mono.error(new ConstraintViolationException(violations));
        }
        
        return userService.existsByUsername(data.username()).flatMap(b -> {
            if (b) {
                return Mono.error(new HttpStatusException(HttpStatus.BAD_REQUEST, "A user with that username already exists"));
            }

            return roleService.findById(data.roleId())
                .switchIfEmpty(Mono.error(new HttpStatusException(HttpStatus.NOT_FOUND, "Role not found")))
                .flatMap(role -> {
                    User o = new User();
                    o.setUsername(data.username());
                    o.setPassword(data.password());
                    o.setFirstName(data.firstname());
                    o.setLastName(data.lastname());
                    o.setRoles(List.of(role));
                    HttpResponse<?> response = HttpResponse.ok(Map.of("success", 1));
                    return userService.save(o)
                        .thenReturn(response);
                });
        });
    }

    @Get("/user/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<User> edit(Long id) {
        return userService.findById(id)
            .switchIfEmpty(Mono.error(new HttpStatusException(HttpStatus.NOT_FOUND, "Record not found")));
    }

    @Put("/user/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<?>> update(Long id, @Valid @Body UserDto data) {
        Set<ConstraintViolation<UserDto>> violations = validator.validate(data);
        if (!violations.isEmpty()) {
            return Mono.error(new ConstraintViolationException(violations));
        }
        
        String password = data.password();
        return userService.existsByOtherUsername(data.username(), id).flatMap(b -> {
            if (b) {
                return Mono.error(new HttpStatusException(HttpStatus.BAD_REQUEST, "A user with that username already exists"));
            }

            return roleService.findById(data.roleId())
                .switchIfEmpty(Mono.error(new HttpStatusException(HttpStatus.NOT_FOUND, "Role not found")))
                .flatMap(role -> {
                    User o = new User();
                    o.setId(id);
                    o.setUsername(data.username());
                    o.setPassword(!"********".equals(password) ? password : "");
                    o.setFirstName(data.firstname());
                    o.setLastName(data.lastname());
                    o.setRoles(List.of(role));
                    HttpResponse<?> response = HttpResponse.ok(Map.of("success", 1));
                    return userService.update(o)
                        .thenReturn(response);
                });
        });
    }

    @Delete("/user/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<?>> delete(Long id) {
        HttpResponse<?> response = HttpResponse.ok(Map.of("success", 1));
        return userService.deleteById(id)
            .thenReturn(response);
    }
}
