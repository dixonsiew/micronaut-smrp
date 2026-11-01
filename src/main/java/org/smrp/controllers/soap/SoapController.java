package org.smrp.controllers.soap;

import java.io.StringReader;
import java.util.List;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import reactor.core.publisher.Mono;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;


@Tag(name = "Soap")
@Secured(SecurityRule.IS_ANONYMOUS)
@SecurityRequirement(name = "")
@Controller("/soap")
public class SoapController {
    
    private final HttpClient httpClient;

    public SoapController(@Client("https://soap-service-free.mock.beeceptor.com") HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    // @ApiResponse(content = @Content(
    //     array = @ArraySchema(schema = @Schema(implementation = CountryCodeAndName.class))
    // ))
    @Get("/countries")
    @Produces(MediaType.APPLICATION_JSON)
    public Mono<HttpResponse<List<CountryCodeAndName>>> getCountries() {
        String s = """
            <x:Envelope
                xmlns:x="http://schemas.xmlsoap.org/soap/envelope/"
                xmlns:cou="https://soap-service-free.mock.beeceptor.com/CountryInfoService">
                <x:Header/>
                <x:Body>
                    <cou:ListOfCountryNamesByName/>
                </x:Body>
            </x:Envelope>
        """;
        HttpRequest<?> request = HttpRequest.POST("/CountryInfoService.wso", s)
            .header("Content-Type", "text/xml; charset=utf-8")
            .header("SOAPAction", "https://soap-service-free.mock.beeceptor.com/CountryInfoService.wso/ListOfCountryNamesByName");
        Mono<HttpResponse<String>> data = Mono.from(
            httpClient.exchange(
                request, 
                Argument.of(String.class),
                Argument.of(String.class)
            ));
        return data.flatMap(x -> {
            JAXBContext context;
            try {
                context = JAXBContext.newInstance(SoapEnvelope.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                SoapEnvelope envelope = (SoapEnvelope) unmarshaller.unmarshal(new StringReader(x.body()));
                List<CountryCodeAndName> result = envelope.getBody()
                    .getResponse()
                    .getResult()
                    .getCountryCodeAndNames();
                return Mono.just(HttpResponse.ok(result));
            } catch (Exception e) {
                return Mono.error(e);
            }
        });
    }
}
