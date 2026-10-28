package org.smrp.services;

import org.smrp.models.Role;

import io.micronaut.data.r2dbc.operations.R2dbcOperations;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Singleton
public class RoleService {
    
    private final R2dbcOperations r2dbcOperations;
    
    public RoleService(R2dbcOperations r2dbcOperations) {
        this.r2dbcOperations = r2dbcOperations;
    }

    public Flux<Role> findAll(String sortBy, String sortDir) {
        String q = "select id, name from role order by %s %s";
        final String s = String.format(q, sortBy, sortDir);
        return Flux.from(r2dbcOperations.withConnection(
            connection -> Flux.from(connection.createStatement(s)
                    .execute())
            .flatMap(result ->
                result.map((row, metadata) -> Role.fromRow(row))
            )
        ));
    }

    public Mono<Role> findById(Long id) {
        return Mono.from(r2dbcOperations.withConnection(
            connection -> Mono.from(connection.createStatement("select id, name from role where id = $1 limit 1")
                    .bind(0, id)
                    .execute())
            .flatMap(result ->
                Mono.from(result.map((row, metadata) -> Role.fromRow(row)))
            )
        ));
    }
}
