package com.example.mochiapi.models;

public class DailyGoal{
    private int id;
    private int userId;
    private String title;
    private String description;
    private boolean completed;
    private double quantity;
    private String date;
    private int personGoalId;
    private int goalTemplateId;
    private double progress;

    public DailyGoal() {
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public int getGoalTemplateId() {
        return goalTemplateId;
    }

    public void setGoalTemplateId(int goalTemplateId) {
        this.goalTemplateId = goalTemplateId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
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

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public double getQuantity() {
        return quantity;
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

    public int getUserId() {
        return userId;
    }

    public int getId() {
        return id;
    }

    public int getPersonGoalId() {
        return personGoalId;
    }

    public void setPersonGoalId(int personGoalId) {
        this.personGoalId = personGoalId;
    }

}
