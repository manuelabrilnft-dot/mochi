package com.example.mochiapi.models;

public class UserPreference{
    private int layout;
    private int background;
    private int suserId;

    public UserPreference() {
    }

    public int getLayout() {
        return layout;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public int getSuserId() {
        return suserId;
    }

    public void setSuserId(int suserId) {
        this.suserId = suserId;
    }
}
