package com.example.mochiapi.models;

public class MochiEntity {
    private int color;
    private int evolutionStage;
    private int costume;
    private int userId;
    private String name;
    private double moodCoefficient;

    public MochiEntity() {
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getEvolutionStage() {
        return evolutionStage;
    }

    public void setEvolutionStage(int evolutionStage) {
        this.evolutionStage = evolutionStage;
    }

    public int getCostume() {
        return costume;
    }

    public void setCostume(int costume) { this.costume = costume; }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMoodCoefficient() { return moodCoefficient; }

    public void setMoodCoefficient(double moodCoefficient) { this.moodCoefficient = moodCoefficient; }
}
