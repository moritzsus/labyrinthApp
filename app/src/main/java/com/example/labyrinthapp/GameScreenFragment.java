package com.example.labyrinthapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;

public class GameScreenFragment extends Fragment {
    View rootView;
    private LabyrinthView labyrinthView;
    private int rows = 12;
    private int cols = 12;
    private int[][] labyrinth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_game_screen, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        labyrinthView = rootView.findViewById(R.id.labyrinthView);

        generateLabyrinth();
        labyrinthView.setLabyrinth(labyrinth);
        labyrinthView.invalidate();
    }

    private void generateLabyrinth() {
        int expandedRows = 2 * rows + 1;
        int expandedCols = 2 * cols + 1;

        labyrinth = new int[expandedRows][expandedCols];
        for (int i = 0; i < expandedRows; i++) {
            Arrays.fill(labyrinth[i], 1);
        }

        labyrinth[0][1] = 0;
        labyrinth[expandedRows - 1][expandedCols - 2] = 0;

        // Erzeuge ein Labyrinth im inneren Bereich
        labyrinth[1][1] = 0;
        carvePassages(1, 1);

        // Setze das Ziel
        labyrinth[expandedRows - 2][expandedCols - 2] = 0;
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
}