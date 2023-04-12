package com.otosone.bsscommunicator;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.otosone.bsscommunicator.databinding.ActivityBluetoothScanBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothScanActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private BluetoothAdapter mBluetoothAdapter;
    private ImageButton scan_btn;
    private ActivityBluetoothScanBinding binding;
    private Context context;

    private ListView device_list;
    private List<String> deviceListItems = new ArrayList<>();
    private ArrayAdapter<String> deviceListAdapter;

    // Replace MY_UUID with a unique identifier for your app
    static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothSocket mSocket; // declare a member variable to store the BluetoothSocket
    private String mDeviceAddress = "00:00:00:00:00:00"; // replace with the MAC address of the device you want to connect to

    private final ActivityResultLauncher<Intent> enableBluetoothLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            checkPermissionsAndStartDiscovery();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);
        Databinding();
        context = this;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    enableBluetoothLauncher.launch(enableBtIntent);
                } else {
                    checkPermissionsAndStartDiscovery();
                }
            }
        });

        // Replace "your_list_view_id" with the ID of your ListView in your layout XML file
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceListItems);
        device_list.setAdapter(deviceListAdapter);

        device_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDevice = deviceListAdapter.getItem(position);
                String[] deviceInfo = selectedDevice.split(" - ");
                String deviceAddress = deviceInfo[1];

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mBluetoothAdapter.cancelDiscovery(); // Cancel discovery as it's resource-intensive

                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
                try {
                    mSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                    mSocket.connect();
                    // Connection is successful, you can now use the mSocket to communicate with the device
                    // You can also pass the mSocket to another activity or a background thread to handle the data transfer
                } catch (IOException e) {
                    Log.e("BluetoothScan", "Error connecting to the device: "
                            + e.getMessage());
                    try {
                        mSocket.close();
                    } catch (IOException closeException) {
                        Log.e("BluetoothScan", "Error closing socket after connection failure: " + closeException.getMessage());
                    }
                }
            }
        });

        registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    private void Databinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bluetooth_scan);
        scan_btn = binding.scanBtn;
        device_list = binding.deviceList;
    }

    private void checkPermissionsAndStartDiscovery() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(BluetoothScanActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
        } else {
            startDiscovery();
        }
    }

    private void startDiscovery() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mBluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                String deviceInfo = device.getName() + " - " + device.getAddress();

                // Check if the device is already in the list before adding it
                if (!deviceListItems.contains(deviceInfo)) {
                    Log.d("BluetoothScan", "Device found: " + deviceInfo);
                    deviceListItems.add(deviceInfo);
                    deviceListAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d("BluetoothScan", "Discovery finished");
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startDiscovery();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e("BluetoothScan", "Failed to close socket: " + e.getMessage());
            }
        }
    }
}
