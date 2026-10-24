package org.smrp.services;

import org.smrp.models.Role;

import io.r2dbc.spi.ConnectionFactory;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Singleton
public class RoleService {
    
    private final ConnectionFactory connectionFactory;
    
    public RoleService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Flux<Role> findAll(String sortBy, String sortDir) {
        String q = "select id, name from role order by %s %s";
        final String s = String.format(q, sortBy, sortDir);
        return Mono.from(connectionFactory.create())
            .flatMapMany(connection -> 
                Flux.from(connection.createStatement(s)
                    .execute())
                    .flatMap(result -> Flux.from(result.map((row, metadata) -> Role.fromRow(row))))
                    .doFinally(signal -> connection.close())
            );
    }

    public Mono<Role> findById(Long id) {
        String q = "select id, name from role where id = $1 limit 1";
        return Mono.from(connectionFactory.create())
            .flatMap(connection ->
                Mono.from(connection.createStatement(q)
                    .bind("$1", id)
                    .execute())
                    .flatMap(result -> Mono.from(result.map((row, metadata) -> Role.fromRow(row))))
                    .doFinally(signal -> connection.close())
            );
    }
}
