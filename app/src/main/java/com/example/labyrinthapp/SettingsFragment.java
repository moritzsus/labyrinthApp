package com.example.labyrinthapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

public class SettingsFragment extends Fragment {
    RadioButton smartphonesensor;
    RadioButton mpu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_settings, container, false);

        smartphonesensor = rootview.findViewById(R.id.radioButtonSPSens);
        mpu = rootview.findViewById(R.id.radioButtonMpu);

        if(MainActivity.getInstance().getInputMethod() == MainActivity.InputMethodEnum.SMARTPHONESENSOR) {
            smartphonesensor.setChecked(true);
            mpu.setChecked(false);
        }
        else {
            smartphonesensor.setChecked(false);
            mpu.setChecked(true);
        }
        //TODO broker adresse mit ip einstellbar
        //TODO disable edittext when not mpu
        // verhindert durchklicken auf darunterliegendes Fragment
        rootview.requestFocus();
        return rootview;
    }
}