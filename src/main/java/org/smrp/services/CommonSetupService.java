package org.smrp.services;

import org.smrp.models.CommonSetup;

import io.r2dbc.spi.ConnectionFactory;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Singleton 
public class CommonSetupService {
    
    private final ConnectionFactory connectionFactory;

    public CommonSetupService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Mono<CommonSetup> findById(String table, Long id) {
        String q = "select id, code, \"desc\", ref, created_by, created_date, modified_by, modified_date, deleted, deleted_by, deleted_date from %s where id = $1 limit 1";
        final String s = String.format(q, table);
        return Mono.from(connectionFactory.create())
            .flatMap(connection ->
                Mono.from(connection.createStatement(s)
                    .bind("$1", id)
                    .execute())
                    .flatMap(result -> Mono.from(result.map((row, metadata) -> CommonSetup.fromRow(row))))
                    .doFinally(signal -> connection.close())
            );
    }

    public Mono<CommonSetup> findByDesc(String desc, String table) {
        String q = "select t.id, t.code, t.desc, t.ref, t.created_by, t.created_date, t.modified_by, t.modified_date, t.deleted, t.deleted_by, t.deleted_date from %s t where lower(t.desc) = lower($1) limit 1";
        final String s = String.format(q, table);
        return Mono.from(connectionFactory.create())
            .flatMap(connection ->
                Mono.from(connection.createStatement(s)
                    .bind("$1", desc)
                    .execute())
                    .flatMap(result -> Mono.from(result.map((row, metadata) -> CommonSetup.fromRow(row))))
                    .doFinally(signal -> connection.close())
            );
    }

    public Flux<CommonSetup> findAll(String table, int offset, int limit, String sortBy, String sortDir) {
        if (limit > 0) {
            String q = "select t.id, t.code, t.desc, t.ref, t.created_by, t.created_date, t.modified_by, t.modified_date, t.deleted, t.deleted_by, t.deleted_date from %s t where t.deleted is not true order by \"%s\" %s offset $1 limit $2";
            final String s = String.format(q, table, sortBy, sortDir);
            return Mono.from(connectionFactory.create())
                .flatMapMany(connection -> 
                    Flux.from(connection.createStatement(s)
                        .bind("$1", offset)
                        .bind("$2", limit)
                        .execute())
                        .flatMap(result -> result.map((row, metadata) -> CommonSetup.fromRow(row)))
                        .doFinally(signal -> connection.close())
                );
        } else {
            String q = "select t.id, t.code, t.desc, t.ref, t.created_by, t.created_date, t.modified_by, t.modified_date, t.deleted, t.deleted_by, t.deleted_date from %s t where t.desc <> '' and t.deleted is not true order by t.code";
            final String s = String.format(q, table);
            return Mono.from(connectionFactory.create())
                .flatMapMany(connection ->
                    Flux.from(connection.createStatement(s)
                        .execute())
                        .flatMap(result -> result.map((row, metadata) -> CommonSetup.fromRow(row)))
                        .doFinally(signal -> connection.close())
                );
        }
    }

    public Mono<Long> count(String table) {
        String q = "select count(id) from %s t where t.deleted is not true";
        final String s = String.format(q, table);
        return Mono.from(connectionFactory.create())
            .flatMap(connection ->
                Mono.from(connection.createStatement(s).execute())
                    .flatMap(result -> Mono.from(result.map((row, metadata) -> row.get(0, Long.class))))
                    .doFinally(signal -> connection.close())
            );
    }

    public Flux<CommonSetup> findByKeyword(String keyword, int offset, int limit, String sortBy, String sortDir, String table) {
        String q = "select t.id, t.code, t.desc, t.ref, t.created_by, t.created_date, t.modified_by, t.modified_date, t.deleted, t.deleted_by, t.deleted_date from %s t where (t.code ilike $1 or t.desc ilike $2 or t.ref ilike $3) and t.deleted is not true order by \"%s\" %s offset $4 limit $5";
        final String s = String.format(q, table, sortBy, sortDir);
        return Mono.from(connectionFactory.create())
            .flatMapMany(connection -> 
                Flux.from(connection.createStatement(s)
                    .bind("$1", keyword)
                    .bind("$2", keyword)
                    .bind("$3", keyword)
                    .bind("$4", offset)
                    .bind("$5", limit)
                    .execute())
                    .flatMap(result -> result.map((row, metadata) -> CommonSetup.fromRow(row)))
                    .doFinally(signal -> connection.close())
            );
    }

    public Mono<Long> countByKeyword(String keyword, String table) {
        String q = "select count(id) from %s t where (t.code ilike $1 or t.desc ilike $2 or t.ref ilike $3) and t.deleted is not true";
        final String s = String.format(q, table);
        return Mono.from(connectionFactory.create())
            .flatMap(connection ->
                Mono.from(connection.createStatement(s)
                    .bind("$1", keyword)
                    .bind("$2", keyword)
                    .bind("$3", keyword)
                    .execute())
                    .flatMap(result -> Mono.from(result.map((row, metadata) -> row.get(0, Long.class))))
                    .doFinally(signal -> connection.close())
        );
    }

    public Mono<Void> save(CommonSetup o, String table) {
        String q = "insert into %s (id, code, \"desc\", ref, created_by, created_date) values(nextval('%s_id_seq'),$1,$2,$3,$4,now())";
        final String s = String.format(q, table, table);
        return Mono.from(connectionFactory.create())
            .flatMap(connection ->
                Mono.from(connection.beginTransaction())
                    .then(Mono.from(connection.createStatement(s)
                        .bind("$1", o.getCode())
                        .bind("$2", o.getDesc())
                        .bind("$3", o.getRef())
                        .bind("$4", o.getCreatedBy())
                        .execute()))
                    .flatMap(result -> Mono.from(result.getRowsUpdated()))
                    .then(Mono.from(connection.commitTransaction()))
                    .onErrorResume(e ->
                        Mono.from(connection.rollbackTransaction())
                            .then(Mono.error(e)))
                    .doFinally(signal -> connection.close())
            )
            .then();
    }

    public Mono<Void> update(CommonSetup o, String table) {
        String q = "update %s set code = $1, \"desc\" = $2, ref = $3, modified_by = $4, modified_date = now() where id = $5";
        final String s = String.format(q, table);
        return Mono.from(connectionFactory.create())
            .flatMap(connection ->
                Mono.from(connection.beginTransaction())
                    .then(Mono.from(connection.createStatement(s)
                        .bind("$1", o.getCode())
                        .bind("$2", o.getDesc())
                        .bind("$3", o.getRef())
                        .bind("$4", o.getModifiedBy())
                        .bind("$5", o.getId())
                        .execute()))
                    .flatMap(result -> Mono.from(result.getRowsUpdated()))
                    .then(Mono.from(connection.commitTransaction()))
                    .onErrorResume(e ->
                        Mono.from(connection.rollbackTransaction())
                            .then(Mono.error(e)))
                    .doFinally(signal -> connection.close())
            )
            .then();
    }

    public Mono<Void> deleteById(Long id, Long userId, String table) {
        String q = "update %s set deleted = true, deleted_by = $1, deleted_date = now() where id = $2";
        final String s = String.format(q, table);
        return Mono.from(connectionFactory.create())
            .flatMap(connection ->
                Mono.from(connection.beginTransaction())
                    .then(Mono.from(connection.createStatement(s)
                        .bind("$1", userId)
                        .bind("$2", id)
                        .execute()))
                    .flatMap(result -> Mono.from(result.getRowsUpdated()))
                    .then(Mono.from(connection.commitTransaction()))
                    .onErrorResume(e ->
                        Mono.from(connection.rollbackTransaction())
                            .then(Mono.error(e)))
                    .doFinally(signal -> connection.close())
            )
            .then();
    }
}
