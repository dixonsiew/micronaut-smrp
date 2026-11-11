package org.smrp.services;

import jakarta.inject.Singleton;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import org.smrp.controllers.kafka.models.Quote;

@Singleton
public class QuoteService {
    
    private final Sinks.Many<Quote> sink = Sinks.many().multicast().onBackpressureBuffer();

    public void emitQuote(Quote q) {
        sink.tryEmitNext(q);
    }
    
    public Flux<Quote> getQuoteStream() {
        return sink.asFlux();
    }
}
