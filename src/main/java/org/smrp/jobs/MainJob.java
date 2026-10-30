package org.smrp.jobs;

import io.micronaut.scheduling.annotation.Scheduled;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;


@Singleton 
public class MainJob {
    
    private static final Logger LOG = LoggerFactory.getLogger(MainJob.class);
    private final HttpClient httpClient;

    public MainJob(@Client("http://localhost:8300/smrp") HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Scheduled(fixedDelay = "10s") // 
    void executeEveryTen() {
        LOG.info("Simple Job every 10 seconds: {}", new SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(new Date()));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("username", "admin");
        m.put("password", "admin123");

        try {
            HttpRequest<?> request = HttpRequest.POST("/o/token", m);
            Mono<HttpResponse<Map<String,Object>>> x = Mono.from(
                httpClient.exchange(
                    request, 
                    Argument.mapOf(String.class, Object.class),
                    Argument.mapOf(String.class, Object.class)
                ));
            x.subscribe(
                mx -> {
                    System.out.println(mx.body());
                },
                error -> {
                   LOG.error(error.getMessage()); 
                }
            );
        } catch (Exception e) {
            LOG.error(e.getMessage()); 
        }
    }
}
