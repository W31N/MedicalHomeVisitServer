package com.example.medicalhomevisit.dtos;


import com.example.medicalhomevisit.models.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateRequestStatusDto {
    @NotNull
    private RequestStatus status;
    private String responseMessage; // Например, причина отмены

    // Геттеры и сеттеры
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }
}
