package com.example.mochiapi.models;

import java.util.List;
import java.util.Map;

public class Challenge {
    private int id;
    private String title;
    private String description;
    private double quantity;
    private boolean completed;
    private int goalTemplateId;
    private int creatorId;

    private List<String> participants;
    private Map<String, Double> leaderboard;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getGoalTemplateId() {
        return goalTemplateId;
    }

    public void setGoalTemplateId(int goalTemplateId) {
        this.goalTemplateId = goalTemplateId;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public Map<String, Double> getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(Map<String, Double> leaderboard) {
        this.leaderboard = leaderboard;
    }
}