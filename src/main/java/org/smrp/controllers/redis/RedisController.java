package org.smrp.controllers.redis;

import org.smrp.controllers.redis.models.Increment;
import org.smrp.services.IncrementService;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Put;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Tag(name = "Redis")
@Secured(SecurityRule.IS_ANONYMOUS)
@SecurityRequirement(name = "")
@Controller("/redis")
public class RedisController {

    private final IncrementService incrementService;

    public RedisController(IncrementService incrementService) {
        this.incrementService = incrementService;
    }

    @Get
    public Flux<String> keys() {
        return incrementService.keys();
    }

    @Post
    public Mono<Void> create(@Body Increment increment) {
        return incrementService.set(increment.getKey(), increment.getValue());
    }

    @Get("/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<Increment> get(String key) {
        return incrementService.get(key).map(value -> new Increment(key, value));
    }

    @Put("/{key}")
    public Mono<Void> increment(String key, Long value) {
        return incrementService.increment(key, value);
    }

    @Delete("/{key}")
    public Mono<Void> delete(String key) {
        return incrementService.del(key);
    }
}
