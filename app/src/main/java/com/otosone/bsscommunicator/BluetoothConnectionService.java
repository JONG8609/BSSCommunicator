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

import java.nio.charset.StandardCharsets;
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
    public static final String ACTION_DATA_AVAILABLE = "com.otosone.bsscommunicator.ACTION_DATA_AVAILABLE";
    public static final String EXTRA_DATA = "com.otosone.bsscommunicator.EXTRA_DATA";

    private static final String CHANNEL_ID = "BluetoothConnectionServiceChannel";
    private Disposable notificationDisposable;

    private StringBuilder dataBuffer = new StringBuilder();
    public interface MessageReceivedListener {
        void onMessageReceived(String message);
    }

    public void setMessageReceivedListener(MessageReceivedListener messageReceivedListener) {
        this.messageReceivedListener = messageReceivedListener;
        Log.d("BluetoothConnService", "MessageReceivedListener set");
    }

    public class LocalBinder extends Binder {
        public BluetoothConnectionService getService() {
            return BluetoothConnectionService.this;
        }
    }



    public void setConnectionStateListener(ConnectionStateListener listener) {
        this.connectionStateListener = listener;
    }

    private void broadcastUpdate(final String action, String data) {
        final Intent intent = new Intent(action);
        if (data != null) {
            intent.putExtra(EXTRA_DATA, data);
        }
        sendBroadcast(intent);
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

    public void disconnect() {
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

    public void sendMessage(JSONObject jsonObject) {
        if (connection == null ||
                jsonObject == null) {
            Log.e("SendMessageError", "Connection is null or JSON object is null");
            return;
        }

        byte[] data = jsonObject.toString().getBytes(StandardCharsets.UTF_8);

        connection.writeCharacteristic(UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E"), data)
                .subscribe(
                        bytes -> {
                            Log.d("SendData", "Data sent successfully");
                            Log.d("Response", new String(bytes, StandardCharsets.UTF_8));
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
                            String receivedData = new String(bytes, StandardCharsets.US_ASCII);
                            Log.d("ReceivedData", "Data received (ASCII): " + receivedData);

                            String filteredData = receivedData.replaceAll("[^\\x20-\\x7E]", ""); // Remove non-printable ASCII characters
                            Log.d("FilteredData", "Filtered data: " + filteredData);

                            try {
                                JSONObject json = new JSONObject(filteredData);
                                if (messageReceivedListener != null) {
                                    messageReceivedListener.onMessageReceived(filteredData);
                                }

                                broadcastUpdate(ACTION_DATA_AVAILABLE, filteredData);
                            } catch (JSONException e) {
                                Log.e("JSONError", "Error parsing JSON: " + e.getMessage());
                            }
                        },
                        throwable -> {
                            Log.e("ReceiveDataError", "Error receiving data: " + throwable.getMessage());
                        }
                );

    }

    private void disposeNotification() {
        if (notificationDisposable != null && !notificationDisposable.isDisposed()) {
            notificationDisposable.dispose();
        }
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
                .setSmallIcon(R.mipmap.ic_launcher) // Replace with your own notification icon
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }
}
