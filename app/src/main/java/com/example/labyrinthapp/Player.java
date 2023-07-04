package com.example.labyrinthapp;

import androidx.annotation.NonNull;

/**
 * Player class which represents a player with its level and completion time.
 */
public class Player {
    private final String name;
    private final int level;
    private final int time;

    /**
     * Constructs a new player with the given values.
     * @param name Name of the player.
     * @param level Level of the player.
     * @param time Completion time of the player.
     */
    public Player(String name, int level, int time) {
        this.name = name;
        this.level = level;
        this.time = time;
    }

    /**
     * Returns a String representation of a player.
     * @return String representation of a player.
     */
    @NonNull
    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", level=" + level +
                ", time=" + time +
                '}';
    }

    /**
     * Gets the name of the player.
     * @return Name of the player
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the level of the player
     * @return Level of the player.
     */
    public int getLevel() {return level;}

    /**
     * Gets the completion time of a player.
     * @return Completion time of a player.
     */
    public int getTime() {return time;}
}
