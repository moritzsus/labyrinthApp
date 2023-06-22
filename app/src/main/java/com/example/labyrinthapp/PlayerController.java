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
    public void movePlayer(float x, float y, float z) {
        if(labyrinthView == null) {
            Log.d("Rotationswerte", "LABVEW NULL");
        }

        if(x > verticalDeazone) direction = Direction.DOWN;
        if(x < -verticalDeazone) direction = Direction.UP;
        if(y < -horizontalDeazone) direction = Direction.LEFT;
        if(y > horizontalDeazone) direction = Direction.RIGHT;

        switch (direction) {
            case UP:
                playerPositionRow--;
                break;
            case DOWN:
                playerPositionRow++;
                break;
            case LEFT:
                playerPositionCol--;
                break;
            case RIGHT:
                playerPositionCol++;
                break;
            default:
        }

        labyrinthView.setPlayerIndex(playerPositionRow, playerPositionCol);
    }
}
