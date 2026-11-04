package org.smrp.controllers.soap.models;

import jakarta.xml.bind.annotation.*;


@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
public class SoapN2WEnvelope {
    
    @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private SoapN2WBody body;

    public SoapN2WBody getBody() {
        return body;
    }

    public void setBody(SoapN2WBody body) {
        this.body = body;
    }
}
