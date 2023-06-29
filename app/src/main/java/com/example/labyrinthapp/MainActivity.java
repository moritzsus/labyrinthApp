package com.example.labyrinthapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.tv.BroadcastInfoRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
    private String BROKER = "tcp://broker.emqx.io:1883";
    private EditText editTextBroker;
    private SensorManager sensorManager;
    private Sensor gyroSensor;
    private long lastSensorUpdate = 0;
    private final long SENSOR_UPDATE_INTERVAL = 500; // Intervall in Millisekunden
    static private MainActivity instance;
    boolean firstSensorRead = true;
    private Timer tempTimer;
    private TimerTask tempTimerTask;

    private boolean soundOn = true;
    private boolean firstTempRead = true;
    //TODO falls Zeit, ingame sound
    //TODO alle Fragment singletons in oncreate abspeichern -> kann views erstellen

    public MainActivity() {
        instance = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    // hereadd
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
        if(currentScreen == ScreenEnum.GAMESCREEN && inputMethod == InputMethodEnum.SMARTPHONESENSOR) {
            if(tempTimer != null)
                startTemperatureTimer();
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

        if(currentScreen == ScreenEnum.GAMESCREEN && inputMethod == InputMethodEnum.SMARTPHONESENSOR) {
            if(tempTimer != null)
                tempTimer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        // Zurück-Geste unterbinden, keine Aktion ausführen
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
        StartScreenFragment.getInstance().setPlayerName(nametxt);

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, GameScreenFragment.class, null)
                .addToBackStack(null)
                .commit();

        PlayerController.getInstance().resetLevel();
        GameScreenFragment.getInstance().setGameFinished(false);

        if(inputMethod == InputMethodEnum.SMARTPHONESENSOR) {
            startTemperatureTimer();
        }

        currentScreen = MainActivity.ScreenEnum.GAMESCREEN;
    }

    public void onHomeButtonClick(View view) {
        if(currentScreen != ScreenEnum.GAMESCREEN)
            return;

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, StartScreenFragment.class, null)
                .addToBackStack(null)
                .commit();

        if(currentScreen == ScreenEnum.GAMESCREEN && inputMethod == InputMethodEnum.SMARTPHONESENSOR) {
            if(tempTimer != null) {
                tempTimer.cancel();
                firstTempRead = true;
            }
        }

        //TODO fix or delete
        //String name = StartScreenFragment.getInstance().getNameString();
        //Log.d("...", "NAME: " + name);
        //EditText nameEditText = StartScreenFragment.getInstance().getNameEditText();
        //nameEditText.setText("TESTNAME");
        currentScreen = ScreenEnum.STARTSCREEN;
    }

    public void onSettingsClick(View view) {
        if(currentScreen == ScreenEnum.SETTINGSSCREEN || currentScreen == ScreenEnum.BESTENLISTESCREEN)
            return;

        lastScreen = currentScreen;

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                //hereadd
                .add(R.id.fragment_container_view, SettingsFragment.class, null)
                .addToBackStack(null)
                .commit();

        if(currentScreen == ScreenEnum.GAMESCREEN && inputMethod == InputMethodEnum.SMARTPHONESENSOR) {
            if(tempTimer != null)
                tempTimer.cancel();
        }

        currentScreen = ScreenEnum.SETTINGSSCREEN;
    }

    public void onSwitchSound(View view) {
        soundOn = !soundOn;

        GameScreenFragment.getInstance().checkIfMusicPlay();
    }

    public void onBrokerSaveClick(View view) {
        try {
            editTextBroker = findViewById(R.id.editTextBroker);
            BROKER = editTextBroker.getText().toString();
            //TODO darf kein leerer String sein?
            //TODO test wenn init broker wert ungültig
            mqttHandler.setBroker(BROKER);

            //connect to new broker if inputMehod is MPU - if not, checking the radio button will automatically connect to new broker
            if(inputMethod == InputMethodEnum.MPU6050) {
                //TODO fix crash when connecting to entered broker
                mqttHandler.disconnect(mpu_sub_topic, temp_sub_topic);

                mqttHandler.setBroker(BROKER);
                mqttHandler.connect();
                mqttHandler.subscribe(mpu_sub_topic);
                mqttHandler.subscribe(temp_sub_topic);
            }
        }
        catch (Exception e) {
            Log.d("d", "CANNOT CONNECT TO BROKER");
        }

    }

    public void onCloseClick(View view) {
        currentScreen = lastScreen;
        getSupportFragmentManager().popBackStack();

        if(currentScreen == ScreenEnum.GAMESCREEN && inputMethod == InputMethodEnum.SMARTPHONESENSOR) {
            if(tempTimer != null)
                startTemperatureTimer();
        }
    }

    public void onBestenlisteClick(View view) {
        if(currentScreen == ScreenEnum.SETTINGSSCREEN || currentScreen == ScreenEnum.BESTENLISTESCREEN)
            return;

        lastScreen = currentScreen;

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                //hereadd
                .add(R.id.fragment_container_view, BestenlisteFragment.class, null)
                .addToBackStack(null)
                .commit();

        if(currentScreen == ScreenEnum.GAMESCREEN && inputMethod == InputMethodEnum.SMARTPHONESENSOR) {
            if(tempTimer != null)
                tempTimer.cancel();
        }

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
        //TODO resign knopf
        //TODO only when MPU is connected -> crash
        //TODO stop Timer
        //TODO Bestenliste SQLite
        //mqttHandler.publish(pub_topic, "Game Finished");

        GameScreenFragment.getInstance().setGameFinished(true);

        SQLiteHandler sqLiteHandler = new SQLiteHandler(this);

        String name = StartScreenFragment.getInstance().getPlayerName();
        Log.d("NAME", "NAME: " + name);

        //TODO error handling?
        boolean success = sqLiteHandler.addPlayer(name, PlayerController.getInstance().getLevel() - 1, GameScreenFragment.getInstance().getTime());
        onBestenlisteClick(null);

        //TODO delay before leaderboard open?
    }

    public void onResignClick(View view) {
        onGameFinished();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(inputMethod == InputMethodEnum.MPU6050 || GameScreenFragment.getInstance().getGameFinished())
            return;

            // TODO falls Zeit accelerometer
        if(currentScreen == ScreenEnum.GAMESCREEN) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                long currentTime = System.currentTimeMillis();
                // TODO falls Zeit, immer prüfen, nur alle 500ms an moveplayer schicken, damit bewegungen zwischen 2 ticks td erkannt werden (muss direction hier speichern?)
                if (currentTime - lastSensorUpdate >= SENSOR_UPDATE_INTERVAL) {
                    if(firstSensorRead) {
                        firstSensorRead = false;
                        return;
                    }
                    float x = sensorEvent.values[0];
                    float y = sensorEvent.values[1];

                    PlayerController.getInstance().movePlayer(x, y);

                    lastSensorUpdate = currentTime;
                }
            }
        }
    }

    private float getCpuTemperature() {
        Process process;
        try {
            process = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/temp");
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            //TODO geht nicht auf simulator -> try catch?
            float temp = Float.parseFloat(line) / 1000.0f;
            Log.d("s", "CPU: " + temp);
            reader.close();
            return temp;
        } catch (Exception e) {
            Log.d("s", "Catch");
            e.printStackTrace();
        }
        return 0.0f;
    }

    private void startTemperatureTimer() {
        tempTimer = new Timer();
        tempTimerTask = new TimerTask() {
            @Override
            public void run() {
                if(firstTempRead) {
                    firstTempRead = false;
                    return;
                }
                float cpuTemp = getCpuTemperature();
                String cpuTempStr = Float.toString(cpuTemp);
                GameScreenFragment.getInstance().setTemperature(cpuTempStr);
                GameScreenFragment.getInstance().increaseCounter();
                GameScreenFragment.getInstance().setTimer();
            }
        };
        tempTimer.schedule(tempTimerTask, 0, 1000);
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Nicht benötigt, kann leer bleiben
    }

    /**
     * Display data in a TextView with ID "textFeld"
     * This has to be done with runOnUiThread
     * @param data
     */
    public void displayStatus(TextView textView, String data) {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                                textView.setText(data);
                            }
                    });

                } catch (Exception e) { }
            }
        };
        t.start();
    }

    public void playConnectionSound() {
        if(!soundOn)
            return;
        //TODO sound bleibt nach level up manchmal leise
        MediaPlayer music = GameScreenFragment.getInstance().getBackgroundMusicMediaPlayer();
        music.setVolume(0.2f, 0.2f);

        MediaPlayer mp = MediaPlayer.create(this, R.raw.connection);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                music.setVolume(0.6f, 0.6f);
                mp.release();
            }
        });
        mp.start();
    }

    public boolean getSoundOn() {return soundOn;}
}