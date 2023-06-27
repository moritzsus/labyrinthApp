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

    public SettingsFragment() {
        instance = this;
    }

    public static SettingsFragment getInstance() {
        if(instance == null)
            return new SettingsFragment();
        return instance;
    }

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
        // verhindert durchklicken auf darunterliegendes Fragment
        rootview.requestFocus();
        return rootview;
    }
}