package com.otosone.bsscommunicator;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

import java.util.UUID;

import io.reactivex.disposables.Disposable;

public class BluetoothConnectionService extends Service {

    private final IBinder binder = new LocalBinder();
    private Disposable connectionDisposable;
    private RxBleConnection connection;

    public class LocalBinder extends Binder {
        BluetoothConnectionService getService() {
            return BluetoothConnectionService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void connectToDevice(RxBleDevice device) {
        // Disconnect the current connection if there is one
        disconnect();

        connectionDisposable = device.establishConnection(false)
                .subscribe(
                        connection -> {
                            // Connection has been established, you can now perform operations on the connected device
                            onDeviceConnected(connection);
                        },
                        throwable -> {
                            // Handle connection error
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
        // Save the connection
        this.connection = connection;
        // Set up notifications for receiving data
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
                            // Data has been sent
                            Log.d("SendData", "Data sent successfully");
                        },
                        throwable -> {
                            // Handle error while sending data
                            Log.e("SendDataError", "Error sending data", throwable);
                        }
                );
    }

    private void setupNotification(RxBleConnection connection) {
        connection.setupNotification(UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E"))
                .flatMap(notificationObservable -> notificationObservable)
                .subscribe(
                        bytes -> {
                            // Data received
                            String receivedData = new String(bytes);
                            Log.d("ReceivedData", "Data received: " + receivedData);
                        },
                        throwable -> {
                            // Handle error while receiving data
                            Log.e("ReceiveDataError", "Error receiving data", throwable);
                        }
                );
    }
}
