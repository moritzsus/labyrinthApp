package com.example.labyrinthapp;

import android.graphics.Paint;
import android.media.MediaPlayer;
import android.telecom.Call;
import android.util.Log;
import android.view.View;

public class PlayerController {
    enum Direction {
        NONE, UP, DOWN, LEFT, RIGHT
    }
    static PlayerController instance;
    int playerPositionRow, playerPositionCol;
    Direction direction = Direction.NONE;
    private int[][] labyrinth;
    LabyrinthView labyrinthView;
    private float verticalDeazone = 0.4f;
    private float horizontalDeazone = 0.5f;
    private int level = 1;

    public PlayerController() {
        instance = this;
        playerPositionRow = 0;
        playerPositionCol = 1;
    }

    public static PlayerController getInstance() {
        if(instance == null) {
            return new PlayerController();
        }
        return instance;
    }

    public void setLabyrinthView(LabyrinthView labyrinthView) {
        this.labyrinthView = labyrinthView;
    }

    public void setLabyrinth(int[][] labyrinth) {
        this.labyrinth = labyrinth;
        playerPositionRow = 0;
        playerPositionCol = 1;
    }

    public void resetLevel() {level = 1;}
    public int getLevel() {
        return level;
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
                if(labyrinth[playerPositionRow - 1][playerPositionCol] != 1) {  // != 1 -> 0 und 2 erlaubt, path und entrance
                    playerPositionRow--;
                }
                break;
            case DOWN:
                if(playerPositionRow + 1 > (labyrinth.length - 1))
                    break;
                if(labyrinth[playerPositionRow + 1][playerPositionCol] != 1) {
                    playerPositionRow++;
                }
                break;
            case LEFT:
                if(labyrinth[playerPositionRow][playerPositionCol - 1] != 1)
                    playerPositionCol--;
                break;
            case RIGHT:
                if(labyrinth[playerPositionRow][playerPositionCol + 1] != 1)
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
                //TODO stop movement
                if(!GameScreenFragment.getInstance().getGameFinished())
                    MainActivity.getInstance().onGameFinished();

                if(MainActivity.getInstance().getSoundOn()) {
                    MediaPlayer music = GameScreenFragment.getInstance().getBackgroundMusicMediaPlayer();
                    music.stop();

                    MediaPlayer mp = MediaPlayer.create(MainActivity.getInstance(), R.raw.game_complete);
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            music.setVolume(0.6f, 0.6f);
                            mp.release();
                        }
                    });
                    mp.start();
                }
            }
            else {
                //TODO erster frame bei neuem labyrinth ist player noch im ziel (erst im nächsten frame neues labyrinth?)
                level++;

                switch (level){
                    case 2:
                        GameScreenFragment.getInstance().setLabyrinthSize(9,9);
                        break;
                    case 3:
                        GameScreenFragment.getInstance().setLabyrinthSize(10,10);
                        break;
                    case 4:
                        GameScreenFragment.getInstance().setLabyrinthSize(11,11);
                        break;
                    case 5:
                        GameScreenFragment.getInstance().setLabyrinthSize(12,12);
                        break;
                }
                GameScreenFragment.getInstance().generateLabyrinth();
                GameScreenFragment.getInstance().sendLabyrinthToView();
                GameScreenFragment.getInstance().setLevel(level);
                //TODO keep direction -> dann wäre erster tick spieler 1 weiter vorm startpunkt -> bool variable?
                direction = Direction.NONE;

                if(MainActivity.getInstance().getSoundOn()) {
                    MediaPlayer music = GameScreenFragment.getInstance().getBackgroundMusicMediaPlayer();
                    music.setVolume(0.2f, 0.2f);

                    MediaPlayer mp = MediaPlayer.create(MainActivity.getInstance(), R.raw.level_passed);

                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            music.setVolume(0.6f, 0.6f);
                            mp.release();
                        }
                    });
                    mp.start();
                }
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
