package com.example.medicalhomevisit.dtos;

import com.example.medicalhomevisit.models.enums.RequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.UUID;

public class CreateRequestDto {
    @NotNull
    private UUID patientId;
    @NotNull
    private RequestType requestType;
    @NotBlank
    private String symptoms;
    private String additionalNotes;
    private Date preferredDate;
    @NotBlank
    private String address;
}


