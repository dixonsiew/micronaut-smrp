package org.smrp.services;

import org.smrp.models.Role;

import io.micronaut.data.r2dbc.connection.DefaultR2dbcReactorConnectionOperations;

import jakarta.inject.Singleton;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Singleton
public class RoleService {
    
    private final DefaultR2dbcReactorConnectionOperations conn;
    
    public RoleService(DefaultR2dbcReactorConnectionOperations conn) {
        this.conn = conn;
    }

    public Flux<Role> findAll(String sortBy, String sortDir) {
        String q = "select id, name from role order by %s %s";
        final String s = String.format(q, sortBy, sortDir);
        return conn.withConnectionFlux(status ->
            Flux.from(status.getConnection().createStatement(s)
                    .execute())
                .flatMap(result ->
                    result.map((row, metadata) -> Role.fromRow(row))
                )
        );
    }

    public Mono<Role> findById(Long id) {
        final String s = "select id, name from role where id = $1 limit 1";
        return conn.withConnectionMono(status ->
            Mono.from(status.getConnection().createStatement(s)
                    .bind(0, id)
                    .execute())
                .flatMap(result ->
                    Mono.from(result.map((row, metadata) -> Role.fromRow(row)))
                )
        );
    }
}
