package com.example.labyrinthapp;

public class Player {
    private String name;
    private int level;
    private int time;

    // for database
    public Player(String name, int level, int time) {
        this.name = name;
        this.level = level;
        this.time = time;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", level=" + level +
                ", time=" + time +
                '}';
    }

    public String getName() {
        return name;
    }

    public int getLevel() {return level;}

    public int getTime() {return time;}
}
