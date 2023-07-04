package com.example.labyrinthapp;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * A fragment that displays the settings.
 * It allows the user to configure various settings.
 */
public class SettingsFragment extends Fragment {
    RadioButton smartphonesensor;
    RadioButton mpu;
    SwitchMaterial soundSwitch;
    EditText brokerAddress;
    ImageView saveButton;
    @SuppressLint("StaticFieldLeak") // see documentation
    static SettingsFragment instance;

    /**
     * Constructs an instance of the SettingsFragment class.
     */
    public SettingsFragment() {
        instance = this;
    }

    /**
     * Gets an instance of the SettingsFragment class.
     * @return An instance of the SettingsFragment class.
     */
    static SettingsFragment getInstance() {
        if(SettingsFragment.instance == null)
            return new SettingsFragment();

        return instance;
    }

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
        brokerAddress = rootview.findViewById(R.id.editTextBroker);
        brokerAddress.setText(MainActivity.getInstance().getBrokerAddress());
        saveButton = rootview.findViewById(R.id.buttonSave);

        if(MainActivity.getInstance().getSensorSource() == MainActivity.SensorSource.SMARTPHONESENSOR) {
            smartphonesensor.setChecked(true);
            mpu.setChecked(false);
            brokerAddress.setEnabled(false);
            brokerAddress.setBackgroundResource(R.drawable.rounded_edittext_background_disabled);
            saveButton.setVisibility(View.INVISIBLE);
        }
        else {
            smartphonesensor.setChecked(false);
            mpu.setChecked(true);
            brokerAddress.setEnabled(true);
            brokerAddress.setBackgroundResource(R.drawable.rounded_edittext_background_enabled);
            saveButton.setVisibility(View.VISIBLE);
        }

        soundSwitch.setChecked(MainActivity.getInstance().getSoundOn());

        rootview.requestFocus();
        return rootview;
    }

    /**
     * Sets the visibility of the save button.
     * @param visible Tells the method if the save button should be visible or not.
     */
    public void setSaveButtonVisibility(boolean visible) {
        if(visible)
            saveButton.setVisibility(View.VISIBLE);
        else
            saveButton.setVisibility(View.INVISIBLE);
    }
}