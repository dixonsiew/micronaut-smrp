package org.smrp.controllers;

import java.net.URI;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Hidden;


@Secured(SecurityRule.IS_ANONYMOUS)
@Controller
public class HomeController {

    private final URI SWAGGER_UI;

    public HomeController(@Value("${micronaut.server.context-path}") String path) {
        this.SWAGGER_UI = UriBuilder.of(String.format("%s/swagger-ui", path)).path("index.html").build();
    }
    
    @Get
    @Hidden
    HttpResponse<?> home() {
        return HttpResponse.seeOther(SWAGGER_UI);
    }
}
