package org.smrp;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SecurityScheme(
    name = "BearerAuth",
    type = SecuritySchemeType.HTTP,
    in = SecuritySchemeIn.HEADER,
    scheme = "bearer",
    paramName = "Authorization",
    description = "Bearer token authentication"
)
@OpenAPIDefinition(
    info = @Info(
        title = "Swagger SMRP Backend API",
        version = "1.0",
        description = "This API handles all SMRP operations."
    ),
    security = {
        @SecurityRequirement(name = "BearerAuth")
    }
)
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}