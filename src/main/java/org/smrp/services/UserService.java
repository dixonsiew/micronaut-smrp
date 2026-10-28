package org.smrp.services;

import java.util.List;

import org.mindrot.jbcrypt.BCrypt;
import org.smrp.models.Role;
import org.smrp.models.User;

import io.micronaut.data.r2dbc.operations.R2dbcOperations;
import io.r2dbc.spi.Connection;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class UserService {
    
    private final R2dbcOperations r2dbcOperations;
    
    public UserService(R2dbcOperations r2dbcOperations) {
        this.r2dbcOperations = r2dbcOperations;
    }

    public Mono<User> findById(Long id) {
        return Mono.from(r2dbcOperations.withConnection(
            connection -> Mono.from(connection.createStatement("select id, username, first_name, last_name, password, last_login from app_user where id = $1 limit 1")
                    .bind(0, id)
                    .execute())
            .flatMap(result ->
                Mono.from(result.map((row, metadata) -> User.fromRow(row)))
            )
        ));
    }

    public Mono<User> findByUsername(String username) {
        return Mono.from(r2dbcOperations.withConnection(
            connection -> Mono.from(connection.createStatement("select id, username, first_name, last_name, password, last_login from app_user where username = $1 limit 1")
                    .bind(0, username)
                    .execute())
            .flatMap(result ->
                Mono.from(result.map((row, metadata) -> User.fromRow(row)))
            )
        ));
    }

    public Flux<User> findAll(int offset, int limit, String sortBy, String sortDir) {
        String q = "select t.id, t.username, t.first_name, t.last_name, t.password, t.last_login from app_user t order by %s %s offset $1 limit $2";
        final String s = String.format(q, sortBy, sortDir);
        return Flux.from(r2dbcOperations.withConnection(
            connection -> Flux.from(connection.createStatement(s)
                    .bind(0, offset)
                    .bind(1, limit)
                    .execute())
            .flatMap(result ->
                result.map((row, metadata) -> User.fromRow(row))
            )
            .flatMap(user -> setRole(user, connection))
        ));
    }

    public Mono<Long> count() {
        return Mono.from(r2dbcOperations.withConnection(
            connection -> Mono.from(connection.createStatement("select count(id) from app_user").execute())
            .flatMap(result ->
                Mono.from(result.map((row, metadata) -> row.get(0, Long.class)))
            )
        ));
    }

    public Mono<Boolean> existsByOtherUsername(String username, Long id) {
        return Mono.from(r2dbcOperations.withConnection(
            connection -> Mono.from(connection.createStatement("select exists (select 1 from app_user t where t.username = $1 and t.id <> $2)")
                    .bind(0, username)
                    .bind(1, id)
                    .execute())
            .flatMap(result ->
                Mono.from(result.map((row, metadata) -> row.get(0, Boolean.class)))
            )
            .defaultIfEmpty(Boolean.FALSE)
        ));
    }

    public Mono<Boolean> existsByUsername(String username) {
        return Mono.from(r2dbcOperations.withConnection(
            connection -> Mono.from(connection.createStatement("select exists (select 1 from app_user t where t.username = $1)")
                    .bind(0, username)
                    .execute())
            .flatMap(result ->
                Mono.from(result.map((row, metadata) -> row.get(0, Boolean.class)))
            )
            .defaultIfEmpty(Boolean.FALSE)
        ));
    }

    public Flux<User> findByKeyword(String keyword, int offset, int limit, String sortBy, String sortDir) {
        String q = "select t.id, t.username, t.first_name, t.last_name, t.password, t.last_login from app_user t where (t.username ilike $1 or t.first_name ilike $2 or t.last_name ilike $3) order by %s %s offset $4 limit $5";
        final String s = String.format(q, sortBy, sortDir);
        return Flux.from(r2dbcOperations.withConnection(
            connection -> Flux.from(connection.createStatement(s)
                    .bind(0, keyword)
                    .bind(1, keyword)
                    .bind(2, keyword)
                    .bind(3, offset)
                    .bind(4, limit)
                    .execute())
            .flatMap(result ->
                result.map((row, metadata) -> User.fromRow(row))
            )
            .flatMap(user -> setRole(user, connection))
        ));
    }

    public Mono<Long> countByKeyword(String keyword) {
        return Mono.from(r2dbcOperations.withConnection(
            connection -> Mono.from(connection.createStatement("select count(id) from app_user t where (t.username ilike $1 or t.first_name ilike $2 or t.last_name ilike $3)")
                    .bind(0, keyword)
                    .bind(1, keyword)
                    .bind(2, keyword)
                    .execute())
            .flatMap(result ->
                Mono.from(result.map((row, metadata) -> row.get(0, Long.class)))
            )
        ));
    }

    public Mono<Void> save(User o) {
        String pwd = BCrypt.hashpw(o.getPassword(), BCrypt.gensalt());
        return Mono.from(r2dbcOperations.withTransaction(status -> {
            Connection connection = status.getConnection();

            Mono<Long> insertUserMono = Mono.from(connection.createStatement("insert into app_user (id, username, password, first_name, last_name, active) values(nextval('app_user_id_seq'),$1,$2,$3,$4,$5) returning id as app_user_id")
                    .bind(0, o.getUsername())
                    .bind(1, pwd)
                    .bind(2, o.getFirstName())
                    .bind(3, o.getLastName())
                    .bind(4, true)
                    .execute())
                .flatMap(result ->
                    Mono.from(result.map((row, metadata) -> row.get("app_user_id", Long.class)))
                );

            return insertUserMono.flatMap(id ->
                Flux.fromIterable(o.getRoles() == null ? List.of() : o.getRoles())
                    .concatMap(role -> 
                        Mono.from(connection.createStatement("insert into app_user_roles (app_user_id, roles_id) values($1, $2)")
                            .bind(0, id)
                            .bind(1, role.getId())
                            .execute())
                        .flatMap(result -> Mono.from(result.getRowsUpdated()))
                        .then()
                    )
                    .then()
            );
        })).then();

        /* return Mono.from(connectionFactory.create())
            .flatMap(connection -> {
                Mono<Long> insertUserMono =
                Mono.from(connection.createStatement("insert into app_user (id, username, password, first_name, last_name, active) values(nextval('app_user_id_seq'),$1,$2,$3,$4,$5) returning id as app_user_id")
                        .bind(0, o.getUsername())
                        .bind(1, pwd)
                        .bind(2, o.getFirstName())
                        .bind(3, o.getLastName())
                        .bind(4, true)
                        .execute())
                    .flatMap(result ->
                        Mono.from(result.map((row, metadata) -> row.get("app_user_id", Long.class)))
                    );

                return Mono.from(connection.beginTransaction())
                    .then(insertUserMono)
                    .flatMap(id -> 
                        Flux.fromIterable(o.getRoles() == null ? List.of() : o.getRoles())
                        .concatMap(role ->
                            Mono.from(connection.createStatement("insert into app_user_roles (app_user_id, roles_id) values($1, $2)")
                                    .bind(0, id)
                                    .bind(1, role.getId())
                                    .execute())
                                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                                .then()
                        )
                        .then()
                    )
                    .then(Mono.from(connection.commitTransaction()))
                    .onErrorResume(e ->
                        Mono.from(connection.rollbackTransaction())
                            .then(Mono.error(e)))
                    .doFinally(signal -> connection.close());
            })
            .then(); */
    }

    public Mono<Void> update(User o) {
        return Mono.from(r2dbcOperations.withTransaction(status -> {
            Connection connection = status.getConnection();

            Mono<Long> u1 = o.getPassword() != null && !o.getPassword().isEmpty() ?
                Mono.from(connection.createStatement("update app_user set password = $1, first_name = $2, last_name = $3 where id = $4")
                    .bind(0, BCrypt.hashpw(o.getPassword(), BCrypt.gensalt()))
                    .bind(1, o.getFirstName())
                    .bind(2, o.getLastName())
                    .bind(3, o.getId())
                    .execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated())) :
                Mono.from(connection.createStatement("update app_user set first_name = $1, last_name = $2 where id = $3")
                    .bind(0, o.getFirstName())
                    .bind(1, o.getLastName())
                    .bind(2, o.getId())
                    .execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()));

            Mono<Long> u2 = Mono.from(connection.createStatement("delete from app_user_roles where app_user_id = $1")
                .bind(0, o.getId())
                .execute())
            .flatMap(result -> Mono.from(result.getRowsUpdated()));

            Mono<Void> u3 = Flux.fromIterable(o.getRoles() == null ? List.of() : o.getRoles())
                .concatMap(role ->
                    Mono.from(connection.createStatement("insert into app_user_roles (app_user_id, roles_id) values($1,$2)")
                            .bind(0, o.getId())
                            .bind(1, role.getId())
                            .execute())
                    .flatMap(result -> Mono.from(result.getRowsUpdated()))
                    .then()
                )
                .then();

            return u1.then(u2).then(u3);
        })).then();
    }

    public Mono<Void> deleteById(Long id) {
        return Mono.from(r2dbcOperations.withTransaction(status -> {
            Connection connection = status.getConnection();

            return Mono.from(connection.createStatement("delete from app_user_roles where app_user_id = $1")
                    .bind(0, id)
                    .execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                .then(Mono.from(connection.createStatement("delete from app_user where id = $1")
                        .bind(0, id)
                        .execute())
                    .flatMap(result -> Mono.from(result.getRowsUpdated())))
                    .then();
        })).then();
    }

    public Mono<Void> updateLastLogin(Long id) {
        return Mono.from(r2dbcOperations.withTransaction(status -> {
            Connection connection = status.getConnection();

            return Mono.from(connection.createStatement("update app_user set last_login = now() where id = $1")
                    .bind(0, id)
                    .execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                .then();
        })).then();
    }

    public Mono<Void> updatePassword(User o) {
        String pwd = BCrypt.hashpw(o.getPassword(), BCrypt.gensalt());
        return Mono.from(r2dbcOperations.withTransaction(status -> {
            Connection connection = status.getConnection();

            return Mono.from(connection.createStatement("update app_user set password = $1 where id = $2")
                    .bind(0, pwd)
                    .bind(1, o.getId())
                    .execute())
                .flatMap(result -> Mono.from(result.getRowsUpdated()))
                .then();
        })).then();
    }

    public boolean validateCredentials(User user, String password) {
        return BCrypt.checkpw(password, user.getPassword());
    }

    private Mono<User> setRole(User o, Connection connection) {
        return Flux.from(connection.createStatement("select r.id, r.name from app_user_roles aur inner join role r on aur.roles_id = r.id where aur.app_user_id = $1")
                .bind(0, o.getId())
                .execute())
            .flatMap(result -> result.map((row, metadata) -> Role.fromRow(row)))
            .collectList()
            .map(items -> {
                o.setRoles(items);
                return o;
            });
    }
}
