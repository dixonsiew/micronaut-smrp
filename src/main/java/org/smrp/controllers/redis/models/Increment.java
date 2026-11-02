package org.smrp.controllers.redis.models;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;


@Introspected
@Serdeable
public class Increment {
    
    private String key;
    private Long value;

    public Increment(String key, Long value) {
        setKey(key);
        setValue(value);
    }
    
    public Increment() {}

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
