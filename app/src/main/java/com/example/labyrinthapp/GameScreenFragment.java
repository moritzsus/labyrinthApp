package com.example.labyrinthapp;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Collections;

public class GameScreenFragment extends Fragment {
    View rootView;
    private LabyrinthView labyrinthView;
    private TextView temperatureView;
    private TextView timerView;
    private TextView levelView;
    private int timeCounter = 0;
    private boolean gameFinished = false;
    private int rows = 2;
    private int cols = 2;
    private int[][] labyrinth;
    private MediaPlayer musicPlayer;
    static GameScreenFragment instance;

    public GameScreenFragment() {
        instance = this;

        musicPlayer = MediaPlayer.create(MainActivity.getInstance(), R.raw.life_of_a_wandering_wizard);
        musicPlayer.setLooping(true);
        musicPlayer.setVolume(0.4f, 0.4f);
    }

    static GameScreenFragment getInstance() {
        if(GameScreenFragment.instance == null)
            return new GameScreenFragment();

        return instance;
    }
    //TODO button designs

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

    @Override
    public void onResume() {
        super.onResume();

        if(MainActivity.getInstance().getSoundOn())
            musicPlayer.start();

        PlayerController.getInstance().resetDirection();
    }

    @Override
    public void onPause() {
        super.onPause();

        if(MainActivity.getInstance().getSoundOn())
            musicPlayer.pause();
    }

    public void checkIfMusicPlay() {

        if(MainActivity.getInstance().getSoundOn())
            musicPlayer.start();
        else
            musicPlayer.pause();
    }

    public void sendLabyrinthToView() {
        labyrinthView.setLabyrinth(labyrinth);
    }

    public void setLabyrinthSize(int rows, int cols){
        this.rows = rows;
        this.cols = cols;
    }

    public void generateLabyrinth() {
        int expandedRows = 2 * rows + 1;
        int expandedCols = 2 * cols + 1;

        labyrinth = new int[expandedRows][expandedCols];
        for (int i = 0; i < expandedRows; i++) {
            Arrays.fill(labyrinth[i], 1);
        }
        // start und ziel setzzen
        labyrinth[0][1] = 2;
        labyrinth[expandedRows - 1][expandedCols - 2] = 2;

        // Erzeuge ein Labyrinth im inneren Bereich
        labyrinth[1][1] = 0;
        carvePassages(1, 1);
    }

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

    public void setTemperature(String temperature) {
        MainActivity.getInstance().displayStatus(temperatureView, temperature);
    }
    public void setTimer() {
        if(!gameFinished) {
            String timerStr = Integer.toString(timeCounter);
            MainActivity.getInstance().displayStatus(timerView, timerStr);
        }
    }

    public void setLevel(int level) {
        String levelStr = Integer.toString(level);
        MainActivity.getInstance().displayStatus(levelView, levelStr);
    }

    public void increaseCounter(){
        if(!gameFinished) {
            timeCounter++;
            Log.d("MQTT", "COUNTER: " + timeCounter);
        }
    }

    public void setGameFinished(boolean finished) {
        gameFinished = finished;
    }

    public boolean getGameFinished() {
        return gameFinished;
    }

    public int getTime() {
        return timeCounter;
    }

    public MediaPlayer getBackgroundMusicMediaPlayer() {
        return musicPlayer;
    }
}