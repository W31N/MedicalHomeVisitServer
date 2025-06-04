package com.example.medicalhomevisit.dtos;

import java.util.Date;

public class UpdateVisitTimeDto {
    private Date scheduledTime;

    public UpdateVisitTimeDto() {}

    public Date getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(Date scheduledTime) { this.scheduledTime = scheduledTime; }
}
