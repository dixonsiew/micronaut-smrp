package org.smrp.services;

import java.io.StringReader;
import java.net.URI;
import java.util.List;

import org.smrp.controllers.soap.models.CountryCodeAndName;
import org.smrp.controllers.soap.models.SoapEnvelope;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;

import jakarta.inject.Singleton;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import reactor.core.publisher.Mono;


@Singleton 
public class SoapService {
    
    private final HttpClient httpClient;
    private final JAXBContext context;

    public SoapService(@Client("/") HttpClient httpClient) {
        this.httpClient = httpClient;
        try {
            this.context = JAXBContext.newInstance(SoapEnvelope.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JAXBContext", e);
        }
    }

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
        URI uri = URI.create("https://soap-service-free.mock.beeceptor.com/CountryInfoService.wso");
        HttpRequest<?> request = HttpRequest.POST(uri.toString(), s)
            .header("Content-Type", "text/xml; charset=utf-8")
            .header("SOAPAction", "https://soap-service-free.mock.beeceptor.com/CountryInfoService.wso/ListOfCountryNamesByName");
        Mono<HttpResponse<String>> data = Mono.from(
            httpClient.exchange(
                request, 
                Argument.of(String.class),
                Argument.of(String.class)
            ));
        return data.flatMap(x -> {
            try {
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
