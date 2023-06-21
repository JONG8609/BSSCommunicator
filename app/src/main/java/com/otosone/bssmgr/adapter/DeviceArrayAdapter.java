package com.otosone.bssmgr.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.otosone.bssmgr.R;
import com.polidea.rxandroidble2.RxBleDevice;

import java.util.List;

public class DeviceArrayAdapter extends ArrayAdapter<RxBleDevice> {

    private DeviceConnectListener listener;

    public DeviceArrayAdapter(Context context, List<RxBleDevice> devices, DeviceConnectListener listener) {
        super(context, 0, devices);
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_device, parent, false);
        }

        RxBleDevice device = getItem(position);

        TextView tvName = convertView.findViewById(R.id.device_name);
        TextView tvMac = convertView.findViewById(R.id.device_mac);
        Button connectButton = convertView.findViewById(R.id.device_connect);

        String deviceName = device.getName();
        if(deviceName == null || deviceName.isEmpty()) {
            deviceName = "Unnamed device";
        }
        tvName.setText(deviceName);
        tvMac.setText(device.getMacAddress());

        // Set the onClickListener for the connect button
        connectButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeviceConnectClicked(device);
                connectButton.setBackgroundResource(R.drawable.rounded_button);
            }
        });

        return convertView;
    }

    public interface DeviceConnectListener {
        void onDeviceConnectClicked(RxBleDevice device);
    }
}
