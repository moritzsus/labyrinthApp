package com.example.labyrinthapp;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttHandler {
    // class based on moodle template
    private MqttClient client;
    private final MemoryPersistence persistence = new MemoryPersistence();
    private final int qos = 0;
    private String broker = "-";
    boolean firstMsg = true;

    /**
     * Connect to broker.
     * The broker address should be set with setBroker() before connecting.
     */
    public void connect () {
        try {
            String clientId = MqttClient.generateClientId();
            client = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            Log.d("MQTT", "Connecting to broker: " + broker);
            client.connect(connOpts);
            Log.d("MQTT", "Connected with broker: " + broker);
            MainActivity.getInstance().playConnectionSound();
            MainActivity.getInstance().displayToast("Connected to broker.");
        } catch (MqttException me) {
            Log.e("MQTT", "Reason: " + me.getReasonCode());
            Log.e("MQTT", "Message: " + me.getMessage());
            Log.e("MQTT", "localizedMsg: " + me.getLocalizedMessage());
            Log.e("MQTT", "cause: " + me.getCause());
            Log.e("MQTT", "exception: " + me);
            MainActivity.getInstance().displayToast("Could not connect to broker.");
        }
        catch (Exception e) {
            MainActivity.getInstance().displayToast("Could not connect to broker.");
        }
    }

    /**
     * Subscribes to a given topic.
     * @param topic Topic to subscribe to.
     */
    public void subscribe(String topic) {
        try {
            client.subscribe(topic, qos, (topic1, msg) -> {
                if(MainActivity.getInstance().getCurrentScreen() != MainActivity.ScreenEnum.GAMESCREEN)
                    return;
                if(GameScreenFragment.getInstance().getGameFinished())
                    return;

                if(firstMsg){
                    firstMsg = false;
                    return;
                }
                String message = new String(msg.getPayload());
                String[] values = message.split(",");

                // length 6 = MPU6050 sensor data
                if(values.length == 6) {
                    float x = Float.parseFloat(values[3]);
                    float y = Float.parseFloat(values[4]);

                    PlayerController.getInstance().movePlayer(x, y);
                }
                //length 2 = counter and temperature
                if(values.length == 2) {
                    String temp = values[1].trim().substring(0, 4);

                    GameScreenFragment gamescreen = GameScreenFragment.getInstance();
                    gamescreen.setTemperature(temp);
                    gamescreen.increaseCounter();
                    gamescreen.setTimer();
                }
            });
            Log.d("MQTT", "subscribed to topic " + topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unsubscribe from default topic (please unsubscribe from further
     * topics prior to calling this function).
     */
    public void disconnect(String mpu_topic, String temp_topic) {
        try {
            client.unsubscribe(mpu_topic);
            client.unsubscribe(temp_topic);
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e("MQTT", e.getMessage());
        }
        try {
            Log.d("MQTT", "Disconnecting from broker");
            client.disconnect();
            Log.d("MQTT", "Disconnected.");
        } catch (MqttException me) {
            Log.e("MQTT", me.getMessage());
        }
    }

    /**
     * Publishes a message via MQTT (with fixed topic).
     * @param topic topic to publish with.
     * @param msg message to publish with publish topic.
     */
    public void publish(String topic, String msg) {
        MqttMessage message = new MqttMessage(msg.getBytes());
        message.setQos(qos);
        try {
            client.publish(topic, message);
            Log.d("MQTT", "Published message.");
        } catch (MqttException e) {
            Log.d("MQTT", "PUBLISHED FAILED");
            e.printStackTrace();
        }
    }

    /**
     * Sets the brokerAddress to the given String.
     * @param brokerAddress The brokerAddress.
     */
    public void setBroker(String brokerAddress) {
        broker = brokerAddress;
    }

    /**
     * Resets the firstMsg variable to true.
     */
    public void resetFirstRead() {
        firstMsg = true;
    }

    /**
     * Sets the this.firstMsg flag.
     * @param firstMsg Flag for setting this.firstMsg.
     */
    public void setFirstMsg(boolean firstMsg) {
        this.firstMsg = firstMsg;
    }
}
