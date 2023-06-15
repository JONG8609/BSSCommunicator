package com.otosone.bssmgr.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.otosone.bssmgr.R;
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

        // Set the background of the item to the selector drawable
        convertView.setBackgroundResource(R.drawable.list_item_selector);

        RxBleDevice device = getItem(position);

        TextView tvName = (TextView) convertView.findViewById(R.id.device_name);
        tvName.setText(String.format("Device #%02d", position + 1));

        return convertView;
    }
}
