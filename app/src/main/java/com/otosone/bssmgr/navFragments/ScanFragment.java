package com.otosone.bssmgr.navFragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.databinding.DataBindingUtil;

import com.otosone.bssmgr.R;
import com.otosone.bssmgr.adapter.DeviceArrayAdapter;
import com.otosone.bssmgr.bluetooth.BluetoothConnectionService;
import com.otosone.bssmgr.databinding.FragmentScanBinding;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanFilter;
import com.polidea.rxandroidble2.scan.ScanResult;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.ArrayList;
import java.util.List;

public class ScanFragment extends Fragment {
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 100;
    private static final int REQUEST_ENABLE_BT = 101;
    private FragmentScanBinding binding;
    private DeviceArrayAdapter.DeviceConnectListener deviceConnectListener;
    private BluetoothConnectionService bluetoothConnectionService;
    private List<RxBleDevice> foundDevices;
    private DeviceArrayAdapter arrayAdapter;
    private BluetoothAdapter bluetoothAdapter;

    public ScanFragment() {
        // Required empty public constructor
    }

    public void setBluetoothConnectionService(BluetoothConnectionService bluetoothConnectionService) {
        this.bluetoothConnectionService = bluetoothConnectionService;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_scan, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        foundDevices = new ArrayList<>();

        deviceConnectListener = device -> {
            bluetoothConnectionService.connectToDevice(device);
        };

        arrayAdapter = new DeviceArrayAdapter(requireContext(), foundDevices, deviceConnectListener);
        binding.listView.setAdapter(arrayAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_FINE_LOCATION_PERMISSION);
        } else {
            if (bluetoothAdapter == null) {
                // Device doesn't support Bluetooth
                Toast.makeText(requireContext(), "Your device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            } else if (!bluetoothAdapter.isEnabled()) {
                // Bluetooth is not enabled
                showEnableBluetoothDialog();
            } else {
                startScan();
            }
        }
    }

    private void showEnableBluetoothDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enable Bluetooth")
                .setMessage("Bluetooth is not enabled. Do you want to enable Bluetooth?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    startActivityForResult (enableBtIntent, REQUEST_ENABLE_BT);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Bluetooth is required for this app to work", Toast.LENGTH_SHORT).show();
                    //finish();
                })
                .create()
                .show();
    }

    private void startScan() {
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScan();
            } else {
                Toast.makeText(requireContext(), "Location permission is required for this app to work", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                startScan();
            } else {
                Toast.makeText(requireContext(), "Bluetooth is required for this app to work", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


