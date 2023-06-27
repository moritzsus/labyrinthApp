package com.example.labyrinthapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class BestenlisteFragment extends Fragment {
    private static BestenlisteFragment instance;
    View rootview;
    ListView listViewLeaderboard;
    ListView listViewLastRun;

    public BestenlisteFragment() {
        instance = this;
    }

    public static BestenlisteFragment getInstance() {
        if(instance == null)
            return new BestenlisteFragment();
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //TODO restart button?
        rootview = inflater.inflate(R.layout.fragment_bestenliste, container, false);
        listViewLeaderboard = rootview.findViewById(R.id.listViewLeaderboard);
        listViewLastRun = rootview.findViewById(R.id.listViewLastRun);

        if(GameScreenFragment.getInstance().getGameFinished()) {
            listViewLastRun.setVisibility(View.VISIBLE);
        }
        else {
            listViewLastRun.setVisibility(View.INVISIBLE);
        }
        // verhindert durchklicken auf darunterliegendes Fragment
        rootview.requestFocus();
        displayLeaderboard();
        return rootview;
    }

    public void displayLeaderboard() {
        SQLiteHandler sqLiteHandler = new SQLiteHandler(MainActivity.getInstance());
        List<Player> topPlayers = sqLiteHandler.getTop10Players();
        Log.d("F", "PLAYERS: " + topPlayers);

        //ArrayAdapter playerAdapter = new ArrayAdapter<Player>(MainActivity.getInstance(), android.R.layout.simple_list_item_1, topPlayers);
        PlayerAdapter playerAdapter = new PlayerAdapter(MainActivity.getInstance(), android.R.layout.simple_list_item_1, topPlayers);
        listViewLeaderboard.setAdapter(playerAdapter);

        // display last player run, if leaderboard opens after game
        if(GameScreenFragment.getInstance().getGameFinished()) {
            Player lastPlayer = new Player(StartScreenFragment.getInstance().getPlayerName(), PlayerController.getInstance().getLevel(), GameScreenFragment.getInstance().getTime());
            List<Player> lastPlayerList = new ArrayList<>();
            lastPlayerList.add(lastPlayer);

            PlayerAdapter playerAdapterLastRun = new PlayerAdapter(MainActivity.getInstance(), android.R.layout.simple_list_item_1, lastPlayerList);
            listViewLastRun.setAdapter(playerAdapterLastRun);
        }
    }
}