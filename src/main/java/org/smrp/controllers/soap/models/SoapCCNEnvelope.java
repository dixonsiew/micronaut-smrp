package org.smrp.controllers.soap.models;

import jakarta.xml.bind.annotation.*;


@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
public class SoapCCNEnvelope {

    @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private SoapCCNBody body;

    public SoapCCNBody getBody() {
        return body;
    }

    public void setBody(SoapCCNBody body) {
        this.body = body;
    }
}
