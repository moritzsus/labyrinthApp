package com.example.labyrinthapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.tv.BroadcastInfoRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public enum ScreenEnum {
        STARTSCREEN, GAMESCREEN, SETTINGSSCREEN, BESTENLISTESCREEN
        //TODO renam bestenlsite?
    }
    ScreenEnum currentScreen = ScreenEnum.STARTSCREEN;;
    //TODO lastScreen löschen?
    ScreenEnum lastScreen;

    public enum InputMethodEnum {
        MPU6050, SMARTPHONESENSOR
    }
    InputMethodEnum inputMethod = InputMethodEnum.SMARTPHONESENSOR;;
    private String TAG = MainActivity.class.getSimpleName();

    //TODO change topics to M02
    MqttHandler mqttHandler;
    private static final String mpu_sub_topic = "mpu/M03";
    private static final String temp_sub_topic = "temp/M03";
    private static final String pub_topic = "finished/M03";
    //TODO die IP-Adresse bitte in SharedPreferences (und über Menü änderbar)
    //TODO BROKER -> broker?
    private String BROKER = "tcp://broker.emx.io:1883";
    private EditText editTextBroker;
    private SensorManager sensorManager;
    private Sensor gyroSensor;
    private long lastSensorUpdate = 0;
    private final long SENSOR_UPDATE_INTERVAL = 500; // Intervall in Millisekunden
    private EditText nameText;
    static private MainActivity instance;
    //TODO falls Zeit, ingame sound

    public MainActivity() {
        if(instance == null)
            instance = this;
    }

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

        mqttHandler = new MqttHandler();
        mqttHandler.setBroker(BROKER);
        // Initialisiere den SensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Überprüfe, ob das Gerät einen Rotationssensor hat
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(inputMethod == InputMethodEnum.MPU6050) {
            mqttHandler.connect();
            mqttHandler.subscribe(mpu_sub_topic);
            mqttHandler.subscribe(temp_sub_topic);
        }
        if (gyroSensor != null) {
            sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        //TODO Handydaten Pausieren/resumen
        super.onPause();
        if(inputMethod == InputMethodEnum.MPU6050) {
            mqttHandler.disconnect(mpu_sub_topic, temp_sub_topic);
        }
        sensorManager.unregisterListener(this);
    }

    public static MainActivity getInstance() {
        if(instance == null)
            return new MainActivity();
        return instance;
    }

    public ScreenEnum getCurrentScreen() {
        return currentScreen;
    }
    public InputMethodEnum getInputMethod() { return inputMethod; }

    public void onPlayButtonClick(View view) {
        EditText name = StartScreenFragment.getInstance().getNameEditText();
        String nametxt = name.getText().toString();
        if(nametxt.length() == 0) {
            return;
        }
        StartScreenFragment.getInstance().setNameString(nametxt);

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, GameScreenFragment.class, null)
                .addToBackStack(null)
                .commit();

        currentScreen = MainActivity.ScreenEnum.GAMESCREEN;
    }

    public void onHomeButtonClick(View view) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, StartScreenFragment.class, null)
                .addToBackStack(null)
                .commit();

        //TODO fix or delete
        //String name = StartScreenFragment.getInstance().getNameString();
        //Log.d("...", "NAME: " + name);
        //EditText nameEditText = StartScreenFragment.getInstance().getNameEditText();
        //nameEditText.setText("TESTNAME");
        currentScreen = ScreenEnum.STARTSCREEN;
    }

    public void onSettingsClick(View view) {
        lastScreen = currentScreen;

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_container_view, SettingsFragment.class, null)
                .addToBackStack(null)
                .commit();

        currentScreen = ScreenEnum.SETTINGSSCREEN;
    }

    public void onBrokerSaveClick(View view) {
        editTextBroker = findViewById(R.id.editTextBroker);
        BROKER = editTextBroker.getText().toString();
        String test = editTextBroker.getText().toString();
        boolean same = (test.equals(BROKER));
        //TODO darf kein leerer String sein?
        //TODO test wenn init broker wert ungültig
        mqttHandler.setBroker(BROKER);
        Log.d("SAVE", "TEST: " + test);
        Log.d("SAVE", "BROKER: " + BROKER.length());
        Log.d("SAVE", "SAME?: " + same);

         //connect to new broker if inputMehod is MPU - if not, checking the radio button will automatically connect to new broker
        if(inputMethod == InputMethodEnum.MPU6050) {
            //TODO fix crash when connecting to entered broker
            mqttHandler.disconnect(mpu_sub_topic, temp_sub_topic);
            //mqttHandler = new MqttHandler();
            //BROKER = "hshd";
            //BROKER = "tcp://broker.emqx.io:1883";
            mqttHandler.setBroker(BROKER);
            mqttHandler.connect();
            mqttHandler.subscribe(mpu_sub_topic);
            mqttHandler.subscribe(temp_sub_topic);
        }
    }

    public void onCloseClick(View view) {
        currentScreen = lastScreen;
        getSupportFragmentManager().popBackStack();
    }

    public void onBestenlisteClick(View view) {
        lastScreen = currentScreen;

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_container_view, BestenlisteFragment.class, null)
                .addToBackStack(null)
                .commit();

        currentScreen = ScreenEnum.BESTENLISTESCREEN;
    }

    public void onMPUClick(View view) {
        if(inputMethod == InputMethodEnum.MPU6050) return;

        inputMethod = InputMethodEnum.MPU6050;
        mqttHandler.connect();
        mqttHandler.subscribe(mpu_sub_topic);
        mqttHandler.subscribe(temp_sub_topic);
    }

    public void onSmartphoneSensorClick(View view) {
        if(inputMethod == InputMethodEnum.SMARTPHONESENSOR) return;

        inputMethod = InputMethodEnum.SMARTPHONESENSOR;
        mqttHandler.disconnect(mpu_sub_topic, temp_sub_topic);
    }

    public void onGameFinished() {
        mqttHandler.publish(pub_topic, "Game Finished");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(inputMethod == InputMethodEnum.MPU6050)
            return;
            // TODO falls Zeit accelerometer
        if(currentScreen == ScreenEnum.GAMESCREEN) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                long currentTime = System.currentTimeMillis();
                // TODO falls Zeit, immer prüfen, nur alle 500ms an moveplayer schicken, damit bewegungen zwischen 2 ticks td erkannt werden (muss direction hier speichern?)
                if (currentTime - lastSensorUpdate >= SENSOR_UPDATE_INTERVAL) {
                    float x = sensorEvent.values[0];
                    float y = sensorEvent.values[1];

                    PlayerController.getInstance().movePlayer(x, y);
                    lastSensorUpdate = currentTime;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nicht benötigt, kann leer bleiben
    }
}