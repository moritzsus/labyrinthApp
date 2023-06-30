package com.example.labyrinthapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Switch;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Set;

public class SettingsFragment extends Fragment {
    private static SettingsFragment instance;
    RadioButton smartphonesensor;
    RadioButton mpu;
    SwitchMaterial soundSwitch;

    /**
     * Constructs an instance of the SettingsFragment class.
     */
    public SettingsFragment() {
        instance = this;
    }
    //TODO muss singleton sein??
    //TODO falls geaendert, auch javadoc anpassen

    /**
     * Creates the views needed to display the settings screen, initializes
     * their corresponding view variables and sets their states.
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
        View rootview = inflater.inflate(R.layout.fragment_settings, container, false);

        smartphonesensor = rootview.findViewById(R.id.radioButtonSPSens);
        mpu = rootview.findViewById(R.id.radioButtonMpu);
        soundSwitch = rootview.findViewById(R.id.soundSwitch);

        if(MainActivity.getInstance().getInputMethod() == MainActivity.InputMethodEnum.SMARTPHONESENSOR) {
            smartphonesensor.setChecked(true);
            mpu.setChecked(false);
        }
        else {
            smartphonesensor.setChecked(false);
            mpu.setChecked(true);
        }

        if(MainActivity.getInstance().getSoundOn())
            soundSwitch.setChecked(true);
        else
            soundSwitch.setChecked(false);

        //TODO broker adresse mit ip einstellbar
        //TODO disable edittext when not mpu
        rootview.requestFocus();
        return rootview;
    }
}