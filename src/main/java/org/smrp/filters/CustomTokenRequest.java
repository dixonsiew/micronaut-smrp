package org.smrp.filters;

import io.micronaut.security.authentication.AuthenticationRequest;

public class CustomTokenRequest implements AuthenticationRequest<String, String> {
    
    private final String token;
    
    public CustomTokenRequest(String token) {
        this.token = token;
    }
    
    @Override
    public String getIdentity() {
        return token;
    }
    
    @Override
    public String getSecret() {
        return null;
    }
}
