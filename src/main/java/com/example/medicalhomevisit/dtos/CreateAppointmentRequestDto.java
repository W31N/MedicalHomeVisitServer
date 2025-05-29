package com.example.medicalhomevisit.dtos;

import com.example.medicalhomevisit.models.enums.RequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public class CreateAppointmentRequestDto {

    private RequestType requestType;
    private String symptoms;
    private String additionalNotes;
    private Date preferredDateTime;
    private String address;


    @NotNull
    public RequestType getRequestType() { return requestType; }
    public void setRequestType(RequestType requestType) { this.requestType = requestType; }
    @NotBlank
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public Date getPreferredDateTime() {
        return preferredDateTime;
    }

    public void setPreferredDateTime(Date preferredDateTime) {
        this.preferredDateTime = preferredDateTime;
    }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
