package com.example.medicalhomevisit.models.enums;

public enum VisitStatus {
    PLANNED(1),
    IN_PROGRESS(2),
    COMPLETED(3),
    CANCELLED(4);

    private int value;

    VisitStatus(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }
}
