package org.smrp.dtos;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;


@Introspected
@Serdeable
public record KeywordDto(String keyword) {
    
    public String getKeyword() {
        return keyword;
    }
}
