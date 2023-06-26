package com.example.labyrinthapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class StartScreenFragment extends Fragment {
    View rootview;
    private EditText editTextName;
    //TODO fix or delete2
    private String playerName = "NAME";
    static StartScreenFragment instance;

    public StartScreenFragment() {
        instance = this;
    }

    public static StartScreenFragment getInstance() {
        if(instance == null)
            return new StartScreenFragment();
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.fragment_start_screen, container, false);
        editTextName = rootview.findViewById(R.id.editTextName);
        return rootview;
    }

    public EditText getNameEditText() {
        return editTextName;
    }

    public void setPlayerName(String name) {
        this.playerName = name;
    }

    public String getPlayerName() {
        return playerName;
    }
}