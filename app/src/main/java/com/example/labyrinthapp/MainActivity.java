package com.example.labyrinthapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
public class MainActivity extends AppCompatActivity {
    private enum ScreenEnum {
        STARTSCREEN, GAMESCREEN
    }
    ScreenEnum screenEnum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, StartScreenFragment.class, null)
                    .commit();
        }
        screenEnum = ScreenEnum.STARTSCREEN;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onPlayButtonClick(View view) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, GameScreenFragment.class, null)
                .commit();
        screenEnum = ScreenEnum.GAMESCREEN;
    }

    public void onHomeButtonClick(View view) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, StartScreenFragment.class, null)
                .commit();
        screenEnum = ScreenEnum.STARTSCREEN;
    }

    public void onSettingsClick(View view) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_container_view, SettingsFragment.class, null)
                .commit();
    }

    public void onCloseClick(View view) {
        if(screenEnum == ScreenEnum.STARTSCREEN) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container_view, StartScreenFragment.class, null)
                    .commit();
        }
        else {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container_view, GameScreenFragment.class, null)
                    .commit();
        }
    }

    public void onBestenlisteClick(View view) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_container_view, BestenlisteFragment.class, null)
                .commit();
    }
}