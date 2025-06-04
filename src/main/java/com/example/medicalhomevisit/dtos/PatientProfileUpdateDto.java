package com.example.medicalhomevisit.dtos;

import com.example.medicalhomevisit.models.enums.Gender;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Date;
import java.util.List;

public class PatientProfileUpdateDto {

    @Past(message = "Дата рождения должна быть в прошлом")
    private Date dateOfBirth;

    private Gender gender;

    @Size(max = 500, message = "Адрес не должен превышать 500 символов")
    private String address;

    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{10,15}$", message = "Неверный формат номера телефона")
    private String phoneNumber;

    @Size(max = 50, message = "Номер полиса не должен превышать 50 символов")
    private String policyNumber;

    private List<String> allergies;
    private List<String> chronicConditions;

    // Конструкторы
    public PatientProfileUpdateDto() {}

    // Геттеры и сеттеры
    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    public List<String> getChronicConditions() {
        return chronicConditions;
    }

    public void setChronicConditions(List<String> chronicConditions) {
        this.chronicConditions = chronicConditions;
    }
}