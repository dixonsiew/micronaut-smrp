package org.smrp.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smrp.controllers.kafka.models.Quote;

import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.messaging.annotation.MessageBody;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;


@Singleton
public class QuoteListener {
    
    private static final Logger LOG = LoggerFactory.getLogger(QuoteListener.class);
    private final Sinks.Many<Quote> sink = Sinks.many().multicast().onBackpressureBuffer();
    // private final QuoteService quoteService;

    // public QuoteListener(QuoteService quoteService) {
    //     this.quoteService = quoteService;
    // }

    @KafkaListener(groupId = "${kafka.consumer.group-id}", offsetReset = OffsetReset.EARLIEST)
    @Topic("quote-requests-micronaut")
    public void receiveQuote(@KafkaKey String uuid, @MessageBody Quote quote) {
        LOG.info("Got Quote - {} with UUID: {}", uuid, quote);
        sink.tryEmitNext(quote);
        // quoteService.emitQuote(quote);
    }

    public Flux<Quote> getQuoteStream() {
        return sink.asFlux();
    }
}
