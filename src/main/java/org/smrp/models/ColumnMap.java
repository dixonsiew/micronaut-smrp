package org.smrp.models;

import io.micronaut.serde.annotation.Serdeable;


@Serdeable
public record ColumnMap(String field, String text) {

    public String getField() {
        return field;
    }

    public String getText() {
        return text;
    }
}
