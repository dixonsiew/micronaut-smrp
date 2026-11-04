package org.smrp.services;

import java.io.StringReader;
import java.net.URI;
import java.util.List;

import org.smrp.controllers.soap.models.*;

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
    private final JAXBContext contextCCN;
    private final JAXBContext contextContinent;
    private final JAXBContext contextNumberToWords;

    public SoapService(@Client("/") HttpClient httpClient) {
        this.httpClient = httpClient;
        try {
            this.contextCCN = JAXBContext.newInstance(SoapCCNEnvelope.class);
            this.contextContinent = JAXBContext.newInstance(SoapContinentEnvelope.class);
            this.contextNumberToWords = JAXBContext.newInstance(SoapN2WEnvelope.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JAXBContext", e);
        }
    }

    public Mono<HttpResponse<List<CountryCodeAndName>>> getCountryCodeAndNames() {
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
                Unmarshaller unmarshaller = contextCCN.createUnmarshaller();
                SoapCCNEnvelope envelope = (SoapCCNEnvelope) unmarshaller.unmarshal(new StringReader(x.body()));
                List<CountryCodeAndName> result = envelope.getBody()
                    .getListOfCountryNamesByNameResponse()
                    .getListOfCountryNamesByNameResult()
                    .getCountryCodeAndNames();
                return Mono.just(HttpResponse.ok(result));
            } catch (Exception e) {
                return Mono.error(e);
            }
        });
    }

    public Mono<HttpResponse<List<Continent>>> getContinents() {
        String s = """
            <x:Envelope
                xmlns:x="http://schemas.xmlsoap.org/soap/envelope/"
                xmlns:cou="https://soap-service-free.mock.beeceptor.com/CountryInfoService">
                <x:Header/>
                <x:Body>
                    <cou:ListOfContinentsByName/>
                </x:Body>
            </x:Envelope>
        """;
        URI uri = URI.create("https://soap-service-free.mock.beeceptor.com/CountryInfoService.wso");
        HttpRequest<?> request = HttpRequest.POST(uri.toString(), s)
            .header("Content-Type", "text/xml; charset=utf-8")
            .header("SOAPAction", "https://soap-service-free.mock.beeceptor.com/CountryInfoService.wso/ListOfContinentsByName");
        Mono<HttpResponse<String>> data = Mono.from(
            httpClient.exchange(
                request, 
                Argument.of(String.class),
                Argument.of(String.class)
            ));
        return data.flatMap(x -> {
            try {
                Unmarshaller unmarshaller = contextContinent.createUnmarshaller();
                SoapContinentEnvelope envelope = (SoapContinentEnvelope) unmarshaller.unmarshal(new StringReader(x.body()));
                List<Continent> result = envelope.getBody()
                    .getListOfContinentsByNameResponse()
                    .getListOfContinentsByNameResult()
                    .getContinents();
                return Mono.just(HttpResponse.ok(result));
            } catch (Exception e) {
                return Mono.error(e);
            }
        });
    }

    public Mono<HttpResponse<String>> getNumberToWords(String number) {
        String s = """
            <x:Envelope
                xmlns:x="http://schemas.xmlsoap.org/soap/envelope/"
                xmlns:web="http://www.dataaccess.com/webservicesserver/">
                <x:Header/>
                <x:Body>
                    <web:NumberToWords>
                        <web:ubiNum>%s</web:ubiNum>
                    </web:NumberToWords>
                </x:Body>
            </x:Envelope>
        """.formatted(number);
        URI uri = URI.create("https://www.dataaccess.com/webservicesserver/numberconversion.wso");
        HttpRequest<?> request = HttpRequest.POST(uri.toString(), s)
            .header("Content-Type", "text/xml; charset=utf-8")
            .header("SOAPAction", "");
        Mono<HttpResponse<String>> data = Mono.from(
            httpClient.exchange(
                request, 
                Argument.of(String.class),
                Argument.of(String.class)
            ));
        return data.flatMap(x -> {
            try {
                Unmarshaller unmarshaller = contextNumberToWords.createUnmarshaller();
                SoapN2WEnvelope envelope = (SoapN2WEnvelope) unmarshaller.unmarshal(new StringReader(x.body()));
                String result = envelope.getBody()
                    .getNumberToWordsResponse()
                    .getNumberToWordsResult();
                return Mono.just(HttpResponse.ok(result));
            } catch (Exception e) {
                return Mono.error(e);
            }
        });
    }
}
