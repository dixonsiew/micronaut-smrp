package org.smrp.controllers.soap.models;

import io.micronaut.core.annotation.Introspected;
import jakarta.xml.bind.annotation.*;


@Introspected
@XmlAccessorType(XmlAccessType.FIELD)
public class SoapBody {

    @XmlElement(name = "ListOfCountryNamesByNameResponse",
                namespace = "http://www.oorsprong.org/websamples.countryinfo")
    private ListOfCountryNamesByNameResponse response;

    public ListOfCountryNamesByNameResponse getResponse() { return response; }
    public void setResponse(ListOfCountryNamesByNameResponse response) { this.response = response; }
}
