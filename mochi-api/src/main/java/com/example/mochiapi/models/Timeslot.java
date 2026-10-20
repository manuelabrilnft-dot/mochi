package com.example.mochiapi.models;


import java.time.LocalTime;

public class Timeslot {
    private LocalTime startTime;
    private LocalTime endTime;

    public Timeslot() {
    }

    public LocalTime getStartDate() {
        return startTime;
    }

    public void setStartDate(LocalTime startDate) {
        this.startTime = startDate;
    }

    public LocalTime getEndDate() {
        return endTime;
    }

    public void setEndDate(LocalTime endDate) {
        this.endTime = endDate;
    }
}
