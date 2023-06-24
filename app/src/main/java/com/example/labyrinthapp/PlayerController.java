package com.example.labyrinthapp;

import android.graphics.Paint;
import android.telecom.Call;
import android.util.Log;
import android.view.View;

public class PlayerController {
    enum Direction {
        NONE, UP, DOWN, LEFT, RIGHT
    }
    static PlayerController playerController;
    int playerPositionRow, playerPositionCol;
    Direction direction = Direction.NONE;
    private int[][] labyrinth;
    Paint playerPaint = new Paint(); // später zu sprite ändern
    LabyrinthView labyrinthView;
    private float verticalDeazone = 0.2f;
    private float horizontalDeazone = 0.5f;
    private int level = 1;

    public PlayerController() {
        playerPositionRow = 0;
        playerPositionCol = 1;
    }

    public static PlayerController getInstance() {
        if(playerController == null) {
            playerController = new PlayerController();
            return playerController;
        }
        return playerController;
    }

    public void setLabyrinthView(LabyrinthView labyrinthView) {
        this.labyrinthView = labyrinthView;
    }

    public void setLabyrinth(int[][] labyrinth) {
        this.labyrinth = labyrinth;
        playerPositionRow = 0;
        playerPositionCol = 1;
    }

    public void setPlayerPaint(Paint paint) {
        playerPaint = paint;
    }

    // rotation values
    public void movePlayer(float x, float y) {
        if(labyrinthView == null) {
            Log.d("Rotationswerte", "LABVEW NULL");
        }

        // movement paused
        if(MainActivity.getInstance().getCurrentScreen() != MainActivity.ScreenEnum.GAMESCREEN) {
            direction = Direction.NONE;
        }

        boolean up = false;
        boolean down = false;
        boolean left = false;
        boolean right = false;

        // set direction flags
        if(x < -verticalDeazone) up = true;
        if(x > verticalDeazone) down = true;
        if(y < -horizontalDeazone) left = true;
        if(y > horizontalDeazone) right = true;

        // set direction depending on the flags
        determineDirection(up, down, left, right);

        switch (direction) {
            case UP:
                if(playerPositionRow - 1 < 0)
                    break;
                if(labyrinth[playerPositionRow - 1][playerPositionCol] == 0) {
                    playerPositionRow--;
                }
                break;
            case DOWN:
                if(playerPositionRow + 1 > (labyrinth.length - 1))
                    break;
                if(labyrinth[playerPositionRow + 1][playerPositionCol] == 0) {
                    playerPositionRow++;
                }
                break;
            case LEFT:
                if(labyrinth[playerPositionRow][playerPositionCol - 1] == 0)
                    playerPositionCol--;
                break;
            case RIGHT:
                if(labyrinth[playerPositionRow][playerPositionCol + 1] == 0)
                    playerPositionCol++;
                break;
            default:
        }
        //TODO im ziel -> direction NONE
        //TODO wenn Abbruch -> Homescreen -> direction NONE
        labyrinthView.setPlayerIndex(playerPositionRow, playerPositionCol);

        // Ziel
        if(playerPositionRow == labyrinth.length - 1 && playerPositionCol == labyrinth[0].length - 2) {
            if(level == 5) {
                Log.d("Rotationswerte", "ALL FINISHED");
            }
            else {
                //TODO erster frame bei neuem labyrinth ist player noch im ziel
                Log.d("Rotationswerte", "Level " + level + " FINISHED");
                level++;
                GameScreenFragment.getInstance().generateLabyrinth();
                GameScreenFragment.getInstance().sendLabyrinthToView();
                direction = Direction.NONE;
            }
        }
    }

    private void determineDirection(boolean up, boolean down, boolean left, boolean right) {
        if(direction == Direction.NONE) {
            if(up) direction = Direction.UP;
            if(down) direction = Direction.DOWN;
            if(left) direction = Direction.LEFT;
            if(right) direction = Direction.RIGHT;
        }
        else if(direction == Direction.UP) {
            if(down && left) direction = Direction.LEFT;
            if(down && right) direction = Direction.RIGHT;
            if(down && !left && !right) direction = Direction.DOWN;
            if(!down && left) direction = Direction.LEFT;
            if(!down && right) direction = Direction.RIGHT;
        }
        else if(direction == Direction.DOWN) {
            if(up && left) direction = Direction.LEFT;
            if(up && right) direction = Direction.RIGHT;
            if(up && !left && !right) direction = Direction.UP;
            if(!up && left) direction = Direction.LEFT;
            if(!up && right) direction = Direction.RIGHT;
        }
        else if(direction == Direction.LEFT) {
            if(right && up) direction = Direction.UP;
            if(right && down) direction = Direction.DOWN;
            if(right && !up && !down) direction = Direction.RIGHT;
            if(!right && up) direction = Direction.UP;
            if(!right && down) direction = Direction.DOWN;
        }
        else if(direction == Direction.RIGHT) {
            if(left && up) direction = Direction.UP;
            if(left && down) direction = Direction.DOWN;
            if(left && !up && !down) direction = Direction.LEFT;
            if(!left && up) direction = Direction.UP;
            if(!left && down) direction = Direction.DOWN;
        }
    }
}
