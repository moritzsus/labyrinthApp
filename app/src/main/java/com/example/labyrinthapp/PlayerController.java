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
    public void movePlayer(float x, float y, float z) {
        if(labyrinthView == null) {
            Log.d("Rotationswerte", "LABVEW NULL");
        }

        // movement paused
        if(MainActivity.getInstance().getCurrentScreen() != MainActivity.ScreenEnum.GAMESCREEN) {
            direction = Direction.NONE;
        }

        //TODO fehleingaben raus
        if(x > verticalDeazone) direction = Direction.DOWN;
        if(x < -verticalDeazone) direction = Direction.UP;
        if(y < -horizontalDeazone) direction = Direction.LEFT;
        if(y > horizontalDeazone) direction = Direction.RIGHT;

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
}
