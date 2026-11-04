package org.smrp.controllers.soap.models;

import jakarta.xml.bind.annotation.*;


@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
public class SoapContinentEnvelope {

    @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private SoapContinentBody body;

    public SoapContinentBody getBody() {
        return body;
    }

    public void setBody(SoapContinentBody body) {
        this.body = body;
    }
}
