package org.smrp.controllers.soap;

import java.util.List;

import org.smrp.controllers.soap.models.Continent;
import org.smrp.controllers.soap.models.CountryCodeAndName;
import org.smrp.services.SoapService;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import reactor.core.publisher.Mono;


@Tag(name = "Soap")
@Secured(SecurityRule.IS_ANONYMOUS)
@SecurityRequirement(name = "")
@Controller("/soap")
public class SoapController {
    
    private final SoapService soapService;

    public SoapController(SoapService soapService) {
        this.soapService = soapService;
    }

    // @ApiResponse(content = @Content(
    //     array = @ArraySchema(schema = @Schema(implementation = CountryCodeAndName.class))
    // ))
    @Get("/countryCodeAndNames")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<List<CountryCodeAndName>>> getCountryCodeAndNames() {
        return soapService.getCountryCodeAndNames();
    }

    @Get("/continents")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<List<Continent>>> getContinents() {
        return soapService.getContinents();
    }

    @Get("/numberToWords/{number}")
    @Produces(MediaType.TEXT_PLAIN)
    public Mono<HttpResponse<String>> numberToWords(String number) {
        return soapService.getNumberToWords(number);
    }
}
