package com.otosone.bsscommunicator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.otosone.bsscommunicator.ChargingItem;
import com.otosone.bsscommunicator.DoorItem;
import com.otosone.bsscommunicator.R;

import java.util.List;

public class DoorAdapter extends BaseAdapter {
    private Context context;
    private List<DoorItem> items;

    public DoorAdapter(Context context, List<DoorItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_door, parent, false);
        }

        DoorItem item = items.get(position);

        CheckBox checkBox = convertView.findViewById(R.id.door_checkbox);
        TextView text1 = convertView.findViewById(R.id.door1_tv);
        TextView text2 = convertView.findViewById(R.id.door2_tv);

        checkBox.setChecked(item.isChecked());
        text1.setText(item.getText1());
        text2.setText(item.getText2());

        return convertView;
    }
}
