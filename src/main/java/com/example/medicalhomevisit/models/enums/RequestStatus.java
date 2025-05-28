package com.example.medicalhomevisit.models.enums;

public enum RequestStatus {
    NEW(1),
    PENDING(2),
    ASSIGNED(3),
    SCHEDULED(4),
    IN_PROGRESS(5),
    COMPLETED(6),
    CANCELLED(7);

    private int value;

    RequestStatus(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }
}
