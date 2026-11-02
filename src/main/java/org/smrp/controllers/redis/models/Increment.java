package org.smrp.controllers.redis.models;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;


@Introspected
@Serdeable
public class Increment {
    
    public String key;
    public long value;

    public Increment(String key, long value) {
        this.key = key;
        this.value = value;
    }
    
    public Increment() {}
}
