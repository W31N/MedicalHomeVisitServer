package com.example.medicalhomevisit.models.enums;

public enum RequestType {
    EMERGENCY(1), REGULAR(2), CONSULTATION(3);

    private int value;

    RequestType(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }
}