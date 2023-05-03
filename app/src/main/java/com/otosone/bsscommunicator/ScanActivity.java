package com.otosone.bsscommunicator;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.otosone.bsscommunicator.bluetooth.BluetoothConnectionService;
import com.otosone.bsscommunicator.databinding.ActivityScanBinding;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.ArrayList;
import java.util.List;

public class ScanActivity extends AppCompatActivity {

    private static final int REQUEST_FINE_LOCATION_PERMISSION = 100;
    private static final int REQUEST_ENABLE_BT = 101;
    private ActivityScanBinding binding;
    private BluetoothConnectionService bluetoothConnectionService;
    private boolean isBound = false;
    private List<RxBleDevice> foundDevices;
    private ArrayAdapter<RxBleDevice> arrayAdapter;
    private BluetoothAdapter bluetoothAdapter;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            BluetoothConnectionService.LocalBinder binder = (BluetoothConnectionService.LocalBinder) service;
            bluetoothConnectionService = binder.getService();
            bluetoothConnectionService.setMessageReceivedListener(message -> {
                binding.textViewReceivedData.setText(getString(R.string.received_data, message));
            });
            isBound = true;
            startScan();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothConnectionService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scan);

        binding.buttonSend.setOnClickListener(view -> {
            String message = binding.editTextMessage.getText().toString();
            if (isBound && !message.isEmpty()) {
                //bluetoothConnectionService.sendMessage();
            }
        });

        foundDevices = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, foundDevices);
        binding.listView.setAdapter(arrayAdapter);
        binding.listView.setOnItemClickListener((parent, view, position, id) -> {
            RxBleDevice selectedDevice = foundDevices.get(position);
            bluetoothConnectionService.setConnectionStateListener(new BluetoothConnectionService.ConnectionStateListener() {
                @Override
                public void onDeviceConnected() {
                    runOnUiThread(() -> {
                        Intent intent = new Intent(ScanActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    });
                }

                @Override
                public void onDeviceDisconnected() {
                    // Handle disconnection if needed
                }
            });
            bluetoothConnectionService.connectToDevice(selectedDevice);
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_FINE_LOCATION_PERMISSION);
        } else {
            if (bluetoothAdapter == null) {
                // Device doesn't support Bluetooth
                Toast.makeText(this, "Your device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            } else if (!bluetoothAdapter.isEnabled()) {
                // Bluetooth is not enabled
                showEnableBluetoothDialog();
            } else {
                bindService();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    private void startScan
            () {
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build();

        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")) // UART Service UUID
                .build();

        bluetoothConnectionService.scanBleDevices(scanSettings, scanFilter, this::onScanResult);
    }

    private void onScanResult(ScanResult scanResult) {
        RxBleDevice foundDevice = scanResult.getBleDevice();
        if (foundDevice != null && !foundDevices.contains(foundDevice)) {
            foundDevices.add(foundDevice);
            arrayAdapter.notifyDataSetChanged();
        }
    }

    private void bindService() {
        Intent bindIntent = new Intent(this, BluetoothConnectionService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (bluetoothAdapter == null) {
                    // Device doesn't support Bluetooth
                    Toast.makeText(this, "Your device does not support Bluetooth", Toast.LENGTH_SHORT).show();
                } else if (!bluetoothAdapter.isEnabled()) {
                    // Bluetooth is not enabled
                    showEnableBluetoothDialog();
                } else {
                    bindService();
                }
            } else {
                // Handle permission denial
            }
        }
    }

    private void showEnableBluetoothDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enable Bluetooth")
                .setMessage("Bluetooth is not enabled. Do you want to enable Bluetooth?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Toast.makeText(this, "Bluetooth is required for this app to work", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .create()
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth is enabled
                bindService();
            } else {
                // User denied enabling Bluetooth
                Toast.makeText(this, "Bluetooth is required for this app to work", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
