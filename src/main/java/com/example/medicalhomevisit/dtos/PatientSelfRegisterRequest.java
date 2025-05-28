package com.example.medicalhomevisit.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PatientSelfRegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String confirmPassword;

    // Конструкторы, геттеры и сеттеры

     @NotBlank(message = "ФИО не может быть пустым")
     @Size(min = 2, message = "ФИО должно содержать не менее 2 символов")
    public String getFullName() {
        return fullName;
    }


    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

     @NotBlank(message = "Email не может быть пустым")
     @Email(message = "Некорректный формат email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

     @NotBlank(message = "Пароль не может быть пустым")
     @Size(min = 6, message = "Пароль должен содержать не менее 6 символов")
    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }

     @NotBlank(message = "Подтверждение пароля не может быть пустым")
    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}