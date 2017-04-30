package com.sjsu.obdreader;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.sjsu.obdreader.model.VehicleLog;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

/**
 * Created by pavanibaradi on 4/22/17.
 */
public class MQTTHelper {
    public static String topic = "com.sjsu.obdreader.vehicle";
    public static String clientId = "obdReader";
    private static MqttClient mqttClient = null;
    private static String brokerHostName = Constants.BROKER_HOST_NAME;
    private static String brokerPortNumber = Constants.BROKER_PORT;
    private static Context context;

    public MQTTHelper(Context c) {
        mqttClient = getClient(c);
    }

    private static MqttClient defineConnectionToBroker() {
        String mqttConnSpec = "tcp://" + brokerHostName + ":" + brokerPortNumber;
        MqttClientPersistence persistence = new MqttDefaultFilePersistence(context.getApplicationContext().getApplicationInfo().dataDir);

        try {
            // define the connection to the broker
            mqttClient = new MqttClient(mqttConnSpec, clientId, persistence);
            mqttClient.connect();
        } catch (MqttException e) {
            // something went wrong!
            disconnectFromBroker();

        }
        return mqttClient;
    }

    private static void disconnectFromBroker() {
        try {
            if (mqttClient != null) {
                mqttClient.close();
            }
        } catch (MqttPersistenceException e) {
            Log.e("mqtt", "disconnect failed - persistence exception", e);
        } catch (MqttException e) {
            e.printStackTrace();
        } finally {
            mqttClient = null;
        }
    }

    public MqttClient getClient(Context c) {
        context = c;
        if (null != mqttClient) {
            return mqttClient;
        } else return defineConnectionToBroker();
    }

    public void publishData(VehicleLog vehicleLog) {
        MqttMessage message = new MqttMessage();
        Gson gson = new Gson();
        message.setPayload(gson.toJson(vehicleLog).getBytes());
        try {
            if (mqttClient != null)
                mqttClient.publish(topic, message);
            else {
                defineConnectionToBroker();
                mqttClient.publish(topic, message);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
