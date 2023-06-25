package com.example.labyrinthapp;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttHandler {
    private String clientId;
    private MqttClient client;
    private MemoryPersistence persistence = new MemoryPersistence();
    private int qos = 0;
    private String broker = "x";

    public void setBroker(String brokerAddress) {
        broker = brokerAddress;
    }

    /**
     * Connect to broker and
     */
    public void connect () {
        //TODO toast anzeigen ob erfolgreich?
        try {
            clientId = MqttClient.generateClientId();
            client = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            //TODO maybe here crash (port falsch -> aufhängen)?
            connOpts.setCleanSession(true);
            //connOpts.setConnectionTimeout(2000);
            Log.d("MQTT", "Connecting to broker: " + broker);
            client.connect(connOpts);
            Log.d("MQTT", "Connected with broker: " + broker);
        } catch (MqttException me) {
            Log.e("MQTT", "Reason: " + me.getReasonCode());
            Log.e("MQTT", "Message: " + me.getMessage());
            Log.e("MQTT", "localizedMsg: " + me.getLocalizedMessage());
            Log.e("MQTT", "cause: " + me.getCause());
            Log.e("MQTT", "exception: " + me);
        }
    }

    /**
     * Subscribes to a given topic
     * @param topic Topic to subscribe to
     */
    public void subscribe(String topic) {
        try {
            client.subscribe(topic, qos, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage msg) throws Exception {
                    if(MainActivity.getInstance().getCurrentScreen() != MainActivity.ScreenEnum.GAMESCREEN)
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
            Log.d("MQTT", "subscribed to topic " + topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unsubscribe from default topic (please unsubscribe from further
     * topics prior to calling this function)
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
     * Publishes a message via MQTT (with fixed topic)
     * @param topic topic to publish with
     * @param msg message to publish with publish topic
     */
    public void publish(String topic, String msg) {
        MqttMessage message = new MqttMessage(msg.getBytes());
        message.setQos(qos);
        try {
            client.publish(topic, message);
            Log.d("MQTT", "PUBLISHED FINISH");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
