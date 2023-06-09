package com.otosone.bsscommunicator.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.otosone.bsscommunicator.listItem.ChargingItem;
import com.otosone.bsscommunicator.R;

import java.util.List;

public class ChargingAdapter extends BaseAdapter {

    private Context context;
    private List<ChargingItem> chargingItems;
    private LayoutInflater layoutInflater;

    public ChargingAdapter(Context context, List<ChargingItem> chargingItems) {
        this.context = context;
        this.chargingItems = chargingItems;
        this.layoutInflater =
                LayoutInflater.from(context);
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
        CheckBox checkBox = convertView.findViewById(R.id.charging_checkbox);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checkBox.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));
        }
        ChargingItem chargingItem = getItem(position);

        TextView charging1_tv = convertView.findViewById(R.id.charging1_tv);
        TextView charging2_tv = convertView.findViewById(R.id.charging2_tv);

        charging1_tv.setText(chargingItem.getId());
        charging2_tv.setText(chargingItem.getCharging());

        checkBox.setOnCheckedChangeListener(null); // Remove any existing listeners
        checkBox.setChecked(chargingItem.isChecked()); // Set the initial state of the checkbox

        // Add a new OnCheckedChangeListener
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Your logic for handling the checkbox change
            chargingItem.setChecked(isChecked);
        });

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

        // Inflate custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.charging_dialog_title, null);
        TextView titleId = dialogView.findViewById(R.id.title_id);
        titleId.setText(getItem(position).getId());

        String[] choices = {"START", "STOP"};
        int[] selectedIndex = new int[]{getItem(position).getCharging().equals("START") ? 0 : 1};

        ListView listView = dialogView.findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.my_list_item_single_choice, choices);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setItemChecked(selectedIndex[0], true);
        listView.setOnItemClickListener((parent, view, position1, id) -> selectedIndex[0] = position1);

        AlertDialog dialog = builder.setView(dialogView).create();

        Button okButton = dialogView.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChargingItem chargingItem = getItem(position);

                if (selectedIndex[0] == 0) {
                    chargingItem.setCharging("START");
                } else {
                    chargingItem.setCharging("STOP");
                }

                notifyDataSetChanged();
                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the dialog
                dialog.dismiss();
            }
        });

        dialog.show();
    }

}
