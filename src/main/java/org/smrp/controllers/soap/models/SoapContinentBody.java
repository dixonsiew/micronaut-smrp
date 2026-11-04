package org.smrp.controllers.soap.models;

import jakarta.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
public class SoapContinentBody {

    @XmlElement(name = "ListOfContinentsByNameResponse",
                namespace = "http://www.oorsprong.org/websamples.countryinfo")
    private ListOfContinentsByNameResponse listOfContinentsByNameResponse;

    public ListOfContinentsByNameResponse getListOfContinentsByNameResponse() {
        return listOfContinentsByNameResponse;
    }

    public void setListOfContinentsByNameResponse(ListOfContinentsByNameResponse listOfContinentsByNameResponse) {
        this.listOfContinentsByNameResponse = listOfContinentsByNameResponse;
    }
}
