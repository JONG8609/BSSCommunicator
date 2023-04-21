package com.otosone.bsscommunicator;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.otosone.bsscommunicator.databinding.ActivityScanBinding;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.disposables.Disposable;

public class ScanActivity extends AppCompatActivity {

    private RxBleClient rxBleClient;
    private Disposable scanDisposable;
    private Disposable connectionDisposable;
    private ListView listView;
    private ArrayAdapter<RxBleDevice> arrayAdapter;
    private List<RxBleDevice> foundDevices;
    private ActivityScanBinding binding;
    private TextView textViewReceivedData;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 100;
    private EditText editTextMessage;
    private Button buttonSend;
    private RxBleConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Databind();

        rxBleClient = RxBleClient.create(this);
        foundDevices = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, foundDevices);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            RxBleDevice selectedDevice = foundDevices.get(position);
            connectToDevice(selectedDevice);
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_FINE_LOCATION_PERMISSION);
            Log.d("permissionget", "pp");
        } else {
            startScan();
        }
    }

    private void Databind() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scan);
        listView = binding.listView;
        editTextMessage = binding.editTextMessage;
        buttonSend = binding.buttonSend;
        textViewReceivedData = binding.textViewReceivedData;
        buttonSend.setEnabled(false);
        buttonSend.setOnClickListener(view -> sendMessage());
        binding.setLifecycleOwner(this);
        binding.executePendingBindings();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
        if (connectionDisposable != null && !connectionDisposable.isDisposed()) {
            connectionDisposable.dispose();
        }
    }

    private void startScan() {
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build();

        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")) // UART Service UUID
                .build();

        scanDisposable = rxBleClient.scanBleDevices(scanSettings, scanFilter)
                .subscribe(scanResult -> onScanResult(scanResult),
                        throwable -> {
                            Log.d("error", "error");
                            Log.e("ScanError", "Error scanning for devices", throwable);
                        });
    }

    private void stopScan() {
        if (scanDisposable != null && !scanDisposable.isDisposed()) {
            scanDisposable.dispose();
        }
    }

    private void onScanResult(ScanResult scanResult) {
        RxBleDevice foundDevice = scanResult.getBleDevice();
        if (foundDevice != null && !foundDevices.contains(foundDevice)) {
            Log.d("result", "Found device: " + foundDevice.getName());
            foundDevices.add(foundDevice);
            arrayAdapter.notifyDataSetChanged();
        }
    }

    private void connectToDevice(RxBleDevice device) {
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
        // Enable the send button on the main UI thread
        runOnUiThread(() -> buttonSend.setEnabled(true));
        // Set up notifications for receiving data
        setupNotification(connection);
    }

    private void sendMessage() {
        if (connection == null || editTextMessage.getText().toString().isEmpty()) {
            return;
        }

        byte[] data = editTextMessage.getText().toString().getBytes();

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
                            textViewReceivedData.setText(getString(R.string.received_data, receivedData));
                        },
                        throwable -> {
                            // Handle error while receiving data
                            Log.e("ReceiveDataError", "Error receiving data", throwable);
                        }
                );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                startScan();
            } else {
                Log.d("starterror", "error");
            }
        }
    }
}
