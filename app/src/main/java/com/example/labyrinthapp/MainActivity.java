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
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private enum ScreenEnum {
        STARTSCREEN, GAMESCREEN
    }
    ScreenEnum currentScreen;

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
    // die IP-Adresse bitte in SharedPreferences und über Menü änderbar
    private String BROKER = "tcp://broker.emqx.io:1883";
    private SensorManager sensorManager;
    private Sensor gyroSensor;
    private long lastSensorUpdate = 0;
    private final long SENSOR_UPDATE_INTERVAL = 500; // Intervall in Millisekunden
    PlayerController playerController;



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
        super.onPause();
        if(inputMethod == InputMethodEnum.MPU6050) {
            disconnect(mpu_sub_topic, temp_sub_topic);
        }
        sensorManager.unregisterListener(this);
    }

    public void onPlayButtonClick(View view) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, GameScreenFragment.class, null)
                .commit();

        currentScreen = MainActivity.ScreenEnum.GAMESCREEN;
    }

    public void onHomeButtonClick(View view) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, StartScreenFragment.class, null)
                .commit();
        currentScreen = ScreenEnum.STARTSCREEN;
    }

    public void onSettingsClick(View view) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_container_view, SettingsFragment.class, null)
                .commit();
    }

    public void onCloseClick(View view) {
        if(currentScreen == ScreenEnum.STARTSCREEN) {
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

    public void onGameFinished(View view) {
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
                    String message = new String(msg.getPayload());
                    Log.d(TAG, "Message with topic " + topic + " arrived: " + message);
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
        if(currentScreen == ScreenEnum.GAMESCREEN) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSensorUpdate >= SENSOR_UPDATE_INTERVAL) {
                    float x = sensorEvent.values[0];
                    float y = sensorEvent.values[1];
                    float z = sensorEvent.values[2];

                    PlayerController.getInstance().movePlayer(x, y, z);
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