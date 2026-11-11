package org.smrp.controllers.kafka;

import org.smrp.controllers.kafka.models.Quote;
import org.smrp.services.QuoteService;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
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
    
    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }
    
    @Get("/quotes")
    @Produces(MediaType.TEXT_EVENT_STREAM)
    public Flux<Quote> quotes() {
        return quoteService.getQuoteStream();
    }
}
