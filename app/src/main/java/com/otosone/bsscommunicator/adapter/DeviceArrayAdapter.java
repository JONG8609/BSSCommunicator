package com.otosone.bsscommunicator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.otosone.bsscommunicator.R;
import com.polidea.rxandroidble2.RxBleDevice;

import java.util.List;

public class DeviceArrayAdapter extends ArrayAdapter<RxBleDevice> {

    public DeviceArrayAdapter(Context context, List<RxBleDevice> devices) {
        super(context, 0, devices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_device, parent, false);
        }

        RxBleDevice device = getItem(position);

        TextView tvName = (TextView) convertView.findViewById(R.id.device_name);
        tvName.setText(String.format("Device #%02d", position + 1));

        return convertView;
    }
}
