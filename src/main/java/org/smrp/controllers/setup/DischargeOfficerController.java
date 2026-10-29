package org.smrp.controllers.setup;

import java.util.Map;
import java.util.Set;

import org.smrp.Constants;
import org.smrp.dtos.CommonSetupDto;
import org.smrp.dtos.KeywordDto;
import org.smrp.models.CommonSetup;
import org.smrp.models.Pager;
import org.smrp.services.CommonSetupService;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.utils.SecurityService;
import io.micronaut.validation.Validated;
import io.micronaut.validation.validator.Validator;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;


@Tag(name = "Setup/DischargeOfficer")
@Secured(SecurityRule.IS_AUTHENTICATED)
@SecurityRequirement(name = "BearerAuth")
@Controller("/api")
@Validated
public class DischargeOfficerController {
    
    private final CommonSetupService commonSetupService;
    private final String table = "discharge_officer";
    private final Validator validator;
    private final SecurityService securityService;
    
    public DischargeOfficerController(CommonSetupService commonSetupService, Validator validator, SecurityService securityService) {
        this.commonSetupService = commonSetupService;
        this.validator = validator;
        this.securityService = securityService;
    }

    @Get("/lookup/discharge-officers")
    @Produces(MediaType.APPLICATION_JSON)
    public Flux<CommonSetup> lookupList() {
        return commonSetupService.findAll(table, 0, 0, "", "");
    }

    @Get("/discharge-officers")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<Flux<CommonSetup>>> list(
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

        return commonSetupService.count(table).map(total -> {
            Pager pg = Pager.from(total, page, limit);
            Flux<CommonSetup> items = commonSetupService.findAll(table, pg.getLowerBound(), pg.getPageSize(), fsortBy, fsortDir);
            return HttpResponse.ok(items)
                .header(Constants.X_TOTAL_COUNT, String.valueOf(total))
                .header(Constants.X_TOTAL_PAGE, String.valueOf(pg.getTotalPages()));
        });
    }

    @Post("/discharge-officers")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<?>> searchList(
        @Valid @Body KeywordDto data,
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

        final String key = String.format("%%%s%%", data.getKeyword());
        final String fsortBy = sortBy;
        final String fsortDir = sortDir;

        return commonSetupService.countByKeyword(key, table).map(total -> {
            Pager pg = Pager.from(total, page, limit);
            Flux<CommonSetup> items = commonSetupService.findByKeyword(key, pg.getLowerBound(), pg.getPageSize(), fsortBy, fsortDir, table);
            return HttpResponse.ok(items)
                .header(Constants.X_TOTAL_COUNT, String.valueOf(total))
                .header(Constants.X_TOTAL_PAGE, String.valueOf(pg.getTotalPages()));
        });
    }

    @Post("/discharge-officer")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<?>> create(@Valid @Body CommonSetupDto data) {
        Set<ConstraintViolation<CommonSetupDto>> violations = validator.validate(data);
        if (!violations.isEmpty()) {
            return Mono.error(new ConstraintViolationException(violations));
        }

        Map<String, Object> attributes = securityService.getAuthentication().map(auth -> auth.getAttributes()).orElse(Map.of());
        Long userId = (Long) attributes.get("id");
        CommonSetup o = new CommonSetup();
        o.setCode(data.code());
        o.setDesc(data.desc());
        o.setRef(data.ref());
        o.setCreatedBy(userId);
        HttpResponse<?> response = HttpResponse.ok(Map.of("success", 1));
        return commonSetupService.save(o, table)
            .thenReturn(response);
    }

    @Get("/discharge-officer/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<CommonSetup> edit(Long id) {
        return commonSetupService.findById(table, id)
            .switchIfEmpty(Mono.error(new HttpStatusException(HttpStatus.NOT_FOUND, "Record not found")));
    }

    @Put("/discharge-officer/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<?>> update(Long id, @Valid @Body CommonSetupDto data) {
        Set<ConstraintViolation<CommonSetupDto>> violations = validator.validate(data);
        if (!violations.isEmpty()) {
            return Mono.error(new ConstraintViolationException(violations));
        }

        Map<String, Object> attributes = securityService.getAuthentication().map(auth -> auth.getAttributes()).orElse(Map.of());
        Long userId = (Long) attributes.get("id");
        return commonSetupService.findById(table, id)
            .switchIfEmpty(Mono.error(new HttpStatusException(HttpStatus.NOT_FOUND, "Record not found")))
            .flatMap(item -> {
                item.setCode(data.code());
                item.setDesc(data.desc());
                item.setRef(data.ref());
                item.setModifiedBy(userId);
                HttpResponse<?> response = HttpResponse.ok(Map.of("success", 1));
                return commonSetupService.update(item, table)
                    .thenReturn(response);
            });
    }

    @Delete("/discharge-officer/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<?>> delete(Long id) {
        Map<String, Object> attributes = securityService.getAuthentication().map(auth -> auth.getAttributes()).orElse(Map.of());
        Long userId = (Long) attributes.get("id");
        HttpResponse<?> response = HttpResponse.ok(Map.of("success", 1));
        return commonSetupService.deleteById(id, userId, table)
            .thenReturn(response);
    }
}
