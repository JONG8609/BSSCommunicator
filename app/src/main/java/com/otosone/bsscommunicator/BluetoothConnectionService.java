package com.otosone.bsscommunicator;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class BluetoothConnectionService extends Service {

    private final IBinder binder = new LocalBinder();
    private Disposable connectionDisposable;
    private RxBleConnection connection;
    private RxBleClient rxBleClient;
    private MessageReceivedListener messageReceivedListener;
    private ConnectionStateListener connectionStateListener;
    private static final String CHANNEL_ID = "BluetoothConnectionServiceChannel";

    public interface MessageReceivedListener {
        void onMessageReceived(String message);
    }

    public void setMessageReceivedListener(MessageReceivedListener listener) {
        this.messageReceivedListener = new MessageReceivedListener() {
            @Override
            public void onMessageReceived(String message) {
                try {
                    JSONObject json = new JSONObject(message);
                    listener.onMessageReceived(json.toString());
                } catch (JSONException e) {
                    Log.e("JSONError", "Error parsing JSON", e);
                }
            }
        };
    }

    public class LocalBinder extends Binder {
       public BluetoothConnectionService getService() {
            return BluetoothConnectionService.this;
        }
    }



    public void setConnectionStateListener(ConnectionStateListener listener) {
        this.connectionStateListener = listener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        rxBleClient = RxBleClient.create(this);
    }

    public interface ConnectionStateListener {
        void onDeviceConnected();
        void onDeviceDisconnected();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void connectToDevice(RxBleDevice device) {
        disconnect();

        connectionDisposable = device.establishConnection(false)
                .subscribe(
                        connection -> {
                            onDeviceConnected(connection);
                            if (connectionStateListener != null) {
                                connectionStateListener.onDeviceConnected();
                            }
                        },
                        throwable -> {
                            Log.e("ConnectionError", "Error connecting to device", throwable);
                        }
                );
    }

    private void disconnect() {
        if (connectionDisposable != null && !connectionDisposable.isDisposed()) {
            connectionDisposable.dispose();
            if (connectionStateListener != null) {
                connectionStateListener.onDeviceDisconnected();
            }
        }
    }

    public boolean isConnected() {
        return connection != null;
    }

    private void onDeviceConnected(RxBleConnection connection) {
        this.connection = connection;
        setupNotification(connection);
    }

    public void sendMessage(String message) {
        if (connection == null || message.isEmpty()) {
            Log.e("SendMessageError", "Connection is null or message is empty");
            return;
        }

        byte[] data = message.getBytes();

        connection.writeCharacteristic(UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E"), data)
                .subscribe(
                        bytes -> {
                            Log.d("SendData", "Data sent successfully");
                        },
                        throwable -> {
                            Log.e("SendDataError", "Error sending data", throwable);
                        }
                );
    }

    private void setupNotification(RxBleConnection connection) {
        connection.setupNotification(UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E"))
                .flatMap(notificationObservable -> notificationObservable)
                .subscribe(
                        bytes -> {
                            String receivedData = new String(bytes, Charset.forName("UTF-8"));
                            Log.d("ReceivedData", "Data received: " + receivedData);
                            Log.d("ReceivedData", "Raw bytes: " + Arrays.toString(bytes));
                            try {
                                // Parse the received data as a JSON object
                                JSONObject json = new JSONObject(receivedData);
                                if (messageReceivedListener != null) {
                                    messageReceivedListener.onMessageReceived(receivedData);
                                }
                            } catch (JSONException e) {
                                // The received data is not a valid JSON string
                                Log.e("JSONError", "Error parsing JSON", e);
                            }
                        },
                        throwable -> {
                            Log.e("ReceiveDataError", "Error receiving data", throwable);
                        }
                );
    }

    public void scanBleDevices(ScanSettings scanSettings, ScanFilter scanFilter, Consumer<ScanResult> onScanResult) {
        Disposable scanDisposable = rxBleClient.scanBleDevices(scanSettings, scanFilter)
                .subscribe(onScanResult, throwable -> {
                    // Handle error
                });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForegroundServiceWithNotification();
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Bluetooth Connection Service";
            String description = "Service for managing Bluetooth connections";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void startForegroundServiceWithNotification() {
        Intent notificationIntent = new Intent(this, ScanActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bluetooth Connection Service")
                .setContentText("Service is running")
                .setSmallIcon(R.drawable.blank_rounded) // Replace with your own notification icon
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }
}
