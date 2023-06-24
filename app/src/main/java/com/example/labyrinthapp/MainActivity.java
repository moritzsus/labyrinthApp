package com.example.labyrinthapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
    }
    ScreenEnum currentScreen;
    ScreenEnum lastScreen;

    private enum InputMethodEnum {
        MPU6050, SMARTPHONESENSOR
    }
    InputMethodEnum inputMethod;

    private static final String mpu_sub_topic = "mpu/M03";
    private static final String temp_sub_topic = "temp/M03";
    private static final String pub_topic = "finished/M03";
    private int qos = 0;
    private String clientId;
    private MemoryPersistence persistence = new MemoryPersistence();
    private MqttClient client;
    private String TAG = MainActivity.class.getSimpleName();
    //TODO die IP-Adresse bitte in SharedPreferences (und über Menü änderbar)
    private String BROKER = "tcp://broker.emqx.io:1883";
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

        Log.d(TAG, "First ONCREATE");

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, StartScreenFragment.class, null)
                    .commit();
        }
        currentScreen = ScreenEnum.STARTSCREEN;
        inputMethod = InputMethodEnum.MPU6050;

        // Initialisiere den SensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Überprüfe, ob das Gerät einen Rotationssensor hat
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(inputMethod == InputMethodEnum.MPU6050) {
            connect(BROKER);
            subscribe(mpu_sub_topic);
            subscribe(temp_sub_topic);
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
            disconnect(mpu_sub_topic, temp_sub_topic);
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
        connect(BROKER);
        subscribe(mpu_sub_topic);
        subscribe(temp_sub_topic);
    }

    public void onSDClick(View view) {
        if(inputMethod == InputMethodEnum.SMARTPHONESENSOR) return;

        inputMethod = InputMethodEnum.SMARTPHONESENSOR;
        disconnect(mpu_sub_topic, temp_sub_topic);
    }

    public void onGameFinished() {
        publish(pub_topic, "Game Finished");
    }

    /**
     * Connect to broker and
     * @param broker Broker to connect to
     */
    private void connect (String broker) {
        try {
            clientId = MqttClient.generateClientId();
            client = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            Log.d(TAG, "Connecting to broker: " + broker);
            client.connect(connOpts);
            Log.d(TAG, "Connected with broker: " + broker);
        } catch (MqttException me) {
            Log.e(TAG, "Reason: " + me.getReasonCode());
            Log.e(TAG, "Message: " + me.getMessage());
            Log.e(TAG, "localizedMsg: " + me.getLocalizedMessage());
            Log.e(TAG, "cause: " + me.getCause());
            Log.e(TAG, "exception: " + me);
        }
    }

    /**
     * Subscribes to a given topic
     * @param topic Topic to subscribe to
     */
    private void subscribe(String topic) {
        try {
            client.subscribe(topic, qos, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage msg) throws Exception {
                    if(currentScreen != ScreenEnum.GAMESCREEN)
                        return;
                    String message = new String(msg.getPayload());
                    String[] values = message.split(",");

                    // 6 sind bewegungssensoren, 2 temp und counter
                    if(values.length == 6) {
                        float x = Float.parseFloat(values[3]);
                        float y = Float.parseFloat(values[4]);

                        PlayerController.getInstance().movePlayer(x, y);
                    }
                }
            });
            Log.d(TAG, "subscribed to topic " + topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unsubscribe from default topic (please unsubscribe from further
     * topics prior to calling this function)
     */
    private void disconnect(String mpu_topic, String temp_topic) {
        try {
            client.unsubscribe(mpu_topic);
            client.unsubscribe(temp_topic);
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        try {
            Log.d(TAG, "Disconnecting from broker");
            client.disconnect();
            Log.d(TAG, "Disconnected.");
        } catch (MqttException me) {
            Log.e(TAG, me.getMessage());
        }
    }

    /**
     * Publishes a message via MQTT (with fixed topic)
     * @param topic topic to publish with
     * @param msg message to publish with publish topic
     */
    private void publish(String topic, String msg) {
        MqttMessage message = new MqttMessage(msg.getBytes());
        message.setQos(qos);
        try {
            client.publish(topic, message);
            Log.d(TAG, "PUBLISHED FINISH");
        } catch (MqttException e) {
            e.printStackTrace();
        }
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