package org.smrp.controllers.soap;

import io.micronaut.core.annotation.Introspected;
import jakarta.xml.bind.annotation.*;


@Introspected
@XmlAccessorType(XmlAccessType.FIELD)
public class ListOfCountryNamesByNameResponse {

    @XmlElement(name = "ListOfCountryNamesByNameResult",
                namespace = "http://www.oorsprong.org/websamples.countryinfo")
    private ListOfCountryNamesByNameResult result;

    public ListOfCountryNamesByNameResult getResult() { return result; }
    public void setResult(ListOfCountryNamesByNameResult result) { this.result = result; }
}
