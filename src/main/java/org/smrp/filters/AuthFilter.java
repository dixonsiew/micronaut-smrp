package org.smrp.filters;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ServerFilter;

import org.smrp.models.ErrorResponse;


@ServerFilter("/api/**")
public class AuthFilter {
    
    @RequestFilter
    public @Nullable HttpResponse<?> filterRequest(HttpRequest<?> request) {
        Cookie tokenCookie = request.getCookies().get("token");
        String token = "";
        if (tokenCookie != null) {
            token = tokenCookie.getValue();
        }

        if (token.isEmpty()) {
            String authHeader = request.getHeaders().get("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.replace("Bearer ", "").replace("Bearer", "").trim();
            }
        }

        if (token.isEmpty()) {
            ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.getCode(), "Unauthorized");
            return HttpResponse.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        return null; // Return null to continue the chain
    }
}
