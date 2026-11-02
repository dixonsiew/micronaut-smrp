package org.smrp.services;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Singleton
public class IncrementService {
    
    private final RedisReactiveCommands<String, String> redis;

    public IncrementService(StatefulRedisConnection<String, String> redis) {
        this.redis = redis.reactive();
    }

    public Mono<Long> get(String key) {
        return redis.get(key).map(Long::parseLong).defaultIfEmpty(0L);
    }

    public Mono<Void> set(String key, Long value) {
        return redis.set(key, String.valueOf(value)).then();
    }

    public Mono<Void> increment(String key, Long incrementBy) {
        return redis.incrby(key, incrementBy).then();
    }

    public Mono<Void> del(String key) {
        return redis.del(key).then();
    }

    public Flux<String> keys() {
        return redis.keys("*");
    }
}
