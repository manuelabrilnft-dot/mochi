package com.example.mochiapi.models;

import java.util.List;

public class PersonalGoal{
    private int id;
    private int userId;
    private String title;
    private String description;
    private boolean completed;
    private List<DailyGoal> dailyGoals;
    private double progress;
    private int quantity;

    public PersonalGoal() {
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<DailyGoal> getDailyGoals() {
        return dailyGoals;
    }

    public void setDailyGoals(List<DailyGoal> dailyGoals) {
        this.dailyGoals = dailyGoals;
    }

}
