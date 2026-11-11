package org.smrp.services;

import org.smrp.controllers.kafka.models.Quote;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;


@KafkaClient()
public interface QuoteClient {

    @Topic("quote-requests")
    void process(@KafkaKey String requestid, Quote quote);
}
