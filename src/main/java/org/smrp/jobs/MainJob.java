package org.smrp.jobs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smrp.controllers.kafka.models.Quote;
import org.smrp.services.QuoteClient;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;


@Singleton 
public class MainJob {
    
    private static final Logger LOG = LoggerFactory.getLogger(MainJob.class);
    private final HttpClient httpClient;
    private final QuoteClient quoteClient;
    private final Random random = new Random();

    public MainJob(@Client("http://localhost:8200/smrp") HttpClient httpClient, QuoteClient quoteClient) {
        this.httpClient = httpClient;
        this.quoteClient = quoteClient;
    }

    @Scheduled(fixedDelay = "10s")
    void executeEveryTenSeconds() throws InterruptedException {
        Thread.sleep(200);
        UUID uuid = UUID.randomUUID();
        LOG.info("Sending quote request with UUID: {}", uuid);
        Quote o = new Quote(uuid.toString(), random.nextInt(100));
        quoteClient.process(uuid.toString(), o);
    }


    @Scheduled(fixedDelay = "60s")
    void executeEverySixtySeconds() {
        LOG.info("Simple Job every 60 seconds: {}", new SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(new Date()));
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
