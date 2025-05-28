package com.example.medicalhomevisit.exception; // или другой ваш пакет для исключений

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}