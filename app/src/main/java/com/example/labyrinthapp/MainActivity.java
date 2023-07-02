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

/**
 * The MainActivity (Singleton) class is the entry point of the application.
 * It manages user interaction and controls which fragments should be displayed.
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //TODO strings etc in xml files
    /**
     * Enumeration for representing the current screen/fragment the application displays.
     */
    public enum ScreenEnum {
        STARTSCREEN, GAMESCREEN, SETTINGSSCREEN, BESTENLISTESCREEN
    }
    ScreenEnum currentScreen = ScreenEnum.STARTSCREEN;
    ScreenEnum lastScreen;

    /**
     * Enumeration for representing the current sensor source of the movement data.
     */
    //TODO rename?
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

    /**
     * Constructs an instance of the MainActivity class.
     */
    public MainActivity() {
        instance = this;
    }

    /**
     * Gets an instance of the MainActivity class.
     * @return An instance of the MainActivity class.
     */
    public static MainActivity getInstance() {
        if(instance == null)
            return new MainActivity();
        return instance;
    }

    /**
     * Initializes variables and sensors and sets the StartScreenFragment
     * as first fragment to display.
     *
     * @param savedInstanceState A Bundle object containing the saved state of the activity.
     * Used to restore the activity after it has been terminated or restarted.
     */
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

    /**
     * Called when the activity is started or resumed.
     * Reconnects to MQTT if the sensor source is MPU6050.
     * Registers senor listeners and restarts timers.
     */
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

    /**
     * Called when the activity is paused or stopped.
     * Disconnects from MQTT if the sensor source is MPU6050.
     * Unregisters senor listeners and stops timers.
     */
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

    /**
     * Disables the backPress gesture.
     */
    @Override
    public void onBackPressed() {
    }

    /**
     * Gets the current screen/fragment the application displays.
     * @return The current screen/fragment the application displays.
     */
    public ScreenEnum getCurrentScreen() {
        return currentScreen;
    }

    /**
     * Gets the current sensor source of the movement data.
     * @return The current sensor source of the movement data.
     */
    public InputMethodEnum getInputMethod() {
        return inputMethod;
    }

    /**
     * Handles the click event on the "Play" button in the user interface.
     * This method is called when the "Play" button is clicked and performs the necessary actions
     * to start the game, such as validating the player's name, transitioning to the game screen and
     * resetting the level.
     *
     * @param view The view that was clicked (the "Play" button).
     */
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

    /**
     * Handles the click event on the "Home" button in the user interface.
     * This method is called when the "Home" button is clicked and performs the necessary actions
     * to navigate back to the start screen and resets the gameFinished variable to false.
     *
     * @param view The view that was clicked (the "Home" button).
     */
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

    /**
     * Handles the click event on the "Settings" button in the user interface.
     * This method is called when the "Settings" button is clicked and performs the necessary actions
     * to navigate to the settings screen, save the current screen as the last screen, pause timers
     * and update the current screen to the settings screen.
     *
     * @param view The view that was clicked (the "Settings" button).
     */
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

    /**
     * Handles the click event on the sound switch toggle button in the user interface.
     * This method is called when the sound switch button is toggled and performs the necessary actions
     * to update the sound state and check if the music should play in the game screen.
     *
     * @param view The view that was clicked (the sound switch toggle button).
     */
    public void onSwitchSound(View view) {
        soundOn = !soundOn;

        if(lastScreen == ScreenEnum.GAMESCREEN)
            GameScreenFragment.getInstance().checkIfMusicPlay();
    }

    /**
     * Handles the click event on the "Save" button in the broker settings view.
     * This method is called when the "Save" button is clicked and performs the necessary actions
     * to save the broker address and connect to the new broker.
     *
     * @param view The view that was clicked (the "Save" button).
     */
    public void onBrokerSaveClick(View view) {
        if(inputMethod != InputMethodEnum.MPU6050)
            return;

        try {
            editTextBroker = findViewById(R.id.editTextBroker);
            broker = editTextBroker.getText().toString();
            if (broker.length() == 0) {
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
            Log.d("MQTT", "Could not connect to broker.");
        }
    }

    /**
     * Handles the click event on the "Close" button in the settings view.
     * This method is called when the "Close" button is clicked and performs the necessary actions
     * to close the settings fragment and return to the previous screen.
     * If the previous screen was GameScreenFragment, it unpauses timers.
     *
     * @param view The view that was clicked (the "Close" button).
     */
    public void onCloseClick(View view) {
        currentScreen = lastScreen;
        getSupportFragmentManager().popBackStack();

        if(currentScreen == ScreenEnum.GAMESCREEN && inputMethod == InputMethodEnum.SMARTPHONESENSOR) {
            if(tempTimer != null)
                startTemperatureTimer();
        }
    }

    /**
     * Handles the click event on the "Leaderboard" button in the view.
     * This method is called when the "Leaderboard" button is clicked and performs the necessary actions
     * to navigate to the "Leaderboard" screen.
     *
     * @param view The view that was clicked (the ""Leaderboard"" button).
     */
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

    /**
     * Handles the click event on the "MPU6050" radio button in the view.
     * This method is called when the "MPU6050" radio button is clicked and switches the sensor source
     * to MPU6050.
     *
     * @param view The view that was clicked (the "MPU" radio button).
     */
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

    /**
     * Handles the click event on the "Smartphone Sensors" radio button in the view.
     * This method is called when the "Smartphone Sensors" radio button is clicked and
     * switches the sensor source to Smartphone Sensors.
     *
     * @param view The view that was clicked (the "Smartphone Sensors" radio button).
     */
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
        //TODO javadoc anpassen
        //mqttHandler.publish(pub_topic, "Game Finished");

        GameScreenFragment.getInstance().setGameFinished(true);

        SQLiteHandler sqLiteHandler = new SQLiteHandler(this);

        String name = StartScreenFragment.getInstance().getPlayerName();

        sqLiteHandler.addPlayer(name, PlayerController.getInstance().getLevel() - 1, GameScreenFragment.getInstance().getTime());
        onBestenlisteClick(null);
    }

    public void onLabyrinthFinished() {
        if (inputMethod == InputMethodEnum.MPU6050) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mqttHandler.publish(pub_topic, "Labyrinth Finished");
                }
            }).start();
        }
    }

    /**
     * Handles the click event on the "Resign" button in the view.
     * This method is called when the "Resign" button is clicked.
     * It ends the game and stops the music if sound is turned on.
     *
     * @param view The view that was clicked (the "Resign" button).
     */
    public void onResignClick(View view) {
        onGameFinished();
        if(soundOn){
            MediaPlayer mp = GameScreenFragment.getInstance().getBackgroundMusicMediaPlayer();
            mp.stop();
        }
    }

    /**
     * Handles the click event on the "Restart" button in the view.
     * This method is called when the "Restart" button is clicked.
     * It restarts the game and resets the level and timers.
     *
     * @param view The view that was clicked (the "Restart" button).
     */
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

    /**
     * Called when there is a change in sensor values.
     * This method is responsible for handling sensor changes and updating the player's movement
     * based on the sensor readings.
     * It only sends the data to PlayerController every SENSOR_UPDATE_INTERVAL.
     *
     * @param sensorEvent The sensor event containing the updated sensor values.
     */
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

    /**
     * Retrieves the CPU temperature.
     * This method reads the CPU temperature from the system and returns it in degrees Celsius.
     *
     * @return The CPU temperature in degrees Celsius, or 0.0 if it could not be retrieved.
     */
    private float getCpuTemperature() {
        Process process;
        try {
            process = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/temp");
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            float temp = Float.parseFloat(line) / 1000.0f;
            reader.close();
            return temp;
        } catch (Exception e) {
            Log.d("CPU Temperature", "Could not read CPU temperature.");
            e.printStackTrace();
        }
        return 0.0f;
    }

    /**
     * Starts a timer to periodically update the CPU temperature and play timer on the game screen.
     * The timer updates every 1000ms.
     */
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

    /**
     * Called when the accuracy of a sensor has changed.
     *
     * @param sensor   The Sensor which has changed.
     * @param accuracy The new accuracy value of the sensor.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    /**
     * Display the given data in the given TextView.
     * This has to be done with runOnUiThread.
     *
     * @param textView The textView which should display the given data.
     * @param data The data which should be displayed in the given textView.
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

    /**
     * Displays a toast message with the given String.
     *
     * @param msg The message which should be displayed in the toast message.
     */
    public void displayToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Plays the connection sound if sound is enabled.
     */
    public void playConnectionSound() {
        if(!soundOn)
            return;

        MediaPlayer mp = MediaPlayer.create(this, R.raw.connection);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mp.release();
            }
        });
        mp.start();
    }

    /**
     * Gets a boolean indicating weather sound is enabled or not.
     * @return A flag indicating whether sound is enabled or not.
     */
    public boolean getSoundOn() {return soundOn;}

    /**
     * Gets the broker address.
     * @return A String holding the broker address.
     */
    public String getBrokerAddress() { return broker; }

    /**
     * Gets the current sensor source (MPU6050 or Smartphone Sensors).
     * @return The current sensor source (MPU6050 or Smartphone Sensors).
     */
    public InputMethodEnum getSensorSource() { return inputMethod; }
}