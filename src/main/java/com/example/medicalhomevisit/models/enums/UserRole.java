package com.example.medicalhomevisit.models.enums;


public enum UserRole {
    PATIENT(1), ADMIN(2), MEDICAL_STAFF(3), DISPATCHER(4);

    private int value;

    UserRole(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }
}
