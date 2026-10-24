package org.smrp.models;

import io.micronaut.serde.annotation.Serdeable;
import io.r2dbc.spi.Row;


@Serdeable
public class Role {

    private Long id;
    private String name;

    public static Role fromRow(Row row) {
        Role role = new Role();
        role.setId(row.get("id", Long.class));
        role.setName(row.get("name", String.class));
        return role;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
