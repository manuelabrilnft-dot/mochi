package com.example.mochiapi.models;

public class UserProfile {
    private String username;
    private int level;
    private int streak;
    private int xp;
    private int layout;
    private int background;
    private MochiEntity mochi;

    public UserProfile() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getLayout() { return layout; }
    public void setLayout(int layout) { this.layout = layout; }

    public int getBackground() { return background; }
    public void setBackground(int background) { this.background = background; }

    public MochiEntity getMochi() { return mochi; }
    public void setMochi(MochiEntity mochi) { this.mochi = mochi; }
}