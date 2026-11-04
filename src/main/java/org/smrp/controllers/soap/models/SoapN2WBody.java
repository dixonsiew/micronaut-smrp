package org.smrp.controllers.soap.models;

import jakarta.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
public class SoapN2WBody {
    
    @XmlElement(name = "NumberToWordsResponse",
                namespace = "http://www.dataaccess.com/webservicesserver/")
    private NumberToWordsResponse numberToWordsResponse;

    public NumberToWordsResponse getNumberToWordsResponse() {
        return numberToWordsResponse;
    }

    public void setNumberToWordsResponse(NumberToWordsResponse numberToWordsResponse) {
        this.numberToWordsResponse = numberToWordsResponse;
    }
}
