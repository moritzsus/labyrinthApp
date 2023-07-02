package com.example.labyrinthapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

//TODO Log nachrichten anpassen (mit TAGS?)
//TODO strings etc in xml files
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public enum ScreenEnum {
        STARTSCREEN, GAMESCREEN, SETTINGSSCREEN, BESTENLISTESCREEN
    }
    ScreenEnum currentScreen = ScreenEnum.STARTSCREEN;
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
    private String broker;
    private EditText editTextBroker;
    private SensorManager sensorManager;
    private Sensor gyroSensor;
    private long lastSensorUpdate = 0;
    private final long SENSOR_UPDATE_INTERVAL = 500;
    static private MainActivity instance;
    boolean firstSensorRead = true;
    private Timer tempTimer;
    private TimerTask tempTimerTask;

    private boolean soundOn;
    private boolean firstTempRead = true;

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
        broker = getResources().getString(R.string.broker_address);

        soundOn = true;
        mqttHandler = new MqttHandler();
        mqttHandler.setBroker(broker);
        // Initialisiere den SensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // uberprufe, ob das Gerat einen Rotationssensor hat
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
        // Back-Geste unterbinden, keine Aktion ausfuehren
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
            name.setBackgroundResource(R.drawable.rounded_edittext_background_error);
            MainActivity.getInstance().displayToast("Please enter your name.");
            return;
        }
        name.setBackgroundResource(R.drawable.rounded_edittext_background_error);

        StartScreenFragment.getInstance().setPlayerName(nametxt);

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, GameScreenFragment.class, null)
                .addToBackStack(null)
                .commit();

        PlayerController.getInstance().resetLevel();

        if(inputMethod == InputMethodEnum.SMARTPHONESENSOR) {
            startTemperatureTimer();
        }

        currentScreen = MainActivity.ScreenEnum.GAMESCREEN;
    }

    public void onHomeButtonClick(View view) {
        if(currentScreen != ScreenEnum.GAMESCREEN)
            return;

        GameScreenFragment.getInstance().setLabyrinthSize(8,8);
        firstSensorRead = true;

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

        GameScreenFragment.getInstance().setGameFinished(false);

        currentScreen = ScreenEnum.STARTSCREEN;
    }

    public void onSettingsClick(View view) {
        if(currentScreen == ScreenEnum.SETTINGSSCREEN || currentScreen == ScreenEnum.BESTENLISTESCREEN)
            return;

        lastScreen = currentScreen;

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
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

        if(lastScreen == ScreenEnum.GAMESCREEN)
            GameScreenFragment.getInstance().checkIfMusicPlay();
    }

    public void onBrokerSaveClick(View view) {
        if(inputMethod != InputMethodEnum.MPU6050)
            return;

        String temp = broker;
        try {
            editTextBroker = findViewById(R.id.editTextBroker);
            broker = editTextBroker.getText().toString();
            if (broker.length() == 0) {
                broker = temp;
                return;
            }
            mqttHandler.setBroker(broker);

            //connect to new broker if inputMehod is MPU - if not, checking the radio button will automatically connect to new broker
            if(inputMethod == InputMethodEnum.MPU6050) {
                mqttHandler.disconnect(mpu_sub_topic, temp_sub_topic);

                mqttHandler.setBroker(broker);
                mqttHandler.connect();
                mqttHandler.subscribe(mpu_sub_topic);
                mqttHandler.subscribe(temp_sub_topic);
            }
        }
        catch (Exception e) {
            broker = temp; // set back to default broker
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

        SettingsFragment.getInstance().setSaveButtonVisibility(true);

        editTextBroker = findViewById(R.id.editTextBroker);
        editTextBroker.setEnabled(true);
        editTextBroker.setBackgroundResource(R.drawable.rounded_edittext_background_enabled);

        inputMethod = InputMethodEnum.MPU6050;
        mqttHandler.connect();
        mqttHandler.subscribe(mpu_sub_topic);
        mqttHandler.subscribe(temp_sub_topic);
    }

    public void onSmartphoneSensorClick(View view) {
        if(inputMethod == InputMethodEnum.SMARTPHONESENSOR) return;

        SettingsFragment.getInstance().setSaveButtonVisibility(false);

        editTextBroker = findViewById(R.id.editTextBroker);
        editTextBroker.setEnabled(false);
        editTextBroker.setBackgroundResource(R.drawable.rounded_edittext_background_disabled);

        inputMethod = InputMethodEnum.SMARTPHONESENSOR;
        mqttHandler.disconnect(mpu_sub_topic, temp_sub_topic);
    }

    public void onGameFinished() {
        //TODO only when MPU is connected -> crash
        //mqttHandler.publish(pub_topic, "Game Finished");

        GameScreenFragment.getInstance().setGameFinished(true);

        SQLiteHandler sqLiteHandler = new SQLiteHandler(this);

        String name = StartScreenFragment.getInstance().getPlayerName();

        sqLiteHandler.addPlayer(name, PlayerController.getInstance().getLevel() - 1, GameScreenFragment.getInstance().getTime());
        onBestenlisteClick(null);
    }

    public void onResignClick(View view) {
        onGameFinished();
        if(soundOn){
            MediaPlayer mp = GameScreenFragment.getInstance().getBackgroundMusicMediaPlayer();
            mp.stop();
        }
    }

    public void onRestartClick(View view) {
        if(currentScreen == ScreenEnum.GAMESCREEN && inputMethod == InputMethodEnum.SMARTPHONESENSOR) {
            if(tempTimer != null) {
                tempTimer.cancel();
                firstTempRead = true;
            }
        }
        GameScreenFragment.getInstance().setGameFinished(false);

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, GameScreenFragment.class, null)
                .addToBackStack(null)
                .commit();

        PlayerController.getInstance().resetLevel();

        if(inputMethod == InputMethodEnum.SMARTPHONESENSOR) {
            startTemperatureTimer();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(inputMethod == InputMethodEnum.MPU6050 || GameScreenFragment.getInstance().getGameFinished())
            return;

        if(currentScreen == ScreenEnum.GAMESCREEN) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                long currentTime = System.currentTimeMillis();
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

    public void displayToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void playConnectionSound() {
        if(!soundOn)
            return;

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

    public String getBrokerAddress() { return broker; }

    public InputMethodEnum getSensorSource() { return inputMethod; }
}