package com.otosone.bsscommunicator.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.otosone.bsscommunicator.ChargingItem;
import com.otosone.bsscommunicator.R;

import java.util.List;

public class ChargingAdapter extends BaseAdapter {

    private Context context;
    private List<ChargingItem> chargingItems;
    private LayoutInflater layoutInflater;

    public ChargingAdapter(Context context, List<ChargingItem> chargingItems) {
        this.context = context;
        this.chargingItems = chargingItems;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return chargingItems.size();
    }

    @Override
    public ChargingItem getItem(int position) {
        return chargingItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_charging, parent, false);
        }

        ChargingItem chargingItem = getItem(position);

        CheckBox checkBox = convertView.findViewById(R.id.charging_checkbox);
        TextView charging1_tv = convertView.findViewById(R.id.charging1_tv);
        TextView charging2_tv = convertView.findViewById(R.id.charging2_tv);

        checkBox.setChecked(chargingItem.isChecked());
        charging1_tv.setText(chargingItem.getText1());
        charging2_tv.setText(chargingItem.getText2());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToggleDialog(position);
            }
        });

        return convertView;
    }

    private void showToggleDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Inflate custom title view
        LayoutInflater inflater = LayoutInflater.from(context);
        View titleView = inflater.inflate(R.layout.charging_dialog_title, null);
        TextView titleId = titleView.findViewById(R.id.title_id);
        titleId.setText("ID: " + getItem(position).getText1());
        builder.setCustomTitle(titleView);

        String[] choices = {"START", "STOP"};
        int[] selectedIndex = new int[]{getItem(position).getText2().equals("START") ? 0 : 1};
        builder.setSingleChoiceItems(choices, selectedIndex[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedIndex[0] = which;
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                ChargingItem chargingItem = getItem(position);

                if (selectedIndex[0] == 0) {
                    chargingItem.setText2("START");
                } else {
                    chargingItem.setText2("STOP");
                }

                notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing, just close the dialog
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
