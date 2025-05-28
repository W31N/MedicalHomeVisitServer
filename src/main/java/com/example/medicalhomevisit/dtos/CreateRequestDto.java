package com.example.medicalhomevisit.dtos;

@Data
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


