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
        return Flux.usingWhen(
            connectionFactory.create(),
            connection -> Flux.from(connection.createStatement(s)
                    .execute())
                .flatMap(result ->
                    result.map((row, metadata) -> Role.fromRow(row))
                ),
            connection -> Mono.from(connection.close()),
            (connection, err) -> Mono.from(connection.close()),
            connection -> Mono.from(connection.close())
        );
    }

    public Mono<Role> findById(Long id) {
        return Mono.usingWhen(
            connectionFactory.create(),
            connection ->
                Mono.from(connection.createStatement("select id, name from role where id = $1 limit 1")
                    .bind(0, id)
                    .execute())
                .flatMap(result ->
                    Mono.from(result.map((row, metadata) -> Role.fromRow(row)))
                ),
            connection -> Mono.from(connection.close()),
            (connection, err) -> Mono.from(connection.close()),
            connection -> Mono.from(connection.close())
        );
    }
}
