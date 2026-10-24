package org.smrp.dtos;

import jakarta.validation.constraints.NotBlank;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;


@Introspected
@Serdeable
public record LoginDto(

    @NotBlank(message = "Username is required")
    String username,

    @NotBlank(message = "Password is required")
    String password
) {}
