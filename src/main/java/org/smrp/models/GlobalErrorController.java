package org.smrp.models;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.exceptions.HttpStatusException;


@Controller 
public class GlobalErrorController {
    
    @Error(exception = IllegalArgumentException.class, global = true)
    public HttpResponse<ErrorResponse> handleException(HttpRequest<?> request, IllegalArgumentException exception) {
        ErrorResponse error = new ErrorResponse(HttpStatus.BAD_REQUEST.getCode(), exception.getMessage());
        return HttpResponse.badRequest(error);
    }

    @Error(exception = RuntimeException.class, global = true)
    public HttpResponse<ErrorResponse> handleRuntimeException(HttpRequest<?> request, RuntimeException exception) {
        ErrorResponse error = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.getCode(), exception.getMessage());
        return HttpResponse.serverError(error);
    }

    @Error(exception = HttpStatusException.class, global = true)
    public HttpResponse<ErrorResponse> handleHttpStatusException(HttpRequest<?> request, HttpStatusException exception) {
        ErrorResponse error = new ErrorResponse(exception.getStatus().getCode(), exception.getMessage());
        return HttpResponse.status(exception.getStatus()).body(error);
    }
}
