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

import java.util.UUID;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class BluetoothConnectionService extends Service {

    private final IBinder binder = new LocalBinder();
    private Disposable connectionDisposable;
    private RxBleConnection connection;
    private RxBleClient rxBleClient;
    private MessageReceivedListener messageReceivedListener;
    private static final String CHANNEL_ID = "BluetoothConnectionServiceChannel";

    public interface MessageReceivedListener {
        void onMessageReceived(String message);
    }

    public void setMessageReceivedListener(MessageReceivedListener listener) {
        this.messageReceivedListener = listener;
    }

    public class LocalBinder extends Binder {
        BluetoothConnectionService getService() {
            return BluetoothConnectionService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        rxBleClient = RxBleClient.create(this);
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
                        },
                        throwable -> {
                            Log.e("ConnectionError", "Error connecting to device", throwable);
                        }
                );
    }

    private void disconnect() {
        if (connectionDisposable != null && !connectionDisposable.isDisposed()) {
            connectionDisposable.dispose();
        }
    }

    private void onDeviceConnected(RxBleConnection connection) {
        this.connection = connection;
        setupNotification(connection);
    }

    public void sendMessage(String message) {
        if (connection == null || message.isEmpty()) {
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
                            String receivedData = new String(bytes);
                            Log.d("ReceivedData", "Data received: " + receivedData);
                            if (messageReceivedListener != null) {
                                messageReceivedListener.onMessageReceived(receivedData);
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
