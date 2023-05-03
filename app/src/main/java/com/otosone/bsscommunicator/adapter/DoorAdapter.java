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

import com.otosone.bsscommunicator.listItem.DoorItem;
import com.otosone.bsscommunicator.R;

import java.util.List;

public class DoorAdapter extends BaseAdapter {

    private Context context;
    private List<DoorItem> doorItems;
    private LayoutInflater layoutInflater;

    public DoorAdapter(Context context, List<DoorItem> doorItems) {
        this.context = context;
        this.doorItems = doorItems;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return doorItems.size();
    }

    @Override
    public DoorItem getItem(int position) {
        return doorItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_door, parent, false);
        }

        DoorItem doorItem = getItem(position);

        CheckBox checkBox = convertView.findViewById(R.id.door_checkbox);
        TextView door1_tv = convertView.findViewById(R.id.door1_tv);
        TextView door2_tv = convertView.findViewById(R.id.door2_tv);

        checkBox.setChecked(doorItem.isChecked());
        door1_tv.setText(doorItem.getId());
        door2_tv.setText(doorItem.getDoorStatus());

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
        View titleView = inflater.inflate(R.layout.door_dialog_title, null);
        TextView titleId = titleView.findViewById(R.id.title_id);
        titleId.setText("ID: " + getItem(position).getId());
        builder.setCustomTitle(titleView);

        String[] choices = {"LOCK", "UNLOCK"};
        int[] selectedIndex = new int[]{getItem(position).getDoorStatus().equals("LOCK") ? 0 : 1};
        builder.setSingleChoiceItems(choices, selectedIndex[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedIndex[0] = which;
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                DoorItem doorItem = getItem(position);

                if (selectedIndex[0] == 0) {
                    doorItem.setDoorStatus("LOCK");
                } else {
                    doorItem.setDoorStatus("UNLOCK");
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
