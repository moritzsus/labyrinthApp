package com.example.labyrinthapp;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.Arrays;
import java.util.Collections;

/**
 * A singleton Fragment that displays the game screen and generates random labyrinths.
 */
public class GameScreenFragment extends Fragment {
    View rootView;
    private LabyrinthView labyrinthView;
    private TextView temperatureView;
    private TextView timerView;
    private TextView levelView;
    private int timeCounter = 0;
    private boolean gameFinished = false;
    private int rows = 8;
    private int cols = 8;
    private int[][] labyrinth;
    private final MediaPlayer musicPlayer;
    @SuppressLint("StaticFieldLeak") // see documentation
    static GameScreenFragment instance;

    /**
     * Constructs an instance of the GameScreenFragment class.
     * It also creates and configures a MediaPlayer object to play background music.
     */
    public GameScreenFragment() {
        instance = this;

        musicPlayer = MediaPlayer.create(MainActivity.getInstance(), R.raw.life_of_a_wandering_wizard);
        musicPlayer.setLooping(true);
        musicPlayer.setVolume(0.4f, 0.4f);
    }

    /**
     * Gets an instance of the GameScreenFragment class.
     * @return An instance of the GameScreenFragment class.
     */
    static GameScreenFragment getInstance() {
        if(GameScreenFragment.instance == null)
            return new GameScreenFragment();

        return instance;
    }

    /**
     * Creates the views needed to display the game screen and initializes
     * their corresponding view variables.
     * It also generates the first random labyrinth and sends it to the
     * labyrinthView for it to display.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The View for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_game_screen, container, false);

        labyrinthView = rootView.findViewById(R.id.labyrinthView);
        generateLabyrinth();
        sendLabyrinthToView();

        temperatureView = rootView.findViewById(R.id.textViewTemp);
        timerView = rootView.findViewById(R.id.textViewTime);
        levelView = rootView.findViewById(R.id.textViewLevel);

        return rootView;
    }

    /**
     * Starts the background music if sound is turned on.
     * Resets the current player's move direction.
     */
    @Override
    public void onResume() {
        super.onResume();

        if(MainActivity.getInstance().getSoundOn())
            musicPlayer.start();

        PlayerController.getInstance().resetDirection();
    }

    /**
     * Pauses the background music if sound is turned on.
     */
    @Override
    public void onPause() {
        super.onPause();

        if(MainActivity.getInstance().getSoundOn())
            musicPlayer.pause();
    }

    /**
     * Starts or pauses the background music depending on the current sound settings.
     */
    public void checkIfMusicPlay() {

        if(MainActivity.getInstance().getSoundOn())
            musicPlayer.start();
        else
            musicPlayer.pause();
    }

    /**
     * Sends the generated labyrinth to the labyrinthView for it to display.
     */
    public void sendLabyrinthToView() {
        if(labyrinthView == null) {
            MainActivity.getInstance().setFirstTempRead(true);
            return;
        }
        labyrinthView.setLabyrinth(labyrinth);
    }

    /**
     * Sets the labyrinth size.
     * @param rows Amount of rows (horizontal paths) the labyrinth should have.
     * @param cols Amount of columns (vertical paths) the labyrinth should have.
     */
    public void setLabyrinthSize(int rows, int cols){
        this.rows = rows;
        this.cols = cols;
    }

    /**
     * Generates a labyrinth using a modified version of the Recursive Backtracking algorithm.
     * The labyrinth is represented as a 2D array of integers.
     * Paths are represented by 0, walls by 1 and the exit and entrance square by 2.
     * Calls the carvePassages(int row, int col) method to carve the passages.
     */
    public void generateLabyrinth() {
        int expandedRows = 2 * rows + 1;
        int expandedCols = 2 * cols + 1;

        labyrinth = new int[expandedRows][expandedCols];
        for (int i = 0; i < expandedRows; i++) {
            Arrays.fill(labyrinth[i], 1);
        }
        // set start and finish
        labyrinth[0][1] = 2;
        labyrinth[expandedRows - 1][expandedCols - 2] = 2;

        labyrinth[1][1] = 0;
        carvePassages(1, 1); // inside the border
    }

    /**
     * Recursively carves passages in the labyrinth starting from the given row and column.
     * It checks neighboring cells in random order and carves a passage if the conditions are met.
     * Conditions: Current square has to be inside the maze (valid labyrinth index)
     * and has to be a wall (represented by 1).
     *
     * @param row The starting row for carving passages.
     * @param col The starting column for carving passages.
     */
    private void carvePassages(int row, int col) {
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        Collections.shuffle(Arrays.asList(directions));

        for (int[] direction : directions) {
            int newRow = row + 2 * direction[0];
            int newCol = col + 2 * direction[1];

            if (newRow >= 0 && newRow < labyrinth.length && newCol >= 0 && newCol < labyrinth[0].length && labyrinth[newRow][newCol] == 1) {
                labyrinth[newRow][newCol] = 0;
                labyrinth[row + direction[0]][col + direction[1]] = 0;
                carvePassages(newRow, newCol);
            }
        }
    }

    /**
     * Sends the given temperature and the temperatureView to MainActivity's displayStatus
     * function to display the current temperature.
     *
     * @param temperature The temperature as a String of either the smartphone CPU
     * or if connected, the MPU6050 temperature.
     */
    public void setTemperature(String temperature) {
        MainActivity.getInstance().displayStatus(temperatureView, temperature);
    }

    /**
     * Sends the current value of the timer and the timerView to
     * MainActivity's displayStatus function to display the current in game time.
     */
    public void setTimer() {
        if(!gameFinished) {
            String timerStr = Integer.toString(timeCounter);
            MainActivity.getInstance().displayStatus(timerView, timerStr);
        }
    }

    /**
     * Sends the given level and the levelView to MainActivity's displayStatus
     * function to display the current level.
     *
     * @param level The current level.
     */
    public void setLevel(int level) {
        String levelStr = Integer.toString(level);
        MainActivity.getInstance().displayStatus(levelView, levelStr);
    }

    /**
     * Increases the timeCounter variable by 1.
     */
    public void increaseCounter(){
        if(!gameFinished) {
            timeCounter++;
        }
    }

    /**
     * Sets the gameFinished flag indicating whether the game is finished or not.
     * @param finished true if the game is finished, false otherwise.
     */
    public void setGameFinished(boolean finished) {
        gameFinished = finished;
    }

    /**
     * Gets the gameFinished flag indicating whether the game is finished or not.
     * @return A flag indicating whether the game is finished or not.
     */
    public boolean getGameFinished() {
        return gameFinished;
    }

    /**
     * Gets the time passed since the game started.
     * @return The time passed since the game started.
     */
    public int getTime() {
        return timeCounter;
    }

    /**
     * Gets the MediaPlayer which plays the background music.
     * @return The MediaPlayer which plays the background music.
     */
    public MediaPlayer getBackgroundMusicMediaPlayer() {
        return musicPlayer;
    }
}