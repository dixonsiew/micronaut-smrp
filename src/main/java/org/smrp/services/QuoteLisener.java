package org.smrp.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.smrp.controllers.kafka.models.Quote;

import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.messaging.annotation.MessageBody;


@KafkaListener(groupId = "${kafka.consumer.group-id}", offsetReset = OffsetReset.EARLIEST)
public class QuoteLisener {
    
    private static final Logger LOG = LoggerFactory.getLogger(QuoteLisener.class);
    private final QuoteService quoteService;

    public QuoteLisener(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @Topic("quote-requests")
    public void receiveQuote(@KafkaKey String uuid, @MessageBody Quote quote) {
        LOG.info("Got Quote - {} with UUID: {}", uuid, quote);
        quoteService.emitQuote(quote);
    }
}
