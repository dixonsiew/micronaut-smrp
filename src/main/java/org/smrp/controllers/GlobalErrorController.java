package org.smrp.controllers;

import org.smrp.models.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.AuthorizationException;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.ConstraintViolationException;


@Secured(SecurityRule.IS_ANONYMOUS)
@Controller
public class GlobalErrorController {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalErrorController.class);
    
    @Error(exception = IllegalArgumentException.class, global = true)
    public HttpResponse<ErrorResponse> handleIllegalArgumentException(HttpRequest<?> request, IllegalArgumentException exception) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.getCode(), exception.getMessage());
        return HttpResponse.badRequest().body(error);
    }

    @Error(exception = RuntimeException.class, global = true)
    public HttpResponse<ErrorResponse> handleRuntimeException(HttpRequest<?> request, RuntimeException exception) {
        LOG.error("RuntimeException: " + exception.getMessage(), exception);
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), exception.getMessage());
        return HttpResponse.serverError().body(error);
    }

    @Error(exception = ConstraintViolationException.class, global = true)
    public HttpResponse<ErrorResponse> handleConstraintViolationException(HttpRequest<?> request, ConstraintViolationException exception) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.getCode(), exception.getMessage());
        return HttpResponse.badRequest().body(error);
    }

    @Error(exception = AuthorizationException.class, global = true)
    public HttpResponse<ErrorResponse> handleAuthorizationException(HttpRequest<?> request, AuthorizationException exception) {
        ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.getCode(), "Unauthorized");
        return HttpResponse.unauthorized().body(error);
    }

    @Error(exception = HttpStatusException.class, global = true)
    public HttpResponse<ErrorResponse> handleHttpStatusException(HttpRequest<?> request, HttpStatusException exception) {
        ErrorResponse error = new ErrorResponse(exception.getStatus().getCode(), exception.getMessage());
        return HttpResponse.status(exception.getStatus()).body(error);
    }
}
