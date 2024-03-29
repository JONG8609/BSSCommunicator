package com.otosone.bssmgr.mqtt;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.otosone.bssmgr.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.android.service.MqttTraceHandler;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class MqttManager implements IMqttActionListener, MqttCallback, MqttTraceHandler {
    // Log
    private final static String TAG = MqttManager.class.getSimpleName();

    // Singleton
    public static int MqqtQos_AtMostOnce = 0;
    public static int MqqtQos_AtLeastOnce = 1;
    public static int MqqtQos_ExactlyOnce = 2;
    // Data
    private MqttAndroidClient mMqttClient;
    private WeakReference<MqttManagerListener> mWeakListener;
    private MqqtConnectionStatus mMqqtClientStatus = MqqtConnectionStatus.NONE;
    private Context mContext;

    public MqttManager(@NonNull Context context, @NonNull MqttManagerListener listener) {
        mContext = context.getApplicationContext();
        mWeakListener = new WeakReference<>(listener);
    }

    public void setListener(@Nullable MqttManagerListener listener) {
        mWeakListener = new WeakReference<>(listener);
    }

    @Override
    public void finalize() throws Throwable {

        try {
            if (mMqttClient != null) {
                mMqttClient.unregisterResources();
            }
        } finally {
            super.finalize();
        }
    }

    public MqqtConnectionStatus getClientStatus() {
        return mMqqtClientStatus;
    }

    // region MQTT
    public void subscribe(@Nullable String topic, int qos) {
        if (mMqttClient != null && mMqqtClientStatus == MqqtConnectionStatus.CONNECTED && topic != null) {
            try {

                mMqttClient.subscribe(topic, qos);
            } catch (MqttException e) {
                Log.e(TAG, "Mqtt:x subscribe error: ", e);
            }
        }
    }

    public void unsubscribe(@Nullable String topic) {
        if (mMqttClient != null && mMqqtClientStatus == MqqtConnectionStatus.CONNECTED && topic != null) {
            try {

                mMqttClient.unsubscribe(topic);
            } catch (MqttException e) {
                Log.e(TAG, "Mqtt:x unsubscribe error: ", e);
            }
        }

    }

    public void publish(@NonNull String topic, @NonNull String message, int qos) {
        publish(topic, message.getBytes(), qos);
    }

    public void publish(@Nullable String topic, byte[] payload, int qos) {
        if (mMqttClient != null && mMqqtClientStatus == MqqtConnectionStatus.CONNECTED && topic != null) {
            final boolean retained = false;

            try {

                mMqttClient.publish(topic, payload, qos, retained, null, null);
            } catch (MqttException e) {
                Log.e(TAG, "Mqtt:x publish error: ", e);
            }
        }
    }

    public void disconnect() {
        if (mMqttClient != null && mMqqtClientStatus == MqqtConnectionStatus.CONNECTED) {
            try {

//                mMqqtClientStatus = MqqtConnectionStatus.DISCONNECTING;
                mMqqtClientStatus = MqqtConnectionStatus.DISCONNECTED;      // Note: it seems that the disconnected callback is never invoked. So we fake here that the final state is disconnected
                mMqttClient.disconnect(null, this);

                mMqttClient.unregisterResources();
                mMqttClient = null;
            } catch (MqttException e) {
                Log.e(TAG, "Mqtt:x disconnection error: ", e);
            }
        }
    }

    public void connectFromSavedSettings() {
        String host = MqttSettings.getServerAddress(mContext);
        int port = MqttSettings.getServerPort(mContext);

        String username = MqttSettings.getUsername(mContext);
        String password = MqttSettings.getPassword(mContext);
        boolean cleanSession = MqttSettings.isCleanSession(mContext);
        boolean sslConnection = MqttSettings.isSslConnection(mContext);

        connect(host, port, username, password, cleanSession, sslConnection);
    }

    @SuppressWarnings("ConstantConditions")
    public void connect(@NonNull String host, int port, @Nullable String username, @Nullable String password, boolean cleanSession, boolean sslConnection) {
        String clientId = "Bluefruit_" + UUID.randomUUID().toString();
        final int timeout = MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT;
        final int keepalive = MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT;

        String message = null;
        String topic = null;
        int qos = 0;
        boolean retained = false;

        String uri;
        if (sslConnection) {
            uri = "ssl://" + host + ":" + port;

        } else {
            uri = "tcp://" + host + ":" + port;
        }


        mMqttClient = new MqttAndroidClient(mContext, uri, clientId);
        mMqttClient.registerResources(mContext);

        MqttConnectOptions conOpt = new MqttConnectOptions();

        conOpt.setCleanSession(cleanSession);
        conOpt.setConnectionTimeout(timeout);
        conOpt.setKeepAliveInterval(keepalive);
        if (username != null && username.length() > 0) {

            conOpt.setUserName(username);
        }
        if (password != null && password.length() > 0) {

            conOpt.setPassword(password.toCharArray());
        }

        boolean doConnect = true;
        if ((message != null && message.length() > 0) || (topic != null && topic.length() > 0)) {
            // need to make a message since last will is set

            try {
                conOpt.setWill(topic, message.getBytes(), qos, retained);
            } catch (Exception e) {
                Log.e(TAG, "Mqtt: Can't set will", e);
                doConnect = false;
                //callback.onFailure(null, e);
            }
        }
        mMqttClient.setCallback(this);
        mMqttClient.setTraceCallback(this);

        if (doConnect) {
            MqttSettings.setConnectedEnabled(mContext, true);

            try {

                mMqqtClientStatus = MqqtConnectionStatus.CONNECTING;
                mMqttClient.connect(conOpt, null, this);
            } catch (MqttException e) {
                Log.e(TAG, "Mqtt: connection error: ", e);
            }
        }
    }

    // region IMqttActionListener
    @Override
    public void onSuccess(IMqttToken iMqttToken) {
        MqttManagerListener listener = mWeakListener.get();
        if (mMqqtClientStatus == MqqtConnectionStatus.CONNECTING) {

            mMqqtClientStatus = MqqtConnectionStatus.CONNECTED;
            if (listener != null) {
                listener.onMqttConnected();
            }

            String topic = MqttSettings.getSubscribeTopic(mContext);
            int topicQos = MqttSettings.getSubscribeQos(mContext);
            if (MqttSettings.isSubscribeEnabled(mContext) && topic != null) {
                subscribe(topic, topicQos);
            }
        } else if (mMqqtClientStatus == MqqtConnectionStatus.DISCONNECTING) {

            mMqqtClientStatus = MqqtConnectionStatus.DISCONNECTED;
            if (listener != null) {
                listener.onMqttDisconnected();
            }
        } else {

        }
    }

    // endregion

    @Override
    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {


        // Remove the auto-connect till the failure is solved
        if (mMqqtClientStatus == MqqtConnectionStatus.CONNECTING) {
            MqttSettings.setConnectedEnabled(mContext, false);
        }

        // Set as an error
        mMqqtClientStatus = MqqtConnectionStatus.ERROR;
        String errorText = mContext.getString(R.string.mqtt_connection_failed) + ". " + throwable.getLocalizedMessage();
        Toast.makeText(mContext, errorText, Toast.LENGTH_LONG).show();

        // Call listener
        MqttManagerListener listener = mWeakListener.get();
        if (listener != null) {
            listener.onMqttDisconnected();
        }
    }

    // region MqttCallback
    @Override
    public void connectionLost(Throwable throwable) {


        if (throwable != null) {        // if disconnected because a reason show toast. Standard disconnect will have a null throwable
            Toast.makeText(mContext, R.string.mqtt_connection_lost, Toast.LENGTH_LONG).show();
        }

        mMqqtClientStatus = MqqtConnectionStatus.DISCONNECTED;

        MqttManagerListener listener = mWeakListener.get();
        if (listener != null) {
            listener.onMqttDisconnected();
        }
    }
    // endregion

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        String message = new String(mqttMessage.getPayload());

        if (message.length() > 0) {      // filter cleared messages (to avoid duplicates)


            MqttManagerListener listener = mWeakListener.get();
            if (listener != null) {
                listener.onMqttMessageArrived(topic, mqttMessage);
            }

            // Fix duplicated messages clearing the received payload and processing only non null messages
            mqttMessage.clearPayload();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {


    }

    // region MqttTraceHandler
    @Override
    public void traceDebug(String source, String message) {

    }

    // endregion

    @Override
    public void traceError(String source, String message) {

    }

    @Override
    public void traceException(String source, String message, Exception e) {

    }

    // Types
    public enum MqqtConnectionStatus {
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED,
        ERROR,
        NONE
    }

    // endregion

    public interface MqttManagerListener {
        void onMqttConnected();

        void onMqttDisconnected();

        void onMqttMessageArrived(String topic, @NonNull MqttMessage mqttMessage);
    }
}
