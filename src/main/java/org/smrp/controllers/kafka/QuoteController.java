package org.smrp.controllers.kafka;

import java.time.Duration;

import org.smrp.controllers.kafka.models.Quote;
import org.smrp.services.QuoteListener;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.sse.Event;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import reactor.core.publisher.Flux;


@Tag(name = "Kafka")
@Secured(SecurityRule.IS_ANONYMOUS)
@SecurityRequirement(name = "")
@Controller("/kafka")
public class QuoteController {
    
    private final QuoteListener quoteListener;

    public QuoteController(QuoteListener quoteListener) {
        this.quoteListener = quoteListener;
    }
    
    @Get("/quotes/stream")
    @Produces(MediaType.TEXT_EVENT_STREAM)
    public Flux<Event<Quote>> quotes() {
        Flux<Event<Quote>> kafkaFlux = quoteListener.getQuoteStream()
            .map(Event::of);
        Flux<Event<Quote>> heartbeat = Flux.interval(Duration.ofSeconds(15))
            .map(tick -> Event.of((Quote) null).comment("keep-alive"));
        return Flux.merge(kafkaFlux, heartbeat);
    }
}
