package org.smrp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;


@Introspected
@Serdeable
public record RefreshTokenDto(

    @JsonProperty("refresh_token")
    @NotBlank(message = "Refresh token is required")
    String refreshToken
) {}
