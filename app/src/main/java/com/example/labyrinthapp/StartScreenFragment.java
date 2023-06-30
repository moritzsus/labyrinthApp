package com.example.labyrinthapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A singleton Fragment that displays the start screen.
 * It allows the user to enter their name and access other functionalities.
 */
public class StartScreenFragment extends Fragment {
    View rootview;
    private EditText editTextName;
    //TODO fix or delete2
    //TODO only red border if no input
    private String playerName = "NAME";
    static StartScreenFragment instance;

    /**
     * Constructs an instance of the StartScreenFragment class.
     */
    public StartScreenFragment() {
        instance = this;
    }

    /**
     * Gets an instance of the StartScreenFragment class.
     * @return An instance of the StartScreenFragment class.
     */
    public static StartScreenFragment getInstance() {
        if(instance == null)
            return new StartScreenFragment();
        return instance;
    }

    /**
     * Creates the views needed to display the start screen and initializes
     * their corresponding view variables.
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
        rootview = inflater.inflate(R.layout.fragment_start_screen, container, false);
        editTextName = rootview.findViewById(R.id.editTextName);
        return rootview;
    }

    /**
     * Gets the name EditText in which the player's name is entered.
     * @return The name EditText in which the player's name is entered.
     */
    public EditText getNameEditText() {
        return editTextName;
    }

    /**
     * Sets the name entered in the start screen.
     * @param name The name entered in the start screen.
     */
    public void setPlayerName(String name) {
        this.playerName = name;
    }

    /**
     * Gets the player's name entered in the start screen.
     * @return The player's name entered in the start screen.
     */
    public String getPlayerName() {
        return playerName;
    }
}