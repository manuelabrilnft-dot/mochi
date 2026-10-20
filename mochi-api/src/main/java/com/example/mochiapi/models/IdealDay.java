package com.example.mochiapi.models;

public class IdealDay {
    private int userId;
    private int max_working_hours;
    private String wake_up_time;
    private String bedtime;

    public IdealDay() {
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMax_working_hours() {
        return max_working_hours;
    }

    public void setMax_working_hours(int max_working_hours) {
        this.max_working_hours = max_working_hours;
    }

    public String getWake_up_time() {
        return wake_up_time;
    }

    public void setWake_up_time(String wake_up_time) {
        this.wake_up_time = wake_up_time;
    }

    public String getBedtime() {
        return bedtime;
    }

    public void setBedtime(String bedtime) {
        this.bedtime = bedtime;
    }
}
