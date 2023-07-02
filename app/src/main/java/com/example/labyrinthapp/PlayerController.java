package com.example.labyrinthapp;

import android.media.MediaPlayer;

/**
 * The PlayerController (Singleton) class handles the movement and control of the player in the labyrinth game.
 */
public class PlayerController {
    /**
     * Enumeration for representing different movement directions.
     */
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

    /**
     * Constructs a PlayerController instance and sets the player's starting position.
     */
    public PlayerController() {
        instance = this;
        playerPositionRow = 0;
        playerPositionCol = 1;
    }

    /**
     * Returns the instance of the PlayerController.
     * @return The PlayerController instance.
     */
    public static PlayerController getInstance() {
        if(instance == null) {
            return new PlayerController();
        }
        return instance;
    }

    /**
     * Sets the LabyrinthView variable so the PlayerController can communicate
     * and send updates depending on where the player moves to.
     * @param labyrinthView The LabyrinthView to which the PlayerController
     * should send player position updates.
     */
    public void setLabyrinthView(LabyrinthView labyrinthView) {
        this.labyrinthView = labyrinthView;
    }

    /**
     * Sets the current labyrinth for the PlayerController.
     * @param labyrinth The labyrinth as a 2D integer array.
     */
    public void setLabyrinth(int[][] labyrinth) {
        this.labyrinth = labyrinth;
        playerPositionRow = 0;
        playerPositionCol = 1;
    }

    /**
     * Resets the level back to 1.
     */
    public void resetLevel() {level = 1;}

    /**
     * Gets the current level.
     * @return The current level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Moves the player based on the provided gyroscope rotation values.
     * Updates the LabyrinthView if player position changed.
     * Tells GameScreenFragment to generate a new labyrinth when the player reaches
     * the finish and updates the level.
     * @param x The rotation value in the x-axis.
     * @param y The rotation value in the y-axis.
     */
    public void movePlayer(float x, float y) {

        // Movement paused (e.g. when in Settings screen)
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
                if(labyrinth[playerPositionRow - 1][playerPositionCol] != 1) {  // != 1 -> 0 and 2: path und entrance
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
        labyrinthView.setPlayerIndex(playerPositionRow, playerPositionCol);

        // check if exit reached
        if(playerPositionRow == labyrinth.length - 1 && playerPositionCol == labyrinth[0].length - 2) {

            if(level == 5) {
                level++; // 6 means completed lv 5 (for SQL)
                if(!GameScreenFragment.getInstance().getGameFinished())
                    MainActivity.getInstance().onGameFinished();

                if(MainActivity.getInstance().getSoundOn()) {
                    MediaPlayer music = GameScreenFragment.getInstance().getBackgroundMusicMediaPlayer();
                    music.stop();

                    MediaPlayer mp = MediaPlayer.create(MainActivity.getInstance(), R.raw.game_complete);
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mp.release();
                        }
                    });
                    mp.start();
                }
            }
            else {
                level++;

                switch (level){
                    case 2:
                        GameScreenFragment.getInstance().setLabyrinthSize(9,9);
                        break;
                    case 3:
                        GameScreenFragment.getInstance().setLabyrinthSize(10,10);
                        break;
                    case 4:
                        GameScreenFragment.getInstance().setLabyrinthSize(11, 11);
                        break;
                    case 5:
                        GameScreenFragment.getInstance().setLabyrinthSize(12,12);
                        break;
                }
                GameScreenFragment.getInstance().generateLabyrinth();
                GameScreenFragment.getInstance().sendLabyrinthToView();
                GameScreenFragment.getInstance().setLevel(level);
                direction = Direction.NONE;

                if(MainActivity.getInstance().getSoundOn()) {
                    MediaPlayer mp = MediaPlayer.create(MainActivity.getInstance(), R.raw.level_passed);

                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mp.release();
                        }
                    });
                    mp.start();
                }
            }
            MainActivity.getInstance().onLabyrinthFinished();
        }
    }

    /**
     * Determines the most likely wanted direction change, even if the flags
     * suggest 2 different directions.
     * @param up When true, an UP direction was detected.
     * @param down When true, a DOWN direction was detected.
     * @param left When true, a LEFT direction was detected.
     * @param right When true, a RIGHT direction was detected.
     */
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

    /**
     * Resets the direction to NONE.
     */
    public void resetDirection() {
        direction = Direction.NONE;
    }
}
