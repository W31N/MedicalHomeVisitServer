package com.example.medicalhomevisit.models.enums;

public enum Gender {
    MALE(1), FEMALE(2), UNKNOWN(3);

    private int value;

    Gender(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }
}
