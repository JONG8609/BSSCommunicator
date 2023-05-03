package com.otosone.bsscommunicator.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.otosone.bsscommunicator.MainActivity;

public class BluetoothStateReceiver extends BroadcastReceiver {

    private MainActivity mainActivity;

    public BluetoothStateReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            if (state == BluetoothAdapter.STATE_OFF) {
                // Bluetooth is turned off, switch to ScanFragment
                mainActivity.switchToScanFragment();
            }
        }
    }
}
